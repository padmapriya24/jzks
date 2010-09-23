/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.qTMC;

/**
 * The message to open is different from the message committed
 */
public class MessageMismatchException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * @param message
	 */
	public MessageMismatchException(String message) {
		super(message);
	}

}
