/*
 * Servidor de chat IRC-style
 * Trabajo grupal de Computadores
 * 
 * Clase Message
 */
package es.uniovi;
import java.util.Date;

/**
 * Clase de datos de mensaje internos para comunicar datos que provienen/van
 * hacia clientes, teniendo informacion sobre el usuario del que vienen/al que
 * van y distintos parametros para poder ser procesados/enviados.
 */
public class Message {
	/* Tipos de mensajes que se pueden enviar/recibir */
	public static final byte TYPE_MISC	= 0x00;
	public static final byte TYPE_MSG 	= 0x01;
	public static final byte TYPE_JOIN 	= 0x02;
	public static final byte TYPE_LEAVE = 0x03;
	public static final byte TYPE_NICK 	= 0x04;
	public static final byte TYPE_QUIT 	= 0x05;
	public static final byte TYPE_LIST 	= 0x10;
	public static final byte TYPE_WHO 	= 0x11;
	public static final byte TYPE_HELLO	= 0x20;
	
	/* Tipos de paquetes posibles */
	public static final byte PKT_CMD 	= 0x00;
	public static final byte PKT_INF 	= 0x01;
	public static final byte PKT_OK 	= 0x02;
	public static final byte PKT_ERR 	= 0x03;
	
	
	private byte type;
	private byte packet;
	private String[] args = new String[0];
	private Date timeStamp;
	
	private User user;

	/**
	 * Constructor de la clase que marcar el tiempo de generacion
	 */
	public Message(){
		// Generar la marca de tiempo del mensaje
		this.timeStamp = new Date();
	}
	
	/**
	 * Obtener el tipo de paquete
	 * @return el tipo de paquete en forma de byte
	 */
	public byte getType() {
		return this.type;
	}
	
	/**
	 * Obtener una descripcion literal del tipo de mensaje
	 * @return un String definiende el tipo de paquete
	 */
	public String getTypeLiteral() {
		switch(this.type){
			case TYPE_MISC:
				return "MISC";
			case TYPE_MSG:
				return "MSG";
			case TYPE_JOIN:
				return "JOIN";
			case TYPE_LEAVE:
				return "LEAVE";
			case TYPE_NICK:
				return "NICK";
			case TYPE_LIST:
				return "LIST";
			case TYPE_WHO:
				return "WHO";
			case TYPE_QUIT:
				return "QUIT";
			case TYPE_HELLO:
				return "HELLO";
			default:
				return "UNKNOW";
		}
	}
	
	/**
	 * Fijar el tipo de mensaje que se almacena
	 * @param type el tipo de paquete usando las variables de la clase
	 */
	public void setType(byte type) {
		boolean validType = true;
		
		/* Comprobar que sea de uno de los tipos de mensaje disponibles */
		switch(type){
			case TYPE_MISC:
				break;
			case TYPE_MSG:
				break;
			case TYPE_JOIN:
				break;
			case TYPE_LEAVE:
				break;
			case TYPE_NICK:
				break;
			case TYPE_LIST:
				break;
			case TYPE_WHO:
				break;
			case TYPE_QUIT:
				break;
			case TYPE_HELLO:
				break;
			default:
				validType = false;
				break;
		}
		
		// Comprobar que sea un tipo valido
		if (validType) {
			this.type = type;
		}
	}
	
	/**
	 * Obtener el tipo de paquete que se esta almacenando
	 * @return el tipo de paquete en formato byte
	 */
	public byte getPacket() {
		return this.packet;
	}
	
	/**
	 * Obtener el tipo de paquete que se esta almacenando
	 * @return el tipo de paquete en formato String
	 */
	public String getPacketLiteral(){
		switch(this.packet){
		case PKT_CMD:
			return "CMD";
		case PKT_INF:
			return "INF";
		case PKT_OK:
			return "OK";
		case PKT_ERR:
			return "ERR";
		default:
			return "UNKNOW";
		}
	}
	
	/**
	 * Fijar el tipo de paquete que se esta almacenando
	 * @param packet tipo de paquete en formato de las variables de la clase
	 */
	public void setPacket(byte packet) {
		boolean validPacket = true;
		
		/* Comprobar que sea de uno de los tipos paquetes disponibles */
		switch(packet){
			case PKT_CMD:
				break;
			case PKT_INF:
				break;
			case PKT_OK:
				break;
			case PKT_ERR:
				break;
			default:
				validPacket = false;
				break;
		}
		
		// Comprobar que sea un tipo valido
		if (validPacket == true) {
			this.packet = packet;
		}
	}
	
	/**
	 * Obtener el array de argumentos almacenados
	 * @return Array de Strings que forman los argumentos
	 */
	public String[] getArgs() {
		return this.args;
	}
	
	/**
	 * Fijar los argumentos que se almacenan
	 * @param args Array de Strings con los argumentos
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	/**
	 * Comprobar si el mensaje esta completo y es v‡lido
	 * @return Boolean indicando la validez del mensaje
	 */
	public boolean isValid() {
		boolean valido = true;
		
		/* Comprobar que sea de uno de los tipos paquetes disponibles */
		switch(this.packet){
			case PKT_CMD:
				break;
			case PKT_INF:
				break;
			case PKT_OK:
				break;
			case PKT_ERR:
				break;
			default:
				valido = false;
				break;
		}
		
		if (valido == false) {
			return valido;
		}
		
		/* Comprobar que sea de uno de los tipos de mensaje disponibles */
		switch(this.type){
			case TYPE_MISC:
				break;
			case TYPE_MSG:
				break;
			case TYPE_JOIN:
				break;
			case TYPE_LEAVE:
				break;
			case TYPE_NICK:
				break;
			case TYPE_LIST:
				break;
			case TYPE_WHO:
				break;
			case TYPE_QUIT:
				break;
			case TYPE_HELLO:
				break;
			default:
				valido = false;
				break;
		}
		
		// Comprobar que se ha fijado un usuario
		if (this.user == null) {
			valido = false;
		}else if (!this.user.isValid()) {
			// Si el usuario no es valido, el paquete tampoco
			valido = false;
		}
		
		return valido;
	}
	
	/**
	 * Obtener el time stamp de creacion del mensaje
	 * @return Date indicando el time stamp
	 */
	public Date getTimeStamp() {
		return this.timeStamp;
	}
	
	/**
	 * Obtener el usuario del mensaje
	 * @return User con el usuario del mensaje
	 */
	public User getUser() {
		return user;
	}

	/**
	 * Fijar el usuario del mensaje que se esta almacenando
	 * @param user User del mensaje
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * Metodo de debug del mensaje para errores y debug
	 */
	public void showInfo() {
		System.out.println("[MESSAGE]");
		System.out.println(" Paquete con TimeStamp: "+this.getTimeStamp());
		System.out.println(" ----------------------------------------");
		
		System.out.print(" Tipo de paquete: ");
		System.out.println(this.getPacketLiteral());
			
		System.out.print(" Tipo de mensaje: ");
		System.out.println(this.getTypeLiteral());
		
		String[] args = this.getArgs();
		System.out.println(" Cantidad de argumentos: "+args.length);
		
		for (int n = 0; n < args.length; n++) {
			System.out.println(" - Argumento "+(n+1)+": "+args[n]);
		}
		
		System.out.println("[/MESSAGE]");
	}
}
