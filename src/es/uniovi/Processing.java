package es.uniovi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Esta clase realiza el procesamiento de los mensaje que se encuentra
 * dentro del buffer de entrada. Asi mismo genera los mensajes de respuesta 
 * y los coloca dentro del buffer de salida.
 *
 */

public class Processing extends Thread{

	private GlobalObject global;
	private BufferMessages bufferInput;
	private BufferMessages bufferOutput;	
	
	public Processing(GlobalObject global) {
		
		this.global=global;
		this.bufferInput=global.getBufferInput();
		this.bufferOutput=global.getBufferOutput();
		
	}

	/**
	 * Espera a tener un mensaje que analizar,
	 * cuando tenga mira de que tipo es y llama al metodo 
	 * correspondiente para analizarlo
	 */
	public void run() {

		Message msg = null;
		
		while(global.isRunning()) {

			//Se lee el buffer
			
			try {
				msg = bufferInput.get();
			} catch (InterruptedException e) {
				System.err.println("Se ha detectado un error al leer un mensaje");	
				e.printStackTrace();				
			}
			
			typeProcessing(msg);
			
			
		}


	}
	
	/**
	 * En funcion del tipo de mensaje llama a la funcion
	 * que le corresponda
	 * @param msg mensaje que se analiza
	 */
	
	private void typeProcessing(Message msg) {
		if (msg.isValid()) {
			
				switch(msg.getType()){
				
				case Message.TYPE_MSG:
					processingMSG(msg);
					break;
				case Message.TYPE_JOIN:
					processingJOIN(msg);
					break;
				case Message.TYPE_LEAVE:
					processingLEAVE(msg);
					break;
				case Message.TYPE_NICK:
					processingNICK(msg);
					break;
				case Message.TYPE_LIST:
					processingLIST(msg);
					break;
				case Message.TYPE_WHO:
					processingWHO(msg);
					break;
				case Message.TYPE_QUIT:
					processingQUIT(msg);
					break;
				default:
					processingUNKNOW(msg);
					break;
				}
		}
		
	}


	/**
	 * 
	 * Funcion que detecta comandos incrrrectos
	 * 
	 */
	
