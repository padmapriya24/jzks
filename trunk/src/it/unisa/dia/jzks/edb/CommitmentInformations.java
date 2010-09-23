/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.jzks.qTMC.Commitment;

/**
 * This object contains the informations about the commitment operation of the
 * whole Merkle Tree
 */
public class CommitmentInformations {

	/**
	 * Pairing object (with fields G and Z)
	 */
	private Commitment commitment;

	/**
	 * Pairing object (with fields G and Z)
	 */
	private Commitment commitmentLeaves;

	/**
	 * Get the Commitment object
	 * 
	 * @return the Commitment Object
	 */
	public Commitment getCommitment() {
		return commitment;
	}

	/**
	 * Set the Commitment object
	 * 
	 * @param commitment
	 *            the Commitment Object
	 */
	public void setCommitment(Commitment commitment) {
		this.commitment = commitment;
	}

	/**
	 * Set the leaves commitment informations
	 * 
	 * @param commitmentLeaves
	 *            the commitmentLeaves to set
	 */
	public void setCommitmentLeaves(Commitment commitmentLeaves) {
		this.commitmentLeaves = commitmentLeaves;
	}

	/**
	 * 
	 * Get the leaves commitment informations
	 * 
	 * @return the commitmentLeaves
	 */
	public Commitment getCommitmentLeaves() {
		return commitmentLeaves;
	}

}
