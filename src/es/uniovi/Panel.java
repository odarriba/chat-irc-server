package es.uniovi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;


/**
 * Clase de ejemplo sencillo de uso del JTree
 *
 * @author Chuidiang
 */
public class Panel
{
	private JTextField textField;
	private DefaultMutableTreeNode main;
	private DefaultTreeModel modelo;
	private HashMap<String, RoomNode> roomNodes;
	JTree tree;
	/**
	 * Ejemplo sencillo de uso de JTree
	 *
	 * @param args Argumentos de linea de comandos. Se ignoran.
	 * @wbp.parser.entryPoint
	 */
	public  void build()
	{
		// Construccion del arbol
		roomNodes=new HashMap<String, RoomNode>();
		main = new DefaultMutableTreeNode("Salas");
		modelo = new DefaultTreeModel(main);
		tree = new JTree(modelo);
		//Comprobamos de donde se esta compilando
		String path = null;
		try {
			path = new java.io.File(".").getCanonicalPath();//obtenemos el path actual
		} catch (IOException e) {
			System.err.println("Error al obtener el path actual");
			e.printStackTrace();
		}
		//Si estamos dentro de src
		if(path.endsWith("src"))
			path="./";
		//Si no estamos dentro de src
		else
			path="./src/";

		DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
		render.setLeafIcon(new ImageIcon(path+"images/user.png"));
		render.setOpenIcon(new ImageIcon(path+"images/room.png"));
		render.setClosedIcon(new ImageIcon(path+"images/room.png"));
		tree.setCellRenderer(render);
		tree.setRootVisible(false);
		// Construccion y visualizacion de la ventana
		JFrame v = new JFrame();
		v.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel lblTitle = new JLabel("Salas y usuarios :");
		lblTitle.setFont(new Font("Dialog", Font.BOLD, 20));
		v.getContentPane().add(lblTitle, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(tree);
		v.getContentPane().add(scroll, BorderLayout.CENTER);

		v.setSize(new Dimension(250, 600));
		v.setVisible(true);
		v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	/**
	 * Introduce una nueva sala en el arbol
	 * @param room el nombre de la nueva sala
	 */
	public synchronized void newRoom(String room){

		DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(room);
		roomNodes.put(room, new RoomNode(root1));
		modelo.insertNodeInto(root1, main, 0);

		showTree();
	}
	/**
	 * Introduce un nuevo usuario en la sala indicada
	 * @param room nombre de la sala en la que se va introducir
	 * el usuario
	 * @param user nombre del usuario a introducir
	 */
	public  synchronized void newUser(String room,String user){
		roomNodes.get(room).setUser(user);
		roomNodes.get(room).getRoom().add(roomNodes.get(room).getUser(user));
		modelo.reload(main);

		showTree();
	}
	/**
	 * Elimina una sala del arbol
	 * @param room sala que queremos eliminar
	 */

	public  synchronized void delRoom(String room){

		modelo.removeNodeFromParent(roomNodes.get(room).getRoom());
		roomNodes.remove(room);
		modelo.reload(main);

		showTree();
	}
	/**
	 * Elimina a un usuario de todas las salas
	 * Si alguna queda vacia la elimina
	 * @param user nombre del usuario a quitar
	 */
	public  synchronized void delUser(String user){
		for (String key: roomNodes.keySet()){
			delUser(key, user);
		}

		showTree();

	}

	/**
	 * Elimina a un usuario de la sala indicada
	 * Si la sala queda vacia la elimina
	 * @param room nombre de la sala de la que se
	 * eliminara al usuario
	 * @param user nombre del usuario a eliminar
	 */
	public  synchronized void delUser(String room, String user){
		if(roomNodes.get(room).isUser(user)){
			roomNodes.get(room).getRoom().remove(roomNodes.get(room).getUser(user));
			modelo.reload(main);
			roomNodes.get(room).delUser(user);

		}
		if(roomNodes.get(room).isEmpty())
			delRoom(room);

		showTree();
	}
	/**
	 * Nos comprueba la existencia de una sala
	 * @param room nombre de la sala a comprobar
	 * @return nos devuelve true si existe y false si no
	 */
	public synchronized boolean isRoom(String room){
		return roomNodes.containsKey(room);
	}

	/**
	 * Expande el arbol automaticamente 
	 */

	public void showTree(){

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

}