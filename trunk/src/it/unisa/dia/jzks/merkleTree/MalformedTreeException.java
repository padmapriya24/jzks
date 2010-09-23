/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

/**
 * Tree structure not valid
 */
public class MalformedTreeException extends Exception {

	private static final long serialVersionUID = 1L;

	public MalformedTreeException(String message) {
		super(message);
	}

}
