/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.qTMC;

import java.util.ArrayList;
import it.unisa.dia.gas.jpbc.Element;

/**
 * Keys Object. It contains the public and the trapdoor key.
 */
public class CommitmentKeys {

	/**
	 * Public key
	 */
	private ArrayList<Element> pk;

	/**
	 * Trapdoor key
	 */
	private Element tk;

	/**
	 * Get the trapdoor key
	 */
	public Element getTk() {
		return tk.duplicate();
	}

	/**
	 * Set the trapdoor key
	 * 
	 * @param tk
	 *            trapdoor key
	 */
	public void setTk(Element tk) {
		this.tk = tk;
	}

	/**
	 * Get the public key
	 */
	public ArrayList<Element> getPk() {
		return pk;
	}

	/**
	 * Set the public key
	 * 
	 * @param pk
	 *            public key
	 */
	public void setPk(ArrayList<Element> pk) {
		this.pk = pk;
	}
}