	private void processingUNKNOW(Message msg) {
		msg.setType(Message.TYPE_MISC);
		msg.setPacket(Message.PKT_ERR);
		msg.setArgs(new String[]{"El comando introducido no esta contemplado en el protocolo"});
		msg.setUser(msg.getUser());
		
		/* Finalmente lo escribimos en el buffer de salida */
		try {
			this.bufferOutput.put(msg);
		} catch(InterruptedException e) {
			System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
		
		
	}


	/**
	 * 
	 * Funcion que detecta la peticion de salida del chat 
	 * de un usuario
	 * 
	 */
	
	private void processingQUIT(Message msg) {
		
		/* Unicamente se genera un mensaje de tipo QUIT - OK y se le envia al origen */
		msg.setType(Message.TYPE_QUIT);
		msg.setPacket(Message.PKT_OK);
		msg.setArgs(new String[]{msg.getUser().getNick()});
		msg.setUser(msg.getUser());
		
		/* Finalmente lo escribimos en el buffer de salida */
		try {
			this.bufferOutput.put(msg);
		} catch(InterruptedException e) {
			System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
		
		global.deleteUser(msg.getUser());
		
	}

	/**
	 * 
	 * Funcion que busca todos los participantes de una sala y los procesa
	 * para generar un mensage de tipo WHO al origem
	 * 
	 */
	
	private void processingWHO(Message msg) {
		
		String chain = "";
		String[] args = msg.getArgs(); /* Obtenemos los parametros del objeto mensaje, al ser de tipo WHO solo sera 1 y esta contendra el nombre de la sala */
		
		if (global.getRoomUsers().containsKey(args[0])) { /* Comprobamos que exista alguna sala con ese nombre */
			
			ArrayList<User> users = global.getRoomUsers().get(args[0]); /* Si existe recogemos todos los usuarios de la sala y concatenamos sus nicks */
			
			for (int i = 0; i < users.size(); i++) {
				
				chain += users.get(i).getNick() + ";" ;
				
				
			}
			System.out.println(chain);
			/* Se genera el mensaje de respuesta a el cliente */
			
			msg.setType(Message.TYPE_WHO);
			msg.setPacket(Message.PKT_INF);
			msg.setArgs(new String[]{  args[0] + ";" + chain.substring(0 , chain.length() - 1) });
			msg.setUser(msg.getUser());
			
			/* Finalmente lo escribimos en el buffer de salida */
			try {
				this.bufferOutput.put(msg);
			} catch(InterruptedException e) {
				System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
				e.printStackTrace();
			}
		}
		else{
			
			/* Si no es asi generamos un mensake de error */ 
			
			msg.setType(Message.TYPE_WHO);
			msg.setPacket(Message.PKT_ERR);
			msg.setArgs(new String[]{" La sala solicitada no existe actualmente" });
			msg.setUser(msg.getUser());
			
			/* Finalmente lo escribimos en el buffer de salida */
			
			try {
				this.bufferOutput.put(msg);
			} catch(InterruptedException e) {
				System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
				e.printStackTrace();
			}
		}
		
	}

	private void processingLIST(Message msg) {
		
		 if (global.noRooms()) {
			 msg.setType(Message.TYPE_LIST);
			 msg.setPacket(Message.PKT_ERR);
			 msg.setArgs(new String[]{" ERROR: No hay salas abiertas" });
			 msg.setUser(msg.getUser());
			
		}
		 else
			 {
			 msg.setType(Message.TYPE_LIST);
			 msg.setPacket(Message.PKT_OK);
			 msg.setArgs( global.listRooms());
			 msg.setUser(msg.getUser());
			 }
		 
		try {
		
		/* Finalmente lo escribimos en el buffer de salida */
		this.bufferOutput.put(msg);
		} catch(InterruptedException e) {
		System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
		e.printStackTrace();
		}
	}

	/**
	 * 
	 * Funcion que decide si es posible el cambio de nick
	 * y en caso afirmativo toma las medidas necesarias
	 * para enviar todos los mensajes a los diferentes
	 * usuarios
	 * 
	 */
	
	private void processingNICK(Message msg) {

		String[] args = msg.getArgs(); /* Obtenemos los parametros del objeto mensaje, al ser de tipo NICK solo sera 1 y esta contendra el nuevo nick */

		if (global.getNickUsers().containsKey(args[0])) {

			// Comprobamos si el nick ya esta en uso y si es asi se crear el mensaje de error

			msg.setType(Message.TYPE_NICK);
			msg.setPacket(Message.PKT_ERR);
			msg.setArgs(new String[]{"Nick ya en uso, por favor intentelo de nuevo"});
			msg.setUser(msg.getUser());
			try {

				/* Finalmente lo escribimos en el buffer de salida */
				this.bufferOutput.put(msg);
			} catch(InterruptedException e) {
				System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
				e.printStackTrace();
			}


		}

		else{
			/* En caso contrario se guardar el nick anterior y se realiza la modificacion */

			String nick_old = msg.getUser().getNick();

			global.modifyUserNick(msg.getUser(), args[0]);

			/* Ya solo quedaria por una parte avisar a todos los participantes, comando INFO,  que compartan sala con el usuario */

			Message msgout = new Message();
			msgout.setPacket(Message.PKT_OK);
			msgout.setType(Message.TYPE_NICK);
			msgout.setArgs(new String[]{ nick_old + ";" + msg.getUser().getNick()});
			msgout.setUser(msg.getUser());

			try {

				/* Finalmente lo escribimos en el buffer de salida */
				this.bufferOutput.put(msgout);
			} catch(InterruptedException e) {
				System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
				e.printStackTrace();
			}



			for (String key: global.getRoomUsers().keySet()){

				ArrayList<User> users = global.getRoomUsers().get(key);

				if (users.contains(msg.getUser())) {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i) != msg.getUser()) {
							msgout = new Message();
							msgout.setPacket(Message.PKT_INF);
							msgout.setType(Message.TYPE_NICK);
							msgout.setArgs(new String[]{ nick_old + ";" + msg.getUser().getNick()});
							msgout.setUser(users.get(i));

							try {

								/* Finalmente lo escribimos en el buffer de salida */
								this.bufferOutput.put(msgout);
							} catch(InterruptedException e) {
								System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
								e.printStackTrace();
							}
						}
					}


				}

			}

		}

