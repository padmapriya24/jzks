/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 * 
 * 
 * External Merkle Node (leave)
 */
package it.unisa.dia.jzks.merkleTree;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This object represents a leaf of the tree
 */
@XStreamAlias("externalMerkleNode")
public class ExternalMerkleNode extends MerkleNode {

	@XStreamAsAttribute
	private String key;

	/**
	 * Set the key field
	 * 
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Get the key value
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

}
