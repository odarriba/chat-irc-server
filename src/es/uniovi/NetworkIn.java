package es.uniovi;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Clase para escuchar a cada socket de usuario de forma independiente y convertir sus mensajes
 * al formato interno para poder añadirlos al buffer de entrada.
 */
public class NetworkIn extends Thread {
	private User user;
	private DataInputStream inputStream;
	private Socket socket;
	private GlobalObject global;
	private BufferMessages bufferInput;
	private Boolean threadRunning;	// Varibale para asegurar que el hilo se cierrfe independientemente cuando sea necesario
	
	/**
	 * Constructor de la clase NetworkIn que se autolanza
	 * @param user Usuario al que se lee
	 * @param global Variable global comun
	 * @param bufferInput Buffer de mensajes de entrada
	 */
	public NetworkIn(User user, GlobalObject global, BufferMessages bufferInput) {
		// Asignar las variables de la clase
		this.user = user;
		this.global = global;
		this.bufferInput = bufferInput;
		this.threadRunning = true;
		
		// Intentar obtener el socket y el InputStream
		try {
			this.socket = this.user.getSocket();
			
			if (this.socket.isClosed()==true || this.socket.isConnected() == false) {
				System.err.println("ERROR: Se ha intentado crear un listener a un socket que aparece cerrado o sin conectar.");
			} else {
				this.inputStream = new DataInputStream(this.socket.getInputStream());
			}
		} catch (IOException e) {
			System.err.println("ERROR: Error al crear el listener para escuchar al puerto "+this.socket.getInetAddress().getHostAddress()+":"+this.socket.getPort());
		}
		
		// Lanzar el hilo
		this.start();
	}
	
	public void run() {
		Message msg;
		System.out.println("INFO: Creado listener para el cliente en: "+this.socket.getInetAddress().getHostAddress());
		
		while(this.global.isRunning() && this.threadRunning) {
			msg = new Message(); // Limpiar la variable
			
			try {
				msg = readMessage();
			} catch (IOException e) {
				if (this.socket.isClosed() || this.socket.isConnected() == false) {
					// Se ha cerrado el socket, borrar los datos del usuario y cancelar la ejecucion de este hilo
					System.out.println("INFO: Se ha detectado la desconexion brusca de "+this.user.getNick()+" en el socket "+this.socket.getInetAddress().getHostAddress()+". Se dispone a eliminarlo del sistema.");
					this.global.deleteUser(this.user);
					
					// Cerrar el listener
					this.threadRunning = false;
				}
			}
			
			// Comprobar que el mensaje leido este completo y sea valido
			if (msg.isValid()) {
				try {
					// Introducirlo en el buffer de entrada
					this.bufferInput.put(msg);
				} catch (InterruptedException e) {
					System.err.println("ERROR: Error al introducir el mensaje: ");
					msg.showInfo(); // Mostrar la info del error
					e.printStackTrace();
				}
			}
		}
	}
	
	private byte[] readByteArray(int size) throws IOException {
		byte[] salida = new byte[size];
		
		for(int n = 0; n < size; n++) {
			salida[n]=this.inputStream.readByte();
		}
		
		return salida;
	}
	
	private Message readMessage() throws IOException {
		Message msg;		// El objeto a crear
		short sizeLoad;		// Tamaño de la carga
		short numArgs;		// Numero de argumentos
		short sizeArg;		// Tamaño de cada argumento cuando se trate
		byte[] argBytes;	// Array de los bytes de cada argumento
		String[] args;		// Array de los argumentos ya convertidos
		
		msg = new Message(); // Crear el objeto
		
		// Leer el tipo de paquete y el tipo de mensaje
		msg.setPacket(this.inputStream.readByte());
		msg.setType(this.inputStream.readByte());
		
		// Leer tama√±o de la carga
		sizeLoad = this.inputStream.readShort();
		
		if (sizeLoad > 0) { 
			// Si hay carga, leer el n√∫mero de par√°metros
			numArgs = this.inputStream.readShort();
			args = new String[numArgs];
			
			// Procesar los argumentos recibidos
			for(int n=0; n<numArgs; n++) {
				// Tama√±o en bytes del argumento
				sizeArg = this.inputStream.readShort();
				
				if (sizeArg > 0){
					// Leer argumento y convertirlo
					argBytes = readByteArray((int)sizeArg);
					args[n] = new String(argBytes, "UTF-8");
				}
				else {
					args[n] = new String();
				}
			}
		}
		else {
			args = new String[0];
		}
		
		// Almacenar argumentos y añadir el usuario
		msg.setArgs(args);
		msg.setUser(this.user);
		
		return msg;
	}
}
