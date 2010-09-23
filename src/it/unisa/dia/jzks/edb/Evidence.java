/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.gas.jpbc.Element;

public class Evidence {

	/**
	 * Commitment C value
	 */
	private Element C;

	/**
	 * Commitment V value
	 */
	private Element V;

	/**
	 * Node opening
	 */
	private Element[] opening;

	/**
	 * Node index
	 */
	private int index;

	// TODO delete this field (used for debug purpose)
	/**
	 * Node path
	 */
	private String path;

	/**
	 * Constructor
	 * 
	 * @param c
	 *            Commitment C value
	 * @param v
	 *            Commitment V value
	 * @param opening
	 *            Node opening
	 * @param index
	 *            Node index
	 */
	public Evidence(Element c, Element v, Element[] opening, int index) {
		super();
		C = c;
		V = v;
		this.opening = opening;
		this.index = index;
	}

	/**
	 * Constructor
	 * 
	 * @param c
	 *            Commitment C value
	 * @param v
	 *            Commitment V value
	 * @param opening
	 *            Node opening
	 * @param index
	 *            Node index
	 * @param path
	 *            Node path
	 */
	public Evidence(Element c, Element v, Element[] opening, int index,
			String path) {
		super();
		C = c;
		V = v;
		this.opening = opening;
		this.index = index;
		this.path = path;
	}

	/**
	 * Set the opening values
	 * 
	 * @param opening
	 *            the opening to set
	 */
	public void setOpening(Element[] opening) {
		this.opening = opening;
	}

	/**
	 * Get the opening values
	 * 
	 * @return the opening
	 */
	public Element[] getOpening() {
		return opening;
	}

	/**
	 * Set the Commitment V value
	 * 
	 * @param v
	 *            the v to set
	 */
	public void setV(Element v) {
		V = v;
	}

	/**
	 * Get the Commitment V value
	 * 
	 * @return the v
	 */
	public Element getV() {
		return V;
	}

	/**
	 * Set the Commitment C value
	 * 
	 * @param c
	 *            the c to set
	 */
	public void setC(Element c) {
		C = c;
	}

	/**
	 * Get the Commitment V value
	 * 
	 * @return the c
	 */
	public Element getC() {
		return C;
	}

	/**
	 * Set the node index
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Get the node index
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the node path
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Get the node path
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

}
