/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.field.generic.GenericElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveField;
import it.unisa.dia.jzks.merkleTree.ExternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.InternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.InvalidExternalMerkleNodeException;
import it.unisa.dia.jzks.merkleTree.InvalidQParameterException;
import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;
import it.unisa.dia.jzks.merkleTree.MalformedTreeException;
import it.unisa.dia.jzks.merkleTree.MerkleNode;
import it.unisa.dia.jzks.merkleTree.RootMerkleNode;
import it.unisa.dia.jzks.qTMC.Commitment;
import it.unisa.dia.jzks.qTMC.LibertYung_qTMC;
import it.unisa.dia.jzks.qTMC.MessageMismatchException;
import it.unisa.dia.jzks.qTMC.OutputCommit;
import it.unisa.dia.lasd.position.Position;
import it.unisa.dia.lasd.tree.TreePosition;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XppReader;

/**
 * Zero Knowledge Set
 */
public class ZeroKnowledgeSet {

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
	 * Source database
	 */
	private Hashtable<String, Object> database;

	/**
	 * Commitments keys for internal nodes commitment operations
	 */
	private ArrayList<Element> commitmentKeys;

	/**
	 * Commitment keys for leaf nodes commitment operations
	 */
	private ArrayList<Element> commitmentKeysLeaves;

	/**
	 * Logger to print messages to console
	 */
	private Logger logger = Logger
			.getLogger("it.unisa.dia.jzks.zksqTMC.ZeroKnowledgeSet");

	/**
	 * Object needed to perform some operations
	 */
	private Utils utils;

	/**
	 * Proofs
	 */
	private PiGreek piGreek;

	/**
	 * Constructor
	 * 
	 * @param database
	 *            Database committed
	 * @param treePath
	 *            Path where the tree is stored
	 */
	public ZeroKnowledgeSet(Hashtable<String, Object> database, String treePath) {

		this(database, readMerkleTreeFromXML(treePath));

	}

	/**
	 * Constructor
	 * 
	 * @param database
	 *            Database committed
	 * @param tree
	 *            Tree committed
	 */
	public ZeroKnowledgeSet(Hashtable<String, Object> database,
			LinkedMerkleTree tree) {

		RootMerkleNode root = ((RootMerkleNode) tree.root().element());

		this.commitment = root.getCommInfo().getCommitment();
		this.commitmentLeaves = root.getCommInfo().getCommitmentLeaves();

		this.database = database;

		this.tree = tree;

		this.commitmentKeys = root.getPk();
		this.commitmentKeysLeaves = root.getPkLeaves();

		// Re-initialize gp (ElementPowPreProcessing) not saved in xml
		this.commitmentLeaves.setGp(this.commitmentKeysLeaves.get(0).pow());
		this.commitment.setGp(this.commitmentKeys.get(0).pow());

		try {
			utils = new Utils(root.getHashAlgo());
		} catch (NoSuchAlgorithmException e) {
			logger.severe("Hash algorithm not valid");
		}

	}

	/**
	 * Print to console the whole tree structure (Testing purpose)
	 */
	public void printTree() {
		tree.printTree();
	}

	/**
	 * For same key x, the prover generates a proof of membership consisting of
	 * hard openings for commitments in nodes on the path connecting leaf x to
	 * the root
	 * 
	 * @param leaf
	 *            The position of the leaf into the Merkle tree
	 * @return ArrayList of hard openings for commitments in nodes on the path
	 *         connecting leaf x to the root
	 */
	private PiGreek proofMembership(Position<MerkleNode> leaf) {
		ExternalMerkleNode node = (ExternalMerkleNode) leaf.element();
		String key = node.getKey();

		logger.info("Proof Yes " + key);

		PiGreek piGreek = new PiGreek();

		piGreek.setValue(database.get(key));
		piGreek.setKey(key);
		piGreek.setFound(true);

		Element[] opening = new Element[2];

		byte[] valueHash = utils.makeHashValue(database.get(key));

		ArrayList<Element> pk = commitmentKeysLeaves;

		try {
			opening = commitmentLeaves.qHOpen(pk, commitmentLeaves.getZr()
					.newElement(new BigInteger(valueHash)), 1, node
					.getCommitment().getAux());
		} catch (MessageMismatchException e1) {
			logger
					.severe("The message in commitment informations and the message in the node mismatch");
			return null;
		}

		piGreek.add(new Evidence(node.getCommitment().getC(), node
				.getCommitment().getV(), opening, 1));

		Position<MerkleNode> parentNodePos = tree.parent(leaf);
		Position<MerkleNode> childNodePos = leaf;
		while (true) {
			opening = new Element[2];

			InternalMerkleNode parentNode = null;
			try {
				parentNode = (InternalMerkleNode) parentNodePos.element();
			} catch (NullPointerException e) {
				break;
			}
			int i = childNodePos.element().getIndex();

			valueHash = utils.internalNodeHash(childNodePos.element());
			pk = commitmentKeys;

			Element mi = commitment.getZr().newElement(
					new BigInteger(valueHash));

			try {
				opening = commitment.qHOpen(pk, mi, i, parentNode
						.getCommitment().getAux());
			} catch (MessageMismatchException e1) {
				logger
						.severe("The message in commitment informations and the message in the node mismatch");
				return null;
			}
			piGreek.add(new Evidence(parentNode.getCommitment().getC(),
					parentNode.getCommitment().getV(), opening, i));

			childNodePos = parentNodePos;
			parentNodePos = tree.parent(childNodePos);
		}
		return piGreek;
	}

