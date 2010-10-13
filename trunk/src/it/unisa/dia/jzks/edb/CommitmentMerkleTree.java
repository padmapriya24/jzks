/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.jzks.merkleTree.ExternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.InternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.InvalidExternalMerkleNodeException;
import it.unisa.dia.jzks.merkleTree.InvalidQParameterException;
import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;
import it.unisa.dia.jzks.merkleTree.MalformedTreeException;
import it.unisa.dia.jzks.merkleTree.MerkleNode;
import it.unisa.dia.jzks.merkleTree.RootMerkleNode;
import it.unisa.dia.jzks.qTMC.Commitment;
import it.unisa.dia.jzks.qTMC.CommitmentKeys;
import it.unisa.dia.jzks.qTMC.LibertYung_qTMC;
import it.unisa.dia.jzks.qTMC.OutputCommit;
import it.unisa.dia.lasd.position.Position;
import it.unisa.dia.lasd.tree.TreePosition;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Build a commitment Merkle Tree from a database
 */
public class CommitmentMerkleTree {

	/**
	 * Default q value
	 */
	private static final int DEFAULT_Q = 8;

	/**
	 * Certainty parameter to check if a BigInteger is probable prime
	 */
	private static final int CERTAINTY = 16;

	/**
	 * Merkle Tree
	 */
	private LinkedMerkleTree tree;

	/**
	 * Commitment information for internal nodes commitment operations
	 */
	private Commitment commitment;

	/**
	 * Commitment information for leaf nodes commitment operations
	 */
	private Commitment commitmentLeaves;

	/**
	 * All commitment informations needed by users
	 */
	private CommitmentInformations commitmentInformations;

	/**
	 * Commitments keys for internal nodes commitment operations
	 */
	private CommitmentKeys commitmentKeys;

	/**
	 * Commitment keys for leaf nodes commitment operations
	 */
	private CommitmentKeys commitmentKeysLeaves;

	/**
	 * Logger to print messages to console
	 */
	private Logger logger = Logger
			.getLogger("it.unisa.dia.jzks.zksqTMC.CommitmentMerkleTree");

	/**
	 * Object needed to perform some operations
	 */
	private Utils utils;

	/**
	 * Pseudorandom generator
	 */
	private SecureRandom random;

