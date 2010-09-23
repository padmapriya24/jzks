/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

/**
 * Exception when we add a new node into the tree that isn't an External Node
 */
public class InvalidExternalMerkleNodeException extends Exception {

	private static final long serialVersionUID = 1L;

	public InvalidExternalMerkleNodeException(String message) {
		super(message);
	}

}
