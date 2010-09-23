/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.jzks.merkleTree.MerkleNode;
import it.unisa.dia.jzks.merkleTree.RootMerkleNode;
import it.unisa.dia.jzks.qTMC.Commitment;
import it.unisa.dia.jzks.qTMC.OutputCommit;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Zero Knowledge Set Verifier
 */
public class ZKSVerifier {

	/**
	 * Commitment information for internal nodes commitment operations
	 */
	private Commitment commitment;

	/**
	 * Commitment information for leaf nodes commitment operations
	 */
	private Commitment commitmentLeaves;

	/**
	 * Logger to print messages to console
	 */
	private Logger logger = Logger
			.getLogger("it.unisa.dia.jzks.zksqTMC.ZKSVerifier");

	/**
	 * Object needed to perform some operations
	 */
	private Utils utils;

	/**
	 * Verify the hard/soft openings of the path between the node with the key
	 * and the root to prove if a key belongs or not to the database
	 * 
	 * @param piGreek
	 *            ArrayList of hard/soft openings
	 * @param key
	 *            Key to verify
	 * @param root
	 *            The RootMerkleNode information
	 * @return The value associated to the key, null if the key doesn't belong
	 *         to the database
	 * @throws FailedZKSVerifyException
	 *             If the verify fails
	 * @throws KeyMismatchZKSVerifyException
	 *             The proof is not valid for the key
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not found
	 */
	public Object verifier(PiGreek piGreek, String key, RootMerkleNode root)
			throws FailedZKSVerifyException, KeyMismatchZKSVerifyException,
			NoSuchAlgorithmException {

		logger.info("Verifing " + key + " => " + piGreek.getValue() + " - "
				+ piGreek.size());

		this.commitment = root.getCommInfo().getCommitment();
		utils = new Utils(root.getHashAlgo());

		this.commitmentLeaves = root.getCommInfo().getCommitmentLeaves();

		if (!piGreek.getKey().equals(key)) {
			logger.severe("There is something wrong (key)!");
			throw new KeyMismatchZKSVerifyException(
					"The proof is not valid for this key");
		}

		boolean found = piGreek.isFound();
		ArrayList<Element> commitmentKeys = root.getPk();
		ArrayList<Element> commitmentKeysLeaves = root.getPkLeaves();

		// Re-initialize gp (ElementPowPreProcessing) not saved in xml
		try {
			commitmentLeaves.getGp().equals(null);
		} catch (NullPointerException e) {
			commitmentLeaves.setGp(commitmentKeysLeaves.get(0).pow());
		}
		try {
			commitment.getGp().equals(null);
		} catch (NullPointerException e) {
			commitment.setGp(commitmentKeys.get(0).pow());
		}

		ArrayList<Element> message = new ArrayList<Element>();

		// TODO
		message.add(commitmentLeaves.getZr().newZeroElement());

		Evidence evidence = piGreek.remove(0);

		boolean f = false;
		// LEAF
		if (found) {
			byte[] valueHash = utils.makeHashValue(piGreek.getValue());
			logger.finer("HASH " + new BigInteger(valueHash).toString(2)
					+ evidence.getC());

			message.add(commitmentLeaves.getZr().newElement(
					new BigInteger(valueHash)));

			if (commitmentLeaves.qHVer(commitmentKeysLeaves, message.get(1), 1,
					evidence.getC(), evidence.getV(), evidence.getOpening()))
				f = true;

		} else {
			logger.finer("HASH " + BigInteger.ZERO.toString(2)
					+ evidence.getC());
			message.add(commitmentLeaves.getZr().newZeroElement());

			if (commitmentLeaves.qSVer(commitmentKeysLeaves, message.get(1), 1,
					evidence.getC(), evidence.getV(), evidence.getOpening()[0]))
				f = true;
		}

		if (!f) {
			logger.severe("There is something wrong (leaf)!");
			throw new FailedZKSVerifyException("The proof is not valid");
		}

		while (true) {

			MerkleNode child = new MerkleNode();
			OutputCommit oc = new OutputCommit();
			oc.setC(evidence.getC());
			oc.setV(evidence.getV());
			child.setCommitment(oc);

			evidence = piGreek.remove(0);

			byte[] valueHash = utils.internalNodeHash(child);

			Element mi = commitment.getZr().newElement(
					new BigInteger(valueHash));

			f = false;
			if (found) {
				if (commitment
						.qHVer(commitmentKeys, mi, evidence.getIndex(),
								evidence.getC(), evidence.getV(), evidence
										.getOpening()))
					f = true;

			} else {
				if (commitment.qSVer(commitmentKeys, mi, evidence.getIndex(),
						evidence.getC(), evidence.getV(),
						evidence.getOpening()[0]))
					f = true;
			}
			if (!f) {
				logger.severe("There is something wrong! " + piGreek.size());
				throw new FailedZKSVerifyException("The proof is not valid");
			}
			if (piGreek.size() == 1)
				break;
		}

		// ROOT verifing
		MerkleNode child = new MerkleNode();
		OutputCommit oc = new OutputCommit();
		oc.setC(evidence.getC());
		oc.setV(evidence.getV());
		child.setCommitment(oc);

		evidence = piGreek.remove(0);

		byte[] valueHash = utils.internalNodeHash(child);

		Element mi = commitment.getZr().newElement(new BigInteger(valueHash));

		f = false;
		if (found) {
			if (commitment.qHVer(commitmentKeys, mi, evidence.getIndex(), root
					.getCommitment().getC(), root.getCommitment().getV(),
					evidence.getOpening()))
				f = true;

		} else {
			if (commitment.qSVer(commitmentKeys, mi, evidence.getIndex(), root
					.getCommitment().getC(), root.getCommitment().getV(),
					evidence.getOpening()[0]))
				f = true;
		}
		if (!f) {
			logger.severe(" There is something wrong! ");
			throw new FailedZKSVerifyException("The proof is not valid");
		}

		logger.info("Verify OK");
		return piGreek.getValue();
	}
}