	/**
	 * Constructor
	 * 
	 * @param ECParameters
	 *            EC for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(String ECParameters, int q, String hashAlgo)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {

		CurveParams curveParams;
		try {
			curveParams = new CurveParams().load(ECParameters);
		} catch (IllegalArgumentException e) {
			throw new InvalidECParameterException("No valid resource found");
		}

		init(curveParams, q, 0, hashAlgo);

	}

	/**
	 * Constructor
	 * 
	 * @param rBits
	 *            EC r parameter for internal operations
	 * @param qBits
	 *            EC q parameter for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(int rBits, int qBits, int q, String hashAlgo)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {

		TypeACurveGeneratorSafe curveGenerator = new TypeACurveGeneratorSafe(
				rBits, qBits);

		CurveParams curveParams = new CurveParams();
		Map<String, String> parameters = curveGenerator.generate();

		curveParams.putAll(parameters);

		init(curveParams, q, 0, hashAlgo);
	}

	/**
	 * Constructor
	 * 
	 * @param curveParams
	 *            Elliptic Curve parameters
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(CurveParams curveParams, int q, String hashAlgo)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {

		init(curveParams, q, 0, hashAlgo);

	}

	/**
	 * Constructor
	 * 
	 * @param curveParams
	 *            Elliptic Curve parameters
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(CurveParams curveParams, int q)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {

		init(curveParams, q, 0, Utils.DEFAULT_HASH_ALGO);

	}

	/**
	 * Constructor
	 * 
	 * @param rBits
	 *            EC r parameter for internal operations
	 * @param qBits
	 *            EC q parameter for internal operations
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(int rBits, int qBits)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(rBits, qBits, DEFAULT_Q, Utils.DEFAULT_HASH_ALGO);
	}

	/**
	 * Constructor
	 * 
	 * @param rBits
	 *            EC r parameter for internal operations
	 * @param qBits
	 *            EC q parameter for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(int rBits, int qBits, int q)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(rBits, qBits, q, Utils.DEFAULT_HASH_ALGO);
	}

	/**
	 * Constructor
	 * 
	 * @param ECParameters
	 *            ECParameters EC for internal operations
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(String ECParameters)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(ECParameters, DEFAULT_Q);
	}

	/**
	 * Constructor
	 * 
	 * @param ECParameters
	 *            ECParameters EC for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(String ECParameters, int q)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(ECParameters, q, Utils.DEFAULT_HASH_ALGO);
	}

	/**
	 * Constructor
	 * 
	 * @param ECParameters
	 *            EC for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param height
	 *            Tree height
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(String ECParameters, int q, int height,
			String hashAlgo) throws InvalidQParameterException,
			InvalidECParameterException, NoSuchAlgorithmException,
			ParameterValueMismatchException, SecurityParameterNotSatisfiedException {

		CurveParams curveParams;
		try {
			curveParams = new CurveParams().load(ECParameters);
		} catch (IllegalArgumentException e) {
			throw new InvalidECParameterException("No valid resource found");
		}

		logger.warning("Arbitrary tree height not yet implemented");
		init(curveParams, q, 0, hashAlgo);

	}

	/**
	 * Constructor
	 * 
	 * @param rBits
	 *            EC r parameter for internal operations
	 * @param qBits
	 *            EC q parameter for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param height
	 *            Tree height
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(int rBits, int qBits, int q, int height,
			String hashAlgo) throws InvalidQParameterException,
			InvalidECParameterException, NoSuchAlgorithmException,
			ParameterValueMismatchException, SecurityParameterNotSatisfiedException {

		TypeACurveGeneratorSafe curveGenerator = new TypeACurveGeneratorSafe(
				rBits, qBits);

		CurveParams curveParams = new CurveParams();
		Map<String, String> parameters = curveGenerator.generate();

		curveParams.putAll(parameters);

		logger.warning("Arbitrary tree height not yet implemented");
		init(curveParams, q, 0, hashAlgo);
	}

	/**
	 * Constructor
	 * 
	 * @param curveParams
	 *            Elliptic Curve parameters
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param height
	 *            Tree height
	 * @param hashAlgo
	 *            Hashing algorithm type
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(CurveParams curveParams, int q, int height,
			String hashAlgo) throws InvalidQParameterException,
			InvalidECParameterException, NoSuchAlgorithmException,
			ParameterValueMismatchException, SecurityParameterNotSatisfiedException {

		init(curveParams, q, height, hashAlgo);

	}

	/**
	 * Constructor
	 * 
	 * @param rBits
	 *            EC r parameter for internal operations
	 * @param qBits
	 *            EC q parameter for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param height
	 *            Tree height
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(int rBits, int qBits, int q, int height)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(rBits, qBits, q, height, Utils.DEFAULT_HASH_ALGO);
	}

	/**
	 * Constructor
	 * 
	 * @param ECParameters
	 *            ECParameters EC for internal operations
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param height
	 *            Tree height
	 * @throws InvalidQParameterException
	 *             Invalid q parameter
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	public CommitmentMerkleTree(String ECParameters, int q, int height)
			throws InvalidQParameterException, InvalidECParameterException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException {
		this(ECParameters, q, height, Utils.DEFAULT_HASH_ALGO);
	}

	/**
	 * Common constructors operations
	 * 
	 * @param curveParams
	 *            EC parameters
	 * @param q
	 *            q parameter for qMercurial Commitments
	 * @param hashAlgo
	 *            Hashing Algorithm type
	 * 
	 * @throws InvalidQParameterException
	 *             Invalid q parameter for qMercurial Commitments
	 * @throws InvalidECParameterException
	 *             Invalid elliptic curve parameters
	 * @throws ParameterValueMismatchException
	 *             The digest length and the q value are not compatible
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 * @throws SecurityParameterNotSatisfiedException
	 *             The depth of the tree does not satisfy the security parameter
	 */
	private void init(CurveParams curveParams, int q, int height,
			String hashAlgo) throws InvalidQParameterException,
			InvalidECParameterException, ParameterValueMismatchException,
			NoSuchAlgorithmException, SecurityParameterNotSatisfiedException {

		// Checks on r parameter
		BigInteger r = new BigInteger(curveParams.get("r"));
		if (!r.isProbablePrime(CERTAINTY))
			throw new InvalidECParameterException("EC parameter r is not prime");

		BigInteger p = (r.subtract(BigInteger.ONE)).divide(BigInteger
				.valueOf(2));
		if (!p.isProbablePrime(CERTAINTY))
			logger.warning("EC parameter r is not a safe prime");

		Pairing pairing = PairingFactory.getPairing(curveParams);

		Field G1 = pairing.getG1();
		Field Zr = pairing.getZr();

		tree = new LinkedMerkleTree();
		tree.setQ(q);

		int bitNode = 0;

		// the r bit lenght is usually different from exp2, there are no solinas
		// prime also safe prime with this bit length
		// int lambda = r.bitLength();
		int lambda = Integer.parseInt(curveParams.get("exp2"));

		utils = new Utils(hashAlgo);

		// check if the height is to be calculated
		if (height == 0) {
			bitNode = ((Double) (Math.log(tree.getQ()) / Math.log(2)))
					.intValue();
			if ((utils.getDigestLength() % bitNode) != 0)
				throw new ParameterValueMismatchException(
						"The digest length and the q value are not compatible");
		} else {
			if ((utils.getDigestLength() % height) != 0)
				throw new ParameterValueMismatchException(
						"The digest lenght and the tree height are not compatible"
								+ lambda + height);
			bitNode = lambda / height;
		}

		// check hash length and q value
		if ((utils.getDigestLength() % bitNode) != 0)
			throw new ParameterValueMismatchException(
					"The digest length and the tree height are not compatible: "
							+ bitNode);
		if (utils.getDigestLength() < lambda)
			throw new SecurityParameterNotSatisfiedException(
					"The digest length does not satisfy the security parameter: "
							+ lambda);
		else
			lambda = utils.getDigestLength();

		tree.setLambda(lambda);
		tree.setBitNode(bitNode);

		commitment = new Commitment(G1, Zr, pairing);
		commitmentKeys = commitment.qKeygen(tree.getQ());
		commitmentLeaves = new Commitment(G1, Zr, pairing);
		commitmentKeysLeaves = commitmentLeaves.qKeygen(1);
		commitmentInformations = new CommitmentInformations();
		commitmentInformations.setCommitment(commitment);
		commitmentInformations.setCommitmentLeaves(commitmentLeaves);

		random = utils.getNewRandomGenerator(Utils.PR_ALGO);
		tree.setBaseSeed(random.generateSeed(16));

	}

