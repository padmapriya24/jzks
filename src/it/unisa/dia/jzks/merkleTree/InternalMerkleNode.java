/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This object represents an internal node of the tree
 */
@XStreamAlias("internalMerkleNode")
public class InternalMerkleNode extends MerkleNode {

	/**
	 * Hard or soft commitment
	 */
	@XStreamAsAttribute
	private int flag = 0;

	/**
	 * Get the flag value
	 * 
	 * @return the flag
	 */
	public int getFlag() {
		return flag;
	}

	/**
	 * Random seed to setup a pseudorandom generator required to create the
	 * paths from frontiers nodes to leaves don't belong to the tree
	 */
	@XStreamAlias("seed")
	private byte[] seed;

	/**
	 * Set the flag field
	 * 
	 * @param flag
	 *            the flag to set
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * @param seed
	 *            the seed to set
	 */
	public void setSeed(byte[] seed) {
		this.seed = seed;
	}

	/**
	 * @return the seed
	 */
	public byte[] getSeed() {
		return seed;
	}

}
