package es.uniovi;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Clase para manejar los mensajes entre los hilos de entrada/salida de red y los diferentes
 * hilos de procesamiento del servidor
 */
public class BufferMessages {
	private ArrayBlockingQueue<Message> buffer;
	private static final int DEFAULT_SIZE = 200;
	
	/**
	 * Constructor por defecto que aplica el tama–o DEFAULT_SIZE
	 */
	public BufferMessages() {
		this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
	}
	
	/**
	 * Constructor especificando el tama–o que, si es menor que 1, aplica
	 * el valor de DEFAULT_SIZE
	 * @param size Tama–o del buffer
	 */
	public BufferMessages(Integer size) {
		if (size <= 0) {
			this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
		} else {
			this.buffer = new ArrayBlockingQueue<Message>(size);
		}
	}

	/**
	 * Obtener un mensaje del buffer
	 * @return Message El mensaje obtenido
	 * @throws InterruptedException
	 */
	public Message get() throws InterruptedException {
		return this.buffer.take();
	}

	/**
	 * Introducir un mensaje en el buffer
	 * @param mensaje El objeto Message a introducir
	 * @throws InterruptedException
	 */
	public void put(Message mensaje) throws InterruptedException {
		this.buffer.put(mensaje);
	}
	
	/**
	 * Obtener el numero de elementos en el buffer
	 * @return Integer El numero de elementos en el buffer
	 */
	public Integer numElements() {
		return this.buffer.size();
	}

	/**
	 * Saber si el buffer esta vacio o no
	 * @return Boolean Si el buffer esta vacio o no
	 */
	public boolean empty() {
		return (this.numElements() == 0);
	}

}
