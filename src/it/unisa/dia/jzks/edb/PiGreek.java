/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.field.generic.GenericElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveField;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppReader;

/**
 * Opening result
 */
public class PiGreek extends ArrayList<Evidence> {

	private static final long serialVersionUID = 1L;

	/**
	 * Node found or not
	 */
	private boolean found;

	/**
	 * Node value
	 */
	private Object value;

	/**
	 * Node key
	 */
	private String key;

	/**
	 * Set the node value
	 * 
	 * @param value
	 *            the value to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * Get the node value
	 * 
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Set the found flag
	 * 
	 * @param found
	 *            the found to set
	 */
	public void setFound(boolean found) {
		this.found = found;
	}

	/**
	 * Get the found flag
	 * 
	 * @return the found
	 */
	public boolean isFound() {
		return found;
	}

	/**
	 * Set the node key
	 * 
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the node key
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the elements of the object XStream that are used into the file xml to
	 * save the Commitment Informations
	 * 
	 * @return XStream object
	 */
	private static XStream getXStream() {
		XStream xstream = new XStream(new DomDriver());

		xstream.processAnnotations(PiGreek.class);

		xstream.setMode(XStream.ID_REFERENCES);

		xstream.alias("NE", NaiveElement.class);
		xstream.alias("CF", CurveField.class);
		xstream.alias("CE", CurveElement.class);
		xstream.alias("NF", NaiveField.class);
		xstream.alias("GE", GenericElement.class);

		xstream.omitField(NaiveElement.class, "secureRandom");
		xstream.omitField(CurveElement.class, "random");
		xstream.omitField(NaiveElement.class, "order");
		xstream.omitField(NaiveElement.class, "oddOrder");
		xstream.omitField(NaiveField.class, "nqr");

		return xstream;
	}

	/**
	 * Load the object from a XML file
	 * 
	 * @param path
	 *            The path where the data are stored
	 * @throws FileNotFoundException
	 *             The path is not valid
	 */
	public static PiGreek loadFromXML(String path) throws FileNotFoundException {
		XStream xstream = getXStream();

		XppReader xmlReader = new XppReader(new FileReader(path));

		PiGreek pg = (PiGreek) xstream.unmarshal(xmlReader);
		return pg;

	}

	/**
	 * Load the object from a XML byte array
	 * 
	 * @param xml
	 *            XML byte array
	 */
	public static PiGreek loadFromXML(byte[] xml) {
		XStream xstream = getXStream();

		PiGreek pg = (PiGreek) xstream.fromXML(new String(xml));
		return pg;

	}

	/**
	 * Store all Commitment informations into a file XML
	 * 
	 * @param path
	 *            The path where the data will be stored
	 * @param ENCODING
	 *            Encoding to write the file
	 */
	public void saveToXML(String path, String ENCODING) {
		XStream xstream = getXStream();

		PrintWriter writer = Utils
				.getPrintWriter(path, ENCODING, xstream, this);
		writer.flush();
		writer.close();
	}

}
