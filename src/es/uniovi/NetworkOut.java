package es.uniovi;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * Clase de salida de red que obtiene paquetes del buffer de salida para enviarlos
 * a sus respectivos destinatarios, objetos de la clase User incrustados dentro
 * de los Message's
 */
public class NetworkOut extends Thread {
	private GlobalObject global;
	private BufferMessages bufferOutput;

	/**
	 * Constructor de la clase NetworkOut
	 * @param bufferOutput Buffer de mensajes de salida
	 * @param global Variable GlobalObject con las variables globales
	 */
	public NetworkOut(BufferMessages bufferOutput, GlobalObject global) {
		this.global = global;
		this.bufferOutput = bufferOutput;
	}
	
	/**
	 * Metodo de ejecucion continua del hilo
	 */
	public void run() {
		Message outputMsg;
		
		// Bucle infinito de ejecucion que obtiene mensajes del buffer y los saca por la red.
		while (this.global.isRunning()) {
			outputMsg = new Message(); // Limpiar el mensaje anterior con un objeto nuevo
			
			// Intentar conseguir un mensaje del bufferOutput
			try {
				outputMsg = this.bufferOutput.get();
			} catch (InterruptedException e) {
				System.err.println("ERROR: Error al obtener un mensaje desde el buffer de salida a red.");
				e.printStackTrace();
			}
			
			// Comprobar que el mensaje es v‡lido y enviarlo.
			try {
				if (outputMsg.isValid()) {
					sendMessage(outputMsg);
				}
				else {
					System.err.println("ERROR: El mensaje saliente no es v‡lido.");
					outputMsg.showInfo();
				}
			} catch(IOException e){
				System.err.println("ERROR: Error al enviar el mensaje saliente a la red.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Funcion para codificar un mensaje en binario y enviarlo por el socket
	 * del usuario que contiene
	 * @param msg Message a enviar
	 * @throws IOException
	 */
	private void sendMessage(Message msg) throws IOException {
		DataOutputStream output;// Stream de salida del socket
		short sizeLoad = 0;		// Tama–o de la carga
		short numArgs = 0;		// Numero de argumentos del mensaje
		byte[][] argsBytes;		// Array con los argumentos en formato binario
		String[] args;			// Array con los argumentos en formato texto
		
		if (!msg.isValid()) {
			// Se supone que se ha comprobado el mensaje antes de llegar aqui,
			// pero por si acaso
			return;
		}
		
		// Obtener el DataOutputStream del usuario del paquete
		output = new DataOutputStream(msg.getUser().getSocket().getOutputStream());
		
		// Obtencion de los argumentos del mensaje
		args = msg.getArgs();
		numArgs = (short) args.length;
		
		// Inicializar el array de argumentos binarios
		argsBytes = new byte[args.length][];
		
		sizeLoad += 2; // Dos bytes de carga por el numero de argumentos
		
		// Codificar los argumentos
		for (int n = 0; n < args.length; n++) {
			argsBytes[n] = args[n].getBytes("UTF-8");
			sizeLoad += (2+argsBytes[n].length);
		}
		
		// Escritura en el Stream del usuario en concreto
		output.write(msg.getPacket());
		output.write(msg.getType());
		output.writeShort(sizeLoad);
		
		if (sizeLoad > 0) {
			// Si hay argumentos, escribir el numero
			output.writeShort(numArgs);
			
			for (int n = 0; n < numArgs; n++) {
				// Escribir la longitud del argumento y el argumento en si
				output.writeShort((short) argsBytes[n].length);
				output.write(argsBytes[n]);
			}
		}
	}
}