	/**
	 * Checks if the key belong to database, generates the evidences needed to
	 * prove that and set the evidence ArrayList
	 * 
	 * @param key
	 *            Key to check
	 * @return true if key belongs to database, false otherwise
	 */
	public boolean belong(String key) {
		byte[] keyHash = utils.makeHashValue(key);
		String path = new BigInteger(keyHash).abs().toString(2);
		// path = tree.fixLen(path);
		logger.fine("PATH: " + path);

		int cursor = 0;
		Position<MerkleNode> parent = tree.root();
		int log = ((Double) (Math.log(tree.getQ()) / Math.log(2))).intValue();
		String localKey = MerkleNode.ROOT_PATH;
		while ((cursor + log) <= path.length()) {
			localKey = localKey + path.substring(cursor, cursor + log);
			Position<MerkleNode> child = tree.findChild(parent, localKey);
			if (child == null) {
				piGreek = proofNoMembership(parent, key, cursor);
				return false;
			}
			parent = child;
			cursor += log;
		}
		if (((ExternalMerkleNode) parent.element()).getKey().equals(
				MerkleNode.EMPTY_KEY)) {
			piGreek = proofNoMembership(parent, key, cursor);
			return false;
		}

		logger.info("Leaf found: " + parent.element().getPath());

		piGreek = proofMembership(parent);
		return true;

	}

	/**
	 * Checks if the key belong to database, generates the evidences needed to
	 * prove that and set the evidence ArrayList
	 * 
	 * @param key
	 *            Key to check
	 * @param path
	 *            the path of the node to find (testing purpose)
	 * @return true if key belongs to database, false otherwise
	 */
	public boolean testBelong(String key, String path) {
		piGreek = new PiGreek();
		int cursor = 0;
		Position<MerkleNode> parent = tree.root();

		int log = ((Double) (Math.log(tree.getQ()) / Math.log(2))).intValue();
		String localKey = MerkleNode.ROOT_PATH;
		while ((cursor + log) <= path.length()) {
			localKey = localKey + path.substring(cursor, cursor + log);
			Position<MerkleNode> child = tree.findChild(parent, localKey);
			if (child == null) {
				System.out.println("Frontier: " + parent.element().getPath());
				piGreek = proofNoMembership(parent, key, path, cursor);
				return false;
			}
			parent = child;
			cursor += log;
		}
		System.out.println(parent.element().getPath());
		if (((ExternalMerkleNode) parent.element()).getKey().equals(
				MerkleNode.EMPTY_KEY)) {
			piGreek = proofNoMembership(parent, key, cursor);
			return false;
		}

		logger.info("Leaf found: " + parent.element().getPath());

		piGreek = proofMembership(parent);
		return true;
	}

	/**
	 * Create a new tree, the subTree to generate proofs about elements not
	 * belonging
	 * 
	 * @param frontier
	 *            Tree frontier node
	 * @return New subtree
	 */
	private LinkedMerkleTree genSubTree(InternalMerkleNode frontier) {
		LinkedMerkleTree subTree = new LinkedMerkleTree();
		subTree.setLambda(tree.getLambda());
		try {
			subTree.setQ(tree.getQ());
		} catch (InvalidQParameterException e1) {
			logger.log(Level.SEVERE, "Exception caught", e1);
			return null;
		}

		logger.fine("SubTree Root: " + frontier.getPath());

		InternalMerkleNode newRoot = new InternalMerkleNode();
		newRoot.setCommitment(frontier.getCommitment());
		newRoot.setIndex(frontier.getIndex());
		newRoot.setPath(MerkleNode.ROOT_PATH);
		newRoot.setFlag(LibertYung_qTMC.SOFT_COMMITMENT);
		subTree.replace(subTree.root(), newRoot);
		return subTree;
	}

