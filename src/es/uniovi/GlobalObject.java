package es.uniovi;
import java.util.HashMap;
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
	private HashMap<String,User[]> roomUsers;
	
	private static final String version = "0.1";
	private static final String compilationDate = "2013-29-04";
	
	public GlobalObject() {
		this.running = true;
		this.nickUsers = new HashMap<String,User>();
		this.roomUsers = new HashMap<String,User[]>();
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
	 * @return Boolean indicando si el modo debug est‡ activo o no
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
	 * A–ade un usuario al objeto compartido de usuarios
	 * @param user Usuario a a–adir
	 */
	public synchronized void addUser(User user) {
		nickUsers.put(user.getNick(), user);
	}
	
	/**
	 * Elimina un usuario del objeto compartido de usuarios
	 * @param user Usuario a eliminar
	 */
	public synchronized void deleteUser(User user) {
		// TODO: Abria que eliminar al usuario de las salas (y emitir los consiguientes mensajes) 
		// antes de eliminarlo de los objetos compartidos.
		nickUsers.remove(user.getNick());
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
}
