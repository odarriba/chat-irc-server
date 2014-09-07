package es.uniovi;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Thread {
	public Integer port;
	public Boolean debug;
	public boolean panel;
	private GlobalObject global;
	private ServerSocket socketPrincipal;
	private NetworkOut netOut;
	private Processing process;
	
	/**
	 * Constructor de la clase Main, donde se crearan todos los objetos necesarios para arrancar el servidor.
	 * Ademas se crean los diferentes hilos pero sin lanzarlos aun.
	 * @param port Puerto de la aplicacion
	 * @param debug Boolean indicando si se activa el modo debug
	 */
	public Main(Integer port, Boolean debug,Boolean panel) {
		
		this.port = port;
		this.debug = debug;
		this.global = new GlobalObject();
		
		this.global.setDebug(this.debug);
		//por defecto no tenemos interfaz
		this.panel=panel;
		this.global.setHasPanel(panel);
		
		// Arrancar los hilos necesarios
		
		this.netOut = new NetworkOut(this.global);
		this.process = new Processing(this.global);
		// TODO: Aqui se crearian los objetos de los hilos de procesamiento
	}
	
	public void run() {

		Socket socketAccepted;
		
		Message msgWelcome;
		Integer usersAccepted=0;
		
		// Mensaje de cabecera con version y fecha de compilacion
		System.out.println("Servidor de ChatIRC v"+this.global.getVersion()+" ("+this.global.getCompilationdate()+")");
		System.out.println("--------------------------------------");
		
		if (this.global.getDebug()) { 
			System.out.println("INFO: Servidor en modo DEBUG.");
		}
		
		// Intentar abrir el socket principal
		System.out.println("INFO: Arrancando el servidor en el puerto "+this.port+"...");
		
		try {
			socketPrincipal = new ServerSocket(this.port);
		} catch (IOException e) {
			// En caso de error notificar al adminsitrador y salir de la ejecucion.
			System.err.println("ERROR: Error al intentar abrir un socket en el puerto "+this.port);
			e.printStackTrace();
			return;
		}
		
		// Arrancar los diferentes hilos iniciales de la aplicaci�n
		this.netOut.start();
		this.process.start();
		
		//solo arracamos la interfaz si se nos indica
		if(this.global.getHasPanel()) {
			global.getPanel().build();
		}
		
		while(this.global.isRunning()) {
			try {
				socketAccepted = socketPrincipal.accept();
				usersAccepted++;
				
				// Crear el usuario y registrarlo
				User user = new User("Anonimo"+usersAccepted, socketAccepted);
				this.global.addUser(user);
				
				// Crear el hilo de lectura de este usuario en particular
				new NetworkIn(user, this.global);
				
				System.out.println("INFO: Mensaje de bienvenida enviado a usuario "+user.getCompleteInfo());
				
				// Crear el mensaje de bienvenida y enviarlo
				msgWelcome = new Message();
				msgWelcome.setType(Message.TYPE_HELLO);
				msgWelcome.setPacket(Message.PKT_OK);
				msgWelcome.setArgs(new String[]{"Bienvenido a Servidor de ChatIRC v"+this.global.getVersion()+" ("+this.global.getCompilationdate()+")"});
				msgWelcome.setUser(user);
				
				try {
					global.getBufferOutput().put(msgWelcome);
				} catch(InterruptedException e) {
					System.err.println("ERROR: Error al enviar el mensaje de bienvenida a "+user.getCompleteInfo());
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				// En caso de error notificar al adminsitrador y volver a ejecutar el bucle.
				System.err.println("ERROR: Error al aceptar las peticiones en el socket principal.");
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
		Boolean panel=false;
		Main main;
		
		// Si no hay los suficientes parametros o si se solicita la ayuda, mostrar el mensaje.
		if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
			System.err.println("Uso: ServidorChatIRC <puerto> [-d | --debug] [-p | --panel]");
			return;
		}
		
		// Leer el puerto de red
		port = new Integer(args[0]);
		
		// Comprobar que el puerto est� entre los valores admitidos
		if (port < 1 || port > 65535) {
			System.err.println("Error: el n�mero de puerto debe est�r comprendido entre 0 y 65535");
			return;
		}
		
		// Si se ha solicitado el modo debug o panel, activar el flag
		if (args.length > 1) {
			for(int i=1;i<args.length;i++){
				if((args[i].equals("-d") || args[i].equals("--debug")))
					debug = true;
				else if((args[i].equals("-p") || args[i].equals("--panel")))
					panel = true;
			}
		}
		
		// Inicializar el hilo Main
		main = new Main(port, debug, panel);
		main.start();
		
		return;
	}

}
