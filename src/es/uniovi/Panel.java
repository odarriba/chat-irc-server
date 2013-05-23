package es.uniovi;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.BorderLayout;
import javax.swing.JTextField;


/**
 * Clase de ejemplo sencillo de uso del JTree
 *
 * @author Chuidiang
 */
public class Panel
{
	static JTextField textField;
	static DefaultMutableTreeNode main;
	static DefaultTreeModel modelo;
    /**
     * Ejemplo sencillo de uso de JTree
     *
     * @param args Argumentos de linea de comandos. Se ignoran.
     */
    public static void build()
    {
        // Construccion del arbol
        main = new DefaultMutableTreeNode("Salas");
        modelo= new DefaultTreeModel(main);
        JTree tree = new JTree(modelo);

        // Construccion y visualizacion de la ventana
        JFrame v = new JFrame();
        JScrollPane scroll = new JScrollPane(tree);
        v.getContentPane().add(scroll, BorderLayout.NORTH);
        
        v.pack();
        v.setVisible(true);
        v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}