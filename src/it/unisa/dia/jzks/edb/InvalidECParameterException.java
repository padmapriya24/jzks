/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * Elliptic Curve parameters not valid
 */
public class InvalidECParameterException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidECParameterException(String message) {
		super(message);
	}

}
