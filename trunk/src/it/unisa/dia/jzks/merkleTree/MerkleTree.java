/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 * 
 * Each key x is assigned to a leaf of a q-ary tree of height h 
 * (and can be seen as the label of the leaf, expressed in q-ary encoding).
 * For each key x such that D(x)=PERPENDICULAR, the corresponding leaf 
 * contains a standard hard mercurial commitment to a hash value of D(x).
 * Into Merkle Tree, we mapped our DB (also add element bye the pair (x,y) 
 * where x is the Key and y=D(x) is the respective value. 
 */
package it.unisa.dia.jzks.merkleTree;

import it.unisa.dia.lasd.position.Position;

/**
 * Merkle Tree
 */
public interface MerkleTree {

	/**
	 * Insert a new node in the Merkle Tree
	 * 
	 * @param node
	 *            New node to insert
	 * @param key
	 *            New node key
	 * @param cursor
	 *            Key index
	 * @return New node position
	 * @throws InvalidExternalMerkleNodeException
	 *             New node not valid
	 * @throws MalformedTreeException
	 *             Tree not valid
	 */
	public Position<MerkleNode> insert(ExternalMerkleNode node, byte[] key,
			int cursor) throws InvalidExternalMerkleNodeException,
			MalformedTreeException;

}