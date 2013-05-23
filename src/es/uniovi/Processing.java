package es.uniovi;

import java.io.IOException;
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
	
	Semaphore leer;
	Semaphore escribir;
	static Message msg;
	String [] args;
	String cadena;
	String nick_old;

	public Processing(GlobalObject global, Semaphore leer, Semaphore escribir) {
		this.global=global;
		this.bufferInput=global.getBufferInput();
		this.bufferOutput=global.getBufferOutput();
		this.leer = leer;
		this.escribir = escribir;
	}

	/**
	 * Espera a tener un mensaje que analizar,
	 * cuando tenga mira de que tipo es y llama al metodo 
	 * correspondiente para analizarlo
	 */
	public void run() {

		Message msg=null;

		while(true) {

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

	private void processingUNKNOW(Message msg) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * 
	 * Funcion que detecta la peticion de salida del chat 
	 * de un usuario
	 * 
	 */
	private void processingQUIT(Message msg) {
		
		global.deleteUser(msg.getUser());
		//global.deleteGlobalUser();
		
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
		
	}

	/**
	 * 
	 * Funcion que busca todos los participantes de una sala y los procesa
	 * para generar un mensage de tipo WHO al origem
	 * 
	 */
	private void processingWHO(Message msg) {
		args = msg.getArgs(); /* Obtenemos los parametros del objeto mensaje, al ser de tipo WHO solo sera 1 y esta contendra el nombre de la sala */
		
		if (global.getRoomUsers().containsKey(args[0])) { /* Comprobamos que exista alguna sala con ese nombre */
			
			User [] users = global.getRoomUsers().get(args[0]); /* Si existe recogemos todos los usuarios de la sala y concatenamos sus nicks */
			
			for (int i = 0; i < users.length; i++) {
				
				cadena.concat(users[i].getNick());
				
				
			}
			
			/* Se genera el mensaje de respuesta a el cliente */
			
			msg.setType(Message.TYPE_WHO);
			msg.setPacket(Message.PKT_OK);
			msg.setArgs(new String[]{ args[0] + ";" + cadena });
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
			msg.setArgs(new String[]{" La sala solicitada ni existe actualmente" });
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
		// TODO Auto-generated method stub
		
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
		
		args = msg.getArgs(); /* Obtenemos los parametros del objeto mensaje, al ser de tipo NICK solo sera 1 y esta contendra el nuevo nick */
		
		if (global.getNickUsers().containsKey(args[0])) {
			
			// Comprobamos si el nick ya esta en uso y si es asi se crear el mensaje de error
			
			msg.setType(Message.TYPE_NICK);
			msg.setPacket(Message.PKT_ERR);
			msg.setArgs(new String[]{"Nick ya en uso, por favor intentelo de nuevo"});
			msg.setUser(msg.getUser());
			
			
		}
		else{
			/* En caso contrario se guardar el nick anterior y se realiza la modificacion */
			nick_old = msg.getUser().getNick();
			
			global.modifyUserNick(msg.getUser(), args[0]);
			
			/* Ya solo quedaria por una parte avisar a todos los participantes, comando INFO,  que compartan sala con el usuario */
			
			for (String key: global.getRoomUsers().keySet()){
				
				User[] users = global.getRoomUsers().get(key);
				
					for (int i = 0; i < users.length; i++) {
						
						if (users[i].getNick().equals(args[0])) {
							
								for (int j = 0; j < users.length; j++) {
									
									if (j != i) {
										// Crear el mensaje de aviso de cambio de nick para el resto de participantes de la sala
									msg.setType(Message.TYPE_NICK);
									msg.setPacket(Message.PKT_INF);
									msg.setArgs(new String[]{ nick_old + ";" + args[0] });
									msg.setUser(users[i]);
									}	
								}
							
						}
					}
				
				
			}
			
			/* Asi como al propio usuario de que el cambio se realizo correctamente */
			
					msg.setType(Message.TYPE_NICK);
					msg.setPacket(Message.PKT_OK);
					msg.setArgs(new String[]{ nick_old + ";" + args[0] });
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

	private void processingLEAVE(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void processingJOIN(Message msg) {
		
		// TODO Auto-generated method stub
		
	}

	private void processingMSG(Message msg) {
		// TODO Auto-generated method stub
		
	}




}


