/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * The depth of the tree does not satisfy the security parameter
 */
public class SecurityParameterNotSatisfied extends Exception {

	private static final long serialVersionUID = 1L;

	public SecurityParameterNotSatisfied(String message) {
		super(message);
	}

}
