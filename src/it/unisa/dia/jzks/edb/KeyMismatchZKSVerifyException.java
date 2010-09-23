/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

/**
 * The proof is not valid for the key
 */
public class KeyMismatchZKSVerifyException extends Exception {

	private static final long serialVersionUID = 1L;

	public KeyMismatchZKSVerifyException(String arg0) {
		super(arg0);
	}
}
