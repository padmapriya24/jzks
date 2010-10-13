/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * The depth of the tree does not satisfy the security parameter
 */
public class SecurityParameterNotSatisfiedException extends Exception {

	private static final long serialVersionUID = 1L;

	public SecurityParameterNotSatisfiedException(String message) {
		super(message);
	}

}
