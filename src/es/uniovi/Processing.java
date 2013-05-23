package es.uniovi;

import java.io.IOException;

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
	public void run() {

		Message msg=null;

		while(this.global.isRunning()) {

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


	private void processingQUIT(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void processingWHO(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void processingLIST(Message msg) {
		// TODO Auto-generated method stub
		
	}

	private void processingNICK(Message msg) {
		// TODO Auto-generated method stub
		
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


