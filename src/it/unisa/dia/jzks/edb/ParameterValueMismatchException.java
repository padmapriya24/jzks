/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * Tree or Commitment parameters value are not compatible
 */
public class ParameterValueMismatchException extends Exception {

	private static final long serialVersionUID = 1L;

	public ParameterValueMismatchException(String message) {
		super(message);
	}
}