	/**
	 * Insert standard commitments of database's data into the leaves
	 * 
	 * @param database
	 *            Hashtable with the data to insert into the tree, the key is an
	 *            hash value
	 * @return true if it's all right, false if some errors occurred
	 */
	public boolean populateTreeLeaves(Hashtable<String, Object> database) {

		Enumeration<String> en;

		try {
			en = database.keys();
		} catch (NullPointerException e) {
			logger.severe("Database is null");
			return false;
		}

		ArrayList<Element> message = new ArrayList<Element>();

		while (en.hasMoreElements()) {
			String key = en.nextElement();
			ExternalMerkleNode node = new ExternalMerkleNode();

			byte[] valueHash = utils.makeHashValue(database.get(key));
			byte[] keyHash = utils.makeHashValue(key);

			logger.info("Inserting " + key);

			message.clear();

			// TODO
			message.add(commitmentLeaves.getZr().newOneElement());
			message.add(commitmentLeaves.getZr().newElement(
					new BigInteger(valueHash)));

			OutputCommit outputCommit = commitmentLeaves.qHCom(
					commitmentKeysLeaves.getPk(), message);

			node.setCommitment(outputCommit);

			node.setKey(key);

			try {
				tree.insert(node, keyHash, 0);
			} catch (InvalidExternalMerkleNodeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the node is not a valid ExternalMerkleNode");
				logger.log(Level.INFO, "Exception caught", e);
				return false;
			} catch (MalformedTreeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the tree is malformed");
				logger.log(Level.INFO, "Exception caught", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Insert standard commitments of database's data into the leaves
	 * 
	 * @param database
	 *            Hashtable with the data to insert into the tree
	 * @return true if it's all right, false if some errors occurred
	 */
	public boolean populateTreeLeavesTest(Hashtable<String, Object> database) {

		Enumeration<String> en;

		try {
			en = database.keys();
		} catch (NullPointerException e) {
			logger.severe("Database is null");
			return false;
		}

		while (en.hasMoreElements()) {
			String key = en.nextElement();
			ExternalMerkleNode node = new ExternalMerkleNode();

			byte[] valueHash = utils.makeHashValue(database.get(key));
			byte[] keyHash = utils.makeHashValue(key);

			logger.info("Inserting " + key);

			ArrayList<Element> message = new ArrayList<Element>();

			// TODO
			message.add(commitmentLeaves.getZr().newOneElement());
			message.add(commitmentLeaves.getZr().newElement(
					new BigInteger(valueHash)));

			OutputCommit outputCommit = commitmentLeaves.qHCom(
					commitmentKeysLeaves.getPk(), message);

			node.setCommitment(outputCommit);

			node.setKey(key);

			try {
				tree.insert(node, (String) database.get(key), 0);
			} catch (InvalidExternalMerkleNodeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the node is not a valid ExternalMerkleNode");
				logger.log(Level.INFO, "Exception caught", e);
				return false;
			} catch (MalformedTreeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the tree is malformed");
				logger.log(Level.INFO, "Exception caught", e);
				return false;
			}
		}
		return true;
	}

	/**
	 * Get the Tree
	 * 
	 * @return The LinkedMerkleTree
	 */
	public LinkedMerkleTree getTree() {
		return tree;
	}

	/**
	 * Commit the whole tree
	 * 
	 * @return All commitment informations needed by users
	 */
	public CommitmentInformations commit() {
		Iterator<Position<MerkleNode>> nodeIter = tree.postOrderInternal();
		ArrayList<Element> message = new ArrayList<Element>();
		while (nodeIter.hasNext()) {
			TreePosition<MerkleNode> nodePos = (TreePosition<MerkleNode>) nodeIter
					.next();
			logger.info("Commit " + nodePos.element().getIndex() + " - "
					+ nodePos.element().getPath());
			InternalMerkleNode node = (InternalMerkleNode) nodePos.element();
			OutputCommit outputCommit = null;

			if (tree.numberOfChildren(nodePos) > 0) {

				message.clear();

				// TODO
				message.add(commitment.getZr().newZeroElement());

				for (int i = 1; i <= tree.getQ(); i++)
					message.add(commitment.getZr().newZeroElement());

				Iterator<Position<MerkleNode>> iterChild = nodePos
						.getChildren().iterator();
				ArrayList<Element> m = new ArrayList<Element>();
				// TODO
				m.add(commitmentLeaves.getZr().newZeroElement());
				m.add(commitmentLeaves.getZr().newZeroElement());
				while (iterChild.hasNext()) {
					Position<MerkleNode> current = iterChild.next();

					// Zero commitment in empty leaves
					if (current.element() instanceof ExternalMerkleNode) {
						ExternalMerkleNode element = (ExternalMerkleNode) current
								.element();
						if (element.getKey().equals(MerkleNode.EMPTY_KEY)) {
							logger.info("Zero commitment " + element.getPath());

							OutputCommit oc = commitmentLeaves.qHCom(
									commitmentKeysLeaves.getPk(), m);

							ExternalMerkleNode newElement = new ExternalMerkleNode();
							newElement.setIndex(element.getIndex());
							newElement.setKey(element.getKey());
							newElement.setPath(element.getPath());
							newElement.setCommitment(oc);
							tree.replace(current, newElement);
						}
					}

					byte[] valueHash = utils
							.internalNodeHash(current.element());
					message.set(current.element().getIndex(), commitment
							.getZr().newElement(new BigInteger(valueHash)));
				}

				outputCommit = commitment
						.qHCom(commitmentKeys.getPk(), message);
				node.setFlag(LibertYung_qTMC.HARD_COMMITMENT);

			} else {
				outputCommit = commitment.qSCom(commitmentKeys.getPk());
				node.setFlag(LibertYung_qTMC.SOFT_COMMITMENT);
				byte[] seed = new byte[16];
				random.nextBytes(seed);
				node.setSeed(seed);
			}

			node.setCommitment(outputCommit);

			if (tree.isRoot(nodePos)) {
				RootMerkleNode root = (RootMerkleNode) node;
				root.setPk(commitmentKeys.getPk());
				root.setPkLeaves(commitmentKeysLeaves.getPk());
				root.setCommInfo(commitmentInformations);
				root.setHashAlgo(utils.getHashAlgo());
			}

			tree.replace(nodePos, node);

		}

		return commitmentInformations;
	}

	/**
	 * Print to console the whole tree structure (Testing purpose)
	 */
	public void printTree() {
		if (logger.getLevel().intValue() <= Level.FINE.intValue())
			tree.printTree();
	}

	/**
	 * Save the the tree in a xml file
	 * 
	 * @param path
	 *            Xml file path
	 * @param ENCODING
	 *            Encoding to use
	 */
	public void saveTreeToXML(String path, String ENCODING) {
		tree.saveToXML(path, ENCODING);
	}

}
