/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.qTMC;

import java.util.ArrayList;

import it.unisa.dia.gas.jpbc.Element;

/**
 * Output parameters from commitment operations
 * 
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
public class OutputCommit {

	private Element C;

	private Element V;

	private ArrayList<Element> aux;

	/**
	 * Get the C parameter
	 */
	public Element getC() {
		return C;
	}

	/**
	 * Set the C parameter
	 */
	public void setC(Element c) {
		C = c;
	}

	/**
	 * Get the V parameter
	 */
	public Element getV() {
		return V;
	}

	/**
	 * Set the V parameter
	 * 
	 * @param v
	 */
	public void setV(Element v) {
		V = v;
	}

	/**
	 * Get the aux parameters (they depends from the operation's type)
	 */
	public ArrayList<Element> getAux() {
		return aux;
	}

	/**
	 * Set the aux parameters (they depends from the operation's type)
	 */
	public void setAux(ArrayList<Element> aux) {
		this.aux = aux;
	}

}