	/**
	 * Generate random numbers to make correct proofs about elements not
	 * belonging
	 * 
	 * @param indexes
	 *            Subtree nodes indexes in the path
	 * @param seed
	 *            Pseudorandom generator seed
	 * @return Random numbers ArrayList
	 */
	private ArrayList<BigInteger> getRandoms(ArrayList<Integer> indexes,
			byte[] seed) {

		// random generating
		SecureRandom random = utils.getNewRandomGenerator(Utils.PR_ALGO);
		random.setSeed(seed);

		// array list dei random da utilizzare per il commit dei nodi del
		// sottoalbero
		ArrayList<BigInteger> randoms = new ArrayList<BigInteger>();

		// random numbers about leaves
		for (int i = 0; i < tree.getQ(); i++) {
			randoms.add(0, BigInteger
					.valueOf(random.nextInt(Integer.MAX_VALUE)));
			randoms.add(0, BigInteger
					.valueOf(random.nextInt(Integer.MAX_VALUE)));
		}

		// random numbers about internal nodes
		while (!indexes.isEmpty()) {
			int i = indexes.remove(0);
			int j = 1;
			for (j = 1; j < i; j++) {
				random.nextInt(Integer.MAX_VALUE);
			}
			randoms.add(0, BigInteger
					.valueOf(random.nextInt(Integer.MAX_VALUE)));
			randoms.add(0, BigInteger
					.valueOf(random.nextInt(Integer.MAX_VALUE)));
			byte[] newSeed = new byte[16];
			random.nextBytes(newSeed);
			for (j = j + 1; j <= tree.getQ(); j++) {
				random.nextInt(Integer.MAX_VALUE);
				random.nextInt(Integer.MAX_VALUE);
			}
			random.setSeed(newSeed);
		}

		return randoms;
	}

	/**
	 * To provide evidence that some key x does not belong to the database, the
	 * prover first generates the missing portion of the subtree where x lies.
	 * Then, it reveals soft openings for all (hard or soft) commitments
	 * contained in nodes appearing in the path from x to the root
	 * 
	 * @param parent
	 *            Last node in the tree that belongs to the path between the
	 *            root and the node (not into the tree) containing the key
	 * @param key
	 *            Key doesn't belong to database
	 * @param cursor
	 *            Index in the string path
	 * @return ArrayList of soft openings for commitments in nodes on the path
	 *         connecting leaf x to the root
	 */
	private PiGreek proofNoMembership(Position<MerkleNode> parent, String key,
			int cursor) {

		byte[] keyHash = utils.makeHashValue(key);
		String path = new BigInteger(keyHash).abs().toString(2);
		// path = tree.fixLen(path);

		logger.info("Proof NO " + key + "=>" + path);

		return proofNoMembership(parent, key, path, cursor);
	}

