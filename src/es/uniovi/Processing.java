package es.uniovi;

import java.util.ArrayList;

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
	public void run(){

		Message msg = null;
		
		while(this.global.isRunning()) {
			//Se lee el buffer
			
			try {
				msg = this.bufferInput.get();
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
			// Si no se env’a comando, error
			if (msg.getPacket() != Message.PKT_CMD) {
				processingUNKNOW(msg);
				return;
			}
			
			switch(msg.getType()) {
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
		constructMessage(Message.TYPE_MISC, Message.PKT_ERR, new String[]{"El comando introducido no esta contemplado en el protocolo"}, msg.getUser());
	}


	/**
	 * 
	 * Funcion que detecta la peticion de salida del chat 
	 * de un usuario
	 * 
	 */
	private void processingQUIT(Message msg) {
		/* Unicamente se genera un mensaje de tipo QUIT - OK y se le envia al origen */
		constructMessage(Message.TYPE_QUIT, Message.PKT_OK, new String[]{msg.getUser().getNick()}, msg.getUser());
		
		global.deleteUser(msg.getUser());
		
		//eliminamos al usuario del arbol
		global.getPanel().delUser(msg.getUser().getNick());
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
			
			/* Se genera el mensaje de respuesta a el cliente */
			constructMessage(Message.TYPE_WHO, Message.PKT_INF, new String[]{  args[0], chain.substring(0 , chain.length() - 1) }, msg.getUser());
		} else {
			/* Si no es asi generamos un mensake de error */ 
			constructMessage(Message.TYPE_WHO, Message.PKT_ERR, new String[]{" La sala solicitada no existe actualmente" }, msg.getUser());
		}
	}

	private void processingLIST(Message msg) {
		constructMessage(Message.TYPE_LIST, Message.PKT_OK, global.listRooms() , msg.getUser());
	}

	/**
	 * 
	 * Funcion que decide si es posible el cambio de nick
	 * y en caso afirmativo toma las medidas necesarias
	 * para enviar todos los mensajes a los diferentes
	 * usuarios
	 * 
	 */
	private void processingNICK(Message msg){
		String[] args = msg.getArgs(); /* Obtenemos los parametros del objeto mensaje, al ser de tipo NICK solo sera 1 y esta contendra el nuevo nick */

		if (global.getNickUsers().containsKey(args[0])) { 
			// Comprobamos si el nick ya esta en uso y si es asi se crear el mensaje de error
			constructMessage(Message.TYPE_NICK, Message.PKT_ERR, new String[] {  "Nick ya en uso, por favor intentelo de nuevo" }, msg.getUser());
		} else {
			/* En caso contrario hacer la notificacionespara proceder al cambio de nick */
			String nick_old = msg.getUser().getNick();
			
			// Hacer primero el cambio de nick en las variables globales
			global.modifyUserNick(msg.getUser(), args[0]);
			
			// Informar al propio usuario de que se le ha cambiado el nombre
			constructMessage(Message.TYPE_NICK, Message.PKT_OK,  new String[] { nick_old, msg.getUser().getNick() }, msg.getUser());

			/* Ya solo quedaria por una parte avisar a todos los participantes, comando INFO,  que compartan sala con el usuario */
			
			//Eliminamos al usuario de todas las salas del arbol
			global.getPanel().delUser(nick_old);
			
			for (String key: global.getRoomUsers().keySet()) {
				ArrayList<User> users = global.getRoomUsers().get(key);

				if (users.contains(msg.getUser())) {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i) != msg.getUser()) 
							constructMessage(Message.TYPE_NICK, Message.PKT_INF, new String[] {  nick_old, msg.getUser().getNick()} , users.get(i));
					}
					
					// Volvemos a introducir al usuario en el arbol con el
					// nuevo nick
					if(!global.getPanel().isRoom(key)) {
						//si no existe la creamos
						global.getPanel().newRoom(key);
					}
					
					global.getPanel().newUser(key, msg.getUser().getNick());
				}
			}
		}
	}

	private void processingLEAVE(Message msg) {
		String room = msg.getArgs()[0];
		
		if (global.IsRoom(room)) {
			if (global.getRoomUsers().get(room).contains(msg.getUser())) {
				ArrayList<User> users = global.getRoomUsers().get(room);

				for (int i = 0; i < users.size(); i++) {
					if (users.get(i).getNick() == (msg.getUser().getNick())) {
						constructMessage(Message.TYPE_LEAVE, Message.PKT_OK,  new String[] { msg.getUser().getNick(), room }, users.get(i));
					} else {
						constructMessage(Message.TYPE_LEAVE, Message.PKT_INF, new String[] { msg.getUser().getNick(), room }, users.get(i));
					}

				}
				
				global.removeUsertoRoom(msg.getUser(), room);
				
				//Lo sacamos de la sala en el arbol
				global.getPanel().delUser(room, msg.getUser().getNick());
			} else {
				constructMessage(Message.TYPE_LEAVE, Message.PKT_ERR,  new String[] { "ERROR: Actualmente no esta en esta sala, por lo que no puedes salir de ella" } , msg.getUser());
			}
		}
	}

	private void processingJOIN(Message msg){
		String room = null;

		try {
			room = msg.getArgs()[0];
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Error en el argumento sala de mensaje JOIN");
			e.printStackTrace();
		}

		if (global.userInRoom(msg.getUser(), room)) {
			constructMessage(Message.TYPE_JOIN, Message.PKT_ERR, new String[] {  "ERROR: El usuario ya se encuentra en esta sala" } , msg.getUser());
		} else {
			global.addUsertoRoom(msg.getUser(), room);
			//introducimos al usuario en el arbol 
			//primero comprobamos si existe la sala
			if(!global.getPanel().isRoom(room)) {
				//si no existe la creamos
				global.getPanel().newRoom(room);
			}
			
			global.getPanel().newUser(room, msg.getUser().getNick());
	
			ArrayList<User> users = global.getRoomUsers().get(room);

			for (int i = 0; i < users.size(); i++) {
				if (users.get(i).getNick() == (msg.getUser().getNick())) { 
					constructMessage(Message.TYPE_JOIN, Message.PKT_OK, new String[] {  msg.getUser().getNick(), room } , users.get(i));
	                i++;
				} else {
					constructMessage(Message.TYPE_JOIN, Message.PKT_INF,  new String[] { msg.getUser().getNick(), room } , users.get(i));
				}
			}
		}
	}

	private void processingMSG(Message msg) {
		String [] args = msg.getArgs(); /* Obtenemos los parametros sala y mesaje */
		
		for (String key: global.getRoomUsers().keySet()) { /* Recorremos todas las salas */
			if (key.equals(args[0])) { /* Hasta detectar la que coincide con la que nos mando el usuario */
				ArrayList<User> salas = global.getRoomUsers().get(key); /* Obtenemos todos los usuarios de esa sala */
				
				for (int i = 0; i < salas.size(); i++) { /* Para cada uno le generamos un mensaje a medida */
					constructMessage(Message.TYPE_MSG, Message.PKT_INF, new String[] { msg.getUser().getNick(), args[0], args[1] }, salas.get(i));
				}
			}		
		}
	}
	
	private void constructMessage(Byte type, Byte pkt, String[] args, User user){
		Message msg = new Message();
		msg.setType(type);
		msg.setPacket(pkt);
		msg.setArgs(args);
		msg.setUser(user);

		try {
			/* Finalmente lo escribimos en el buffer de salida */
			this.bufferOutput.put(msg);
		} catch(InterruptedException e) {
			System.err.println("ERROR: Error al enviar un mensaje a "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}

}



