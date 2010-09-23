/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

/**
 * Exception when we set the parameter q = 1 because in this case we can add
 * only one leaf (this tree is a chain).
 */
public class InvalidQParameterException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidQParameterException(String message) {
		super(message);
	}

}
