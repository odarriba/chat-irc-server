package es.uniovi;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class Main extends Thread {
	public Integer port;
	public Boolean debug;
	private GlobalObject global;
	private ServerSocket socketPrincipal;
	private BufferMessages bufferInput;
	private BufferMessages bufferOutput;
	private NetworkOut netOut;
	
	public Main(Integer port, Boolean debug) {
		this.port = port;
		this.debug = debug;
		this.global = new GlobalObject();
		
		this.global.setDebug(this.debug);
		
		// Arrancar los hilos necesarios
		this.bufferInput = new BufferMessages();
		this.bufferOutput = new BufferMessages();
		
		this.netOut = new NetworkOut(this.bufferOutput, this.global);
	}
	
	public void run() {
		Socket socketAccepted;
		Integer usersAccepted=0;
		
		// Mensaje de cabecera con version y fecha de compilacion
		System.out.println("Servidor de Chat v"+this.global.getVersion()+" ("+this.global.getCompilationdate());
		System.out.println("-----------------------------------------------");
		
		if (this.global.getDebug()) { 
			System.out.println("INFO: Arrancando en modo DEBUG.");
		}
		
		// Intentar abrir el socket principal
		System.out.println("INFO: Arrancando el servidor en el puerto "+this.port+"...");
		try {
			socketPrincipal = new ServerSocket(this.port);
		} catch (IOException e) {
			// En caso de error notificar al adminsitrador y salir de la ejecucion.
			System.err.println("ERROR: Error al intentar abrir un socket en el puerto "+this.port);
			System.err.println(e.getMessage());
			e.printStackTrace();
			return;
		}
		
		// Arrancar los diferentes hilos iniciales de la aplicación
		this.netOut.start();
		
		while(this.global.isRunning()) {
			try {
				socketAccepted = socketPrincipal.accept();
				usersAccepted++;
				
				// Crear el usuario y registrarlo
				User usuario = new User("Anónimo"+usersAccepted, socketAccepted);
				this.global.addUser(usuario);
				
				// Crear el hilo de lectura de este usuario en particular
				
			} catch (IOException e) {
				// En caso de error notificar al adminsitrador y volver a ejecutar el bucle.
				System.err.println("ERROR: Error al aceptar las peticiones en el socket principal.");
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Integer port;
		Boolean debug = false;
		Main main;
		
		// Si no hay los suficientes parametros o si se solicita la ayuda, mostrar el mensaje.
		if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
			System.err.println("Uso: ServidorChatIRC <puerto> [-d | --debug]");
			return;
		}
		
		// Leer el puerto de red
		port = new Integer(args[0]);
		
		// Comprobar que el puerto esté entre los valores admitidos
		if (port < 1 || port > 65535) {
			System.err.println("Error: el número de puerto debe estár comprendido entre 0 y 65535");
			return;
		}
		
		// Si se ha solicitado el modo debug, activar el flag
		if (args.length > 1 && (args[1].equals("-d") || args[1].equals("--debug"))) {
			debug = true;
		}
		
		// Inicializar el hilo Main
		main = new Main(port, debug);
		main.start();
		
		return;
	}

}
