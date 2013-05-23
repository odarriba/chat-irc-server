package es.uniovi;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.swing.internal.plaf.synth.resources.synth;
/**
 * Clase para contener las variables globales compartidas por los diferentes hilos, 
 * asegurando la sincronizacion de las operaciones que tengan que ser atomicas.
 * @author
 *
 */
public class GlobalObject {
	private Boolean running;
	private Boolean debug;
	private HashMap<String,User> nickUsers;
	private HashMap<String,ArrayList<User> > roomUsers;
	private BufferMessages bufferInput;
	private BufferMessages bufferOutput;
	
	private static final String version = "0.1";
	private static final String compilationDate = "2013-29-04";
	
	public GlobalObject() {
		this.running = true;
		this.nickUsers = new HashMap<String,User>();
		this.roomUsers = new HashMap<String,ArrayList<User> >();
		this.bufferInput = new BufferMessages();
		this.bufferOutput = new BufferMessages();
	}

	/**
	 * Obtener el numero de version de la compilacion
	 * @return String del numero de version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Obtener la fecha de la compilacion
	 * @return String de la fecha de compilacion
	 */
	public String getCompilationdate() {
		return compilationDate;
	}

	/**
	 * Metodo encargado de indicar a los hilos si deben continuar ejecutandose
	 * @return Boolean indicando si se continua el proceso o no
	 */
	public Boolean isRunning() {
		return running;
	}

	/**
	 * Metodo sincronizado para cambiar el estado de ejecucion de la aplicacion
	 * @param running Boolean que indica si se debe seguir ejecutando
	 */
	public synchronized void setRunning(Boolean running) {
		this.running = running;
	}

	/**
	 * Obtiene el estado de activacion del modo debug
	 * @return Boolean indicando si el modo debug est� activo o no
	 */
	public Boolean getDebug() {
		return debug;
	}

	/**
	 * Modifica el estado de activacion del modo debug
	 * @param debug Boolean de activacion del modo debug
	 */
	public void setDebug(Boolean debug) {
		this.debug = debug;
	}
	
	/*
	 * Funciones para trabajar con usuarios
	 */
	
	/**
	 * A�ade un usuario al objeto compartido de usuarios
	 * @param user Usuario a a�adir
	 */
	public synchronized void addUser(User user) {
		nickUsers.put(user.getNick(), user);
	}
	
	/**
	 * Funcion que nos inserta una nuevo cliente en la sala indicada
	 * Si la sala no existe nos genera una nueva
	 * @param user usuario a incluir
	 * @param room sala a modificar
	 */
	public synchronized void addUsertoRoom(User user, String room) {
		if (IsRoom(room)){
			if (!roomUsers.get(room).contains(user)) {
				roomUsers.get(room).add(user);
			}
		}
		else{
			roomUsers.put(room, new ArrayList<User>());
			roomUsers.get(room).add(user);
		}
			
	}
	
	/**
	 * Funcion que borra a un usuario de sala
	 * @param user usuario a borrar
	 * @param room sala a modificar
	 */
	
	public synchronized void removeUsertoRoom(User user, String room) {
		roomUsers.get(room).remove(user);
	}
	
	/**
	 * 
	 * @return
	 */
	
	public synchronized String[] listRooms(){
		
		String[] chain = new String[1];
		chain[0] = "";
		
		
		for (String key: roomUsers.keySet()){
			
			chain[0] += key + ";";
		}
		chain[0].substring(0, chain[0].length() - 1);
		
		return chain;
	}
	
	/**
	 * Comprueba la existencia de salas
	 * @return True cuando no hay salas existen salas
	 */
	
	public synchronized boolean noRooms(){
		
		return roomUsers.isEmpty();
		
	}
	
	/**
	 * Elimina un usuario del objeto compartido de usuarios
	 * @param user Usuario a eliminar
	 */
	
	public synchronized void deleteUser(User user) {
		// TODO: Abria que eliminar al usuario de las salas (y emitir los consiguientes mensajes) 
		// antes de eliminarlo de los objetos compartidos.
		for (String key: roomUsers.keySet()){ /* Recorremos todas las salas */
				
				roomUsers.get(key).remove(user);
					if( emptyRoom(key) )
						roomUsers.remove(key);
			}
		
		nickUsers.remove(user.getNick());
	}
	
	public synchronized boolean userInRoom(User user, String room){
		if(IsRoom(room))
		return roomUsers.get(room).contains(user);
		return false;
	}
	
	/**
	 * Comprueba si una sala esta vacia o no
	 * @param room es la sala que vamos a comprobar
	 * @return true si esta vacia y false si no existe o no tiene usuarios.
	 */
	
	public synchronized boolean emptyRoom(String room){
		
		if (roomUsers.containsKey(room)) {
			
			return roomUsers.get(room).isEmpty();
			
		}
		return false;
	}
	
	/**
	 * Comprueba la existencia de una sala
	 * @param room sala a comprobar
	 * @return true si existe y false no
	 */
	
	public synchronized boolean IsRoom(String room){
		
		return roomUsers.containsKey(room);
		
	}
	
	/**
	 * Cambiar el nick de un usuario existente
	 * @param user Usuario al que se le cambia el nick
	 * @param nick Nuevo nick a asignar
	 */
	public synchronized void modifyUserNick(User user, String nick) {
		String oldNick = user.getNick();
		user.setNick(nick);
		
		nickUsers.put(user.getNick(), user);
		nickUsers.remove(oldNick);
	}
	
	/**
	 * Obtener un usuario a partir de su nick
	 * @param nick
	 * @return User El usuario encontrado
	 */
	public User getUserByNick(String nick) {
		return nickUsers.get(nick);
	}
	
	/**
	 * Obtener el buffer de entrada de datos
	 * @return bufferInput
	 */
	
	public BufferMessages getBufferInput() {
		return bufferInput;
	}
	
	/**
	 * Obtener el buffer de salida de datos
	 * @return bufferOutput
	 */
	
	public BufferMessages getBufferOutput() {
		return bufferOutput;
	}

	public void setBufferOutput(BufferMessages bufferOutput) {
		this.bufferOutput = bufferOutput;
	}
	
	/**
	 * Obtener el hashmap de Usuarios
	 * @return nickUsers
	 */
	
	public HashMap<String, User> getNickUsers() {
		return nickUsers;
	}

	public void setNickUsers(HashMap<String, User> nickUsers) {
		this.nickUsers = nickUsers;
	}

	/**
	 * Obtener el hashmap de usuarios por sala
	 * @return roomUsers
	 */
	
	public HashMap<String, ArrayList<User> > getRoomUsers() {
		return roomUsers;
	}

	public void setRoomUsers(HashMap<String, ArrayList<User>> roomUsers) {
		this.roomUsers = roomUsers;
	}
	
}