	/**
	 * To provide evidence that some key x does not belong to the database, the
	 * prover first generates the missing portion of the subtree where x lies.
	 * Then, it reveals soft openings for all (hard or soft) commitments
	 * contained in nodes appearing in the path from x to the root
	 * 
	 * @param parent
	 *            Last node in the tree that belongs to the path between the
	 *            root and the node (not into the tree) containing the key
	 * @param key
	 *            Key doesn't belong to database
	 * @param path
	 *            Node path
	 * @param cursor
	 *            Index in the string path
	 * @return ArrayList of soft openings for commitments in nodes on the path
	 *         connecting leaf x to the root
	 */
	private PiGreek proofNoMembership(Position<MerkleNode> parent, String key,
			String path, int cursor) {

		PiGreek piGreek = new PiGreek();
		piGreek.setFound(false);
		piGreek.setKey(key);

		LinkedMerkleTree subTree = null;
		Position<MerkleNode> parentNodePos;
		Position<MerkleNode> childNodePos;
		Element opening[];

		ArrayList<Element> message = new ArrayList<Element>();
		// TODO
		message.add(commitmentLeaves.getZr().newZeroElement());
		message.add(commitmentLeaves.getZr().newZeroElement());

		// if we need to build the subtree
		if (cursor < path.length()) {
			logger.info("we need subtree");
			InternalMerkleNode frontier = (InternalMerkleNode) parent.element();
			subTree = genSubTree(frontier);

			// add leaves to subtree
			ExternalMerkleNode node = new ExternalMerkleNode();

			Position<MerkleNode> newLeaf = null;

			try {
				newLeaf = subTree.insert(node, path, cursor);
			} catch (InvalidExternalMerkleNodeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the node is not a valid ExternalMerkleNode");
				return null;
			} catch (MalformedTreeException e) {
				logger
						.severe("Error inserting a new leaf into the tree: the tree is malformed");
				return null;
			}

			// array list degli indici dei nodi creati (primo elemento: leaf
			// index,
			// ultimo: root child index)
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			Iterator<Position<MerkleNode>> subTreeIter = subTree
					.postOrderInternal();
			while (subTreeIter.hasNext())
				indexes.add(0, subTreeIter.next().element().getIndex());

			// randoms
			ArrayList<BigInteger> randoms = getRandoms(indexes, frontier
					.getSeed());

			// New leaves commitment
			logger.fine("Commit " + node.getIndex() + " - " + node.getPath());

			OutputCommit newLeafOutputCommit = null;

			parentNodePos = subTree.parent(newLeaf);
			Iterator<Position<MerkleNode>> newLeavesIter = subTree.children(
					parentNodePos).iterator();
			while (newLeavesIter.hasNext()) {
				Position<MerkleNode> current = newLeavesIter.next();
				ExternalMerkleNode currentNode = (ExternalMerkleNode) current
						.element();

				OutputCommit outputCommit = commitmentLeaves.qHComPr(
						commitmentKeysLeaves, message, randoms.remove(0),
						randoms.remove(0));

				currentNode.setCommitment(outputCommit);

				if (current.equals(newLeaf)) {
					newLeafOutputCommit = currentNode.getCommitment();
					currentNode.setKey(key);
				}

				subTree.replace(current, currentNode);
			}

			// Leaf opening
			opening = new Element[1];

			logger.fine("Leaf Opening " + node.getPath());

			opening[0] = commitmentLeaves.qSOpen(commitmentKeysLeaves, message
					.get(1), 1, LibertYung_qTMC.HARD_COMMITMENT,
					newLeafOutputCommit.getAux());

			piGreek.add(new Evidence(newLeafOutputCommit.getC(),
					newLeafOutputCommit.getV(), opening, 1, node.getPath()));

			// SubTree commitment
			Iterator<Position<MerkleNode>> nodeIter = subTree
					.postOrderInternal();
			while (nodeIter.hasNext()) {
				TreePosition<MerkleNode> nodePos = (TreePosition<MerkleNode>) nodeIter
						.next();

				if (subTree.isRoot(nodePos))
					break;

				logger.fine("Commit " + nodePos.element().getIndex() + " - "
						+ nodePos.element().getPath());
				InternalMerkleNode nodeEl = (InternalMerkleNode) nodePos
						.element();
				OutputCommit outputCommit = null;

				if (subTree.numberOfChildren(nodePos) > 0) {

					ArrayList<Element> messages = new ArrayList<Element>();
					// TODO
					messages.add(commitment.getZr().newZeroElement());
					for (int i = 1; i <= tree.getQ(); i++)
						messages.add(commitment.getZr().newZeroElement());

					Iterator<Position<MerkleNode>> iterChild = nodePos
							.getChildren().iterator();
					while (iterChild.hasNext()) {
						Position<MerkleNode> current = iterChild.next();

						byte[] valueHash = utils.internalNodeHash(current
								.element());
						messages.set(current.element().getIndex(), commitment
								.getZr().newElement(new BigInteger(valueHash)));
					}

					outputCommit = commitment.qHComPr(commitmentKeys, messages,
							randoms.remove(0), randoms.remove(0));
					nodeEl.setFlag(LibertYung_qTMC.HARD_COMMITMENT);

				} else {
					outputCommit = commitment.qSComPr(commitmentKeys, randoms
							.remove(0), randoms.remove(0));
					nodeEl.setFlag(LibertYung_qTMC.SOFT_COMMITMENT);
				}

				nodeEl.setCommitment(outputCommit);

				subTree.replace(nodePos, nodeEl);
			}

			// SubTree opening
			childNodePos = newLeaf;
			while (true) {
				opening = new Element[1];

				InternalMerkleNode parentNode = null;
				try {
					parentNode = (InternalMerkleNode) parentNodePos.element();
				} catch (NullPointerException e) {
					break;
				}
				int index = childNodePos.element().getIndex();

				InternalMerkleNode nodeIn = (InternalMerkleNode) parentNode;

				byte[] valueHash = utils.internalNodeHash(childNodePos
						.element());
				logger.finer("HASH " + new BigInteger(valueHash).toString(2));

				Element mi = commitment.getZr().newElement(
						new BigInteger(valueHash));

				logger.fine("SubTree Opening " + nodeIn.getPath() + "=>" + "("
						+ index + ")" + mi + " - " + nodeIn.getFlag());

				opening[0] = commitment.qSOpen(commitmentKeys, mi, index,
						nodeIn.getFlag(), nodeIn.getCommitment().getAux());

				piGreek.add(new Evidence(nodeIn.getCommitment().getC(), nodeIn
						.getCommitment().getV(), opening, index, nodeIn
						.getPath()));

				childNodePos = parentNodePos;
				parentNodePos = subTree.parent(childNodePos);
			}
		} else {
			// Leaf opening
			opening = new Element[1];

			logger.fine("Leaf Opening " + parent.element().getPath());

			opening[0] = commitmentLeaves.qSOpen(commitmentKeysLeaves, message
					.get(1), 1, LibertYung_qTMC.SOFT_COMMITMENT, parent
					.element().getCommitment().getAux());

			piGreek.add(new Evidence(parent.element().getCommitment().getC(),
					parent.element().getCommitment().getV(), opening, 1, parent
							.element().getPath()));
		}

		// Tree open
		parentNodePos = tree.parent(parent);
		childNodePos = parent;
		while (true) {
			opening = new Element[1];

			InternalMerkleNode parentNode = null;
			try {
				parentNode = (InternalMerkleNode) parentNodePos.element();
			} catch (NullPointerException e) {
				break;
			}
			int i = childNodePos.element().getIndex();

			byte[] valueHash = utils.internalNodeHash(childNodePos.element());
			logger.finer("HASH " + new BigInteger(valueHash).toString(2)
					+ childNodePos.element().getCommitment().getC());

			Element mi = commitment.getZr().newElement(
					new BigInteger(valueHash));

			logger.fine("Tree Opening " + parentNode.getPath() + "=>" + "(" + i
					+ ")" + mi);

			opening[0] = commitment.qSOpen(commitmentKeys, mi, i,
					LibertYung_qTMC.HARD_COMMITMENT, parentNode.getCommitment()
							.getAux());

			piGreek.add(new Evidence(parentNode.getCommitment().getC(),
					parentNode.getCommitment().getV(), opening, i, parentNode
							.getPath()));

			childNodePos = parentNodePos;
			parentNodePos = tree.parent(childNodePos);
		}

		return piGreek;
	}

