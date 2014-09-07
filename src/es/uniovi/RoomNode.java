package es.uniovi;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Clase que nos servira para guardar un nodo de tipo sala y 
 * los usuarios que tenga dentro 
 */

public class RoomNode{
	
	private DefaultMutableTreeNode room;
	private HashMap<String, DefaultMutableTreeNode> users;
	
	public RoomNode(DefaultMutableTreeNode room){
		this.room=room;
		this.users=new HashMap<String, DefaultMutableTreeNode>();
	}
	
	/**
	 * Establece el DefaultMutableTreeNode de la sala a la que hace
	 * referencia
	 * @param room el valor del DefaultMutableTreeNode que queremos
	 * establecer
	 */
	public synchronized void setRoom(DefaultMutableTreeNode room){
		this.room=room;
	}
	
	/**
	 * 
	 * @return Nos devuelve el DefaultMutableTreeNode correspondiente 
	 * a la sala del objeto al que hace referencia
	 */
	public synchronized DefaultMutableTreeNode getRoom(){
		return this.room;
	}
	
	/**
	 * Introducimos un usuario en la sala
	 * @param user nombre del usuario que introducimos
	 */
	public synchronized void setUser(String user){
		users.put(user, new DefaultMutableTreeNode(user));
	}
	
	/**
	 * Nos devuelve el DefaultMutableTreeNode correspondiente
	 * al nombre del usuario indicado
	 * @param user nombre del usuario que queremos
	 * @return nos devuelve el DefaultMutableTreeNode del usuario 
	 */    	
	public synchronized DefaultMutableTreeNode getUser(String user){
		return users.get(user);
	}
	
	/**
	 * Comprueba si el usuario esta en la sala
	 * @param user usuario a comprobar
	 * @return devuelve true si esta y false si no
	 */
	public synchronized boolean isUser(String user){
		return users.containsKey(user);
	}
	
	/**
	 * Elimina a un usuario de la sala
	 * @param user nombre del usuario a eliminar
	 */
	public synchronized void delUser(String user){
		users.remove(user);
	}
	
	/** 
	 * @return devuelve true si no hay ningun usuario en la sala y
	 * false si no esta vacia
	 */
	public synchronized boolean isEmpty(){
		return users.isEmpty();
	}
}
