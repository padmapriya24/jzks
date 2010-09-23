/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.utils;

import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;

import java.awt.Container;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

/**
 * Create an applet to show a graphical representation of the tree
 */
public class ViewTree {

	public static void main(String[] args) throws FileNotFoundException {
		JFrame frame = new JFrame();
		Container content = frame.getContentPane();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		LinkedMerkleTree lm = LinkedMerkleTree.loadFromXML(args[0]);
		content.add(new MerkleTree2D(lm));
		frame.pack();
		frame.setVisible(true);
	}

}