	/**
	 * Get the ArrayList of evidences needed by verifier
	 * 
	 * @return ArrayList of hard/soft openings
	 */
	public PiGreek getPiGreek() {
		PiGreek pg = new PiGreek();
		pg.setFound(piGreek.isFound());
		pg.setValue(piGreek.getValue());
		pg.setKey(piGreek.getKey());
		Iterator<Evidence> it = piGreek.iterator();
		while (it.hasNext())
			pg.add(it.next());

		return pg;
	}

	/**
	 * Read the information into the file XML and create a CommitmentMerkleTree
	 * 
	 * @param path
	 *            The path where the data are stored
	 */
	public static LinkedMerkleTree readMerkleTreeFromXML(String path) {
		XStream xstream = getXStreamMerkleTree();
		try {
			XppReader xmlReader = new XppReader(new FileReader(path));

			return (LinkedMerkleTree) xstream.unmarshal(xmlReader);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set the elements of the object XStream that are used into the file xml to
	 * save the Commitment Merkle Tree Information
	 * 
	 * @return XStream object
	 */
	private static XStream getXStreamMerkleTree() {
		XStream xstream = new XStream(new DomDriver());

		xstream.processAnnotations(LinkedMerkleTree.class);

		xstream.setMode(XStream.XPATH_ABSOLUTE_REFERENCES);

		xstream.alias("NaiveElement", NaiveElement.class);
		xstream.alias("CurveField", CurveField.class);
		xstream.alias("CurveElement", CurveElement.class);
		xstream.alias("NaiveField", NaiveField.class);
		xstream.alias("GenericElement", GenericElement.class);

		return xstream;
	}
}