		/* Asi como al propio usuario de que el cambio se realizo correctamente */

	}

	private void processingLEAVE(Message msg) {
		Message msgout = null;
		String room = msg.getArgs()[0];
		
		if(global.IsRoom(room)){
			
			if (global.getRoomUsers().get(room).contains(msg.getUser())) {
				
				ArrayList<User> users = global.getRoomUsers().get(room);

				for (int i = 0; i < users.size(); i++) {
					
					 msgout = new Message();
					
					if (users.get(i).getNick() == (msg.getUser().getNick())) {
					
						msgout.setPacket(Message.PKT_OK);
					}
					else{
						msgout.setPacket(Message.PKT_INF);
					}

					msgout.setType(Message.TYPE_LEAVE);
					msgout.setArgs(new String[]{ msg.getUser().getNick() + ";" + room });
					msgout.setUser(users.get(i));
					
					try {

						/* Finalmente lo escribimos en el buffer de salida */
						this.bufferOutput.put(msgout);
					} catch(InterruptedException e) {
						System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
						e.printStackTrace();
					}


				}
				
				global.removeUsertoRoom(msg.getUser(), room);
				
			}
			else{
				 msgout = new Message();
				 msgout.setPacket(Message.PKT_ERR);
				 msgout.setType(Message.TYPE_LEAVE);
				 msgout.setArgs(new String[]{ "ERROR: Actualmente no esta en esta sala, por lo que no puedes salir de ella" });
				 msgout.setUser(msg.getUser());
				 try {

						/* Finalmente lo escribimos en el buffer de salida */
						this.bufferOutput.put(msgout);
					} catch(InterruptedException e) {
						System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
						e.printStackTrace();
					}
			
			}
			
			
			
			
		}
	}

	private void processingJOIN(Message msg) {

		String room = null;

		try {
			room = msg.getArgs()[0];
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Error en el argumento sala de mensaje JOIN");
			e.printStackTrace();
		}

		if (global.userInRoom(msg.getUser(), room)) {
			msg.setType(Message.TYPE_JOIN);
			msg.setPacket(Message.PKT_ERR);
			msg.setArgs(new String[]{"ERROR: El usuario ya se encuentra en esta sala"});
			msg.setUser(msg.getUser());
			try {

				/* Finalmente lo escribimos en el buffer de salida */
				this.bufferOutput.put(msg);
			} catch(InterruptedException e) {
				System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
				e.printStackTrace();
			}
		}

		else{
			global.addUsertoRoom(msg.getUser(), room);
	
			ArrayList<User> users = global.getRoomUsers().get(room);

			for (int i = 0; i < users.size(); i++) {
				
				Message msgout = new Message();
				
				if (users.get(i).getNick() == (msg.getUser().getNick())) {
				
					msgout.setPacket(Message.PKT_OK);
				}
				else{
					msgout.setPacket(Message.PKT_INF);
				}

				msgout.setType(Message.TYPE_JOIN);
				msgout.setArgs(new String[]{ msg.getUser().getNick() + ";" + room });
				msgout.setUser(users.get(i));

				try {

					/* Finalmente lo escribimos en el buffer de salida */
					this.bufferOutput.put(msgout);
				} catch(InterruptedException e) {
					System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
					e.printStackTrace();
				}




			}

		}



	}

	private void processingMSG(Message msg) {
		
		String [] args = msg.getArgs(); /* Obtenemos los parametros sala y mesaje */
		
		for (String key: global.getRoomUsers().keySet()){ /* Recorremos todas las salas */
			
			if (key.equals(args[0])){ /* Hasta detectar la que coincide con la que nos mando el usuario */
				
				ArrayList<User> salas = global.getRoomUsers().get(key); /* Obtenemos todos los usuarios de esa sala */
			
						for (int i = 0; i < salas.size(); i++) { /* Para cada uno le generamos un mensaje a medida */
							Message msgout = new Message();
							msgout.setType(Message.TYPE_MSG);
							msgout.setPacket(Message.PKT_INF);
							msgout.setArgs(new String[]{ msg.getUser().getNick() + ";" + args[0] + ";" + args[1] });
							msgout.setUser(salas.get(i));

							try {
								
								/* Finalmente lo escribimos en el buffer de salida */
								this.bufferOutput.put(msgout);
							} catch(InterruptedException e) {
								System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+ msg.getUser().getCompleteInfo());
								e.printStackTrace();
							}
						}
			}	
						
		}
	}

}



