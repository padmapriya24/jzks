/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * The proof is not valid
 */
public class FailedZKSVerifyException extends Exception {

	private static final long serialVersionUID = 1L;

	public FailedZKSVerifyException(String arg0) {
		super(arg0);
	}
}
