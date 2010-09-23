/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

import it.unisa.dia.jzks.qTMC.OutputCommit;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

/**
 * Generic node
 */
@XStreamAlias("merkleNode")
public class MerkleNode {

	/**
	 * Root index
	 */
	@XStreamOmitField
	public static final int ROOT_INDEX = 0;

	/**
	 * Root path
	 */
	@XStreamOmitField
	public static final String ROOT_PATH = "root";

	/**
	 * Root path
	 */
	@XStreamOmitField
	public static final String EMPTY_KEY = "null";

	/**
	 * Index field
	 */
	@XStreamAsAttribute
	private int index;

	/**
	 * Node value: commitment
	 */
	@XStreamAlias("commitment")
	private OutputCommit commitment;

	/**
	 * Path field
	 */
	@XStreamAsAttribute
	private String path;

	/**
	 * Get the index value
	 * 
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Set the index field
	 * 
	 * @param index
	 *            the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Get the node value (commitment)
	 * 
	 * @return the commitment
	 */
	public OutputCommit getCommitment() {
		return commitment;
	}

	/**
	 * Set the node value (commitment)
	 * 
	 * @param commitment
	 *            the commitment to set
	 */
	public void setCommitment(OutputCommit commitment) {
		this.commitment = commitment;
	}

	/**
	 * Set the path field
	 * 
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * Get the path field
	 * 
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

}
