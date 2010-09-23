/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.field.generic.GenericElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveField;
import it.unisa.dia.jzks.edb.CommitmentInformations;
import it.unisa.dia.jzks.edb.Utils;
import it.unisa.dia.jzks.qTMC.OutputCommit;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppReader;

/**
 * Root node
 */
@XStreamAlias("root")
public class RootMerkleNode extends InternalMerkleNode {

	/**
	 * Internal nodes Public key
	 */
	@XStreamAlias("iPK")
	private ArrayList<Element> pk;

	/**
	 * Leaves Public key
	 */
	@XStreamAlias("lPK")
	private ArrayList<Element> pkLeaves;

	/**
	 * Pairing object (with fields G and Z)
	 */
	@XStreamAlias("CI")
	private CommitmentInformations commInfo;

	/**
	 * Type of hashing algorithm
	 */
	@XStreamAlias("hashAlgorithm")
	private String hashAlgo = null;

	/**
	 * 
	 * @return the hashing algorithm used
	 */
	public String getHashAlgo() {
		return hashAlgo;
	}

	/**
	 * 
	 * @param hashAlgo
	 *            the hashing algorithm to use
	 */
	public void setHashAlgo(String hashAlgo) {
		this.hashAlgo = hashAlgo;
	}

	/**
	 * @return the pk
	 */
	public ArrayList<Element> getPk() {
		return pk;
	}

	/**
	 * @param pk
	 *            the pk to set
	 */
	public void setPk(ArrayList<Element> pk) {
		this.pk = pk;
	}

	/**
	 * @param pkLeaves
	 *            the pkLeaves to set
	 */
	public void setPkLeaves(ArrayList<Element> pkLeaves) {
		this.pkLeaves = pkLeaves;
	}

	/**
	 * @return the pkLeaves
	 */
	public ArrayList<Element> getPkLeaves() {
		return pkLeaves;
	}

	/**
	 * Store Root Merkle Tree information into a file XML
	 * 
	 * @param path
	 *            The path where the data will be stored
	 * @param ENCODING
	 */
	public void saveToXML(String path, String ENCODING) {
		XStream xstream = getXStream();

		PrintWriter writer = Utils
				.getPrintWriter(path, ENCODING, xstream, this);
		writer.flush();
		writer.close();

	}

	/**
	 * Set the elements of the object XStream that are used into the file xml to
	 * save the Commitment Merkle Tree Information
	 * 
	 * @return XStream object
	 */
	private static XStream getXStream() {
		XStream xstream = new XStream(new DomDriver());

		xstream.processAnnotations(RootMerkleNode.class);

		xstream.setMode(XStream.ID_REFERENCES);

		xstream.alias("NE", NaiveElement.class);
		xstream.alias("CF", CurveField.class);
		xstream.alias("CE", CurveElement.class);
		xstream.alias("NF", NaiveField.class);
		xstream.alias("GE", GenericElement.class);

		xstream.omitField(MerkleNode.class, "index");
		xstream.omitField(MerkleNode.class, "path");
		xstream.omitField(OutputCommit.class, "aux");
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
	public static RootMerkleNode loadFromXML(String path)
			throws FileNotFoundException {
		XStream xstream = getXStream();

		XppReader xmlReader = new XppReader(new FileReader(path));

		RootMerkleNode root = (RootMerkleNode) xstream.unmarshal(xmlReader);
		return root;

	}

	/**
	 * @param commInfo
	 *            the commInfo to set
	 */
	public void setCommInfo(CommitmentInformations commInfo) {
		this.commInfo = commInfo;
	}

	/**
	 * @return the commInfo
	 */
	public CommitmentInformations getCommInfo() {
		return commInfo;
	}

}
