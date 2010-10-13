/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.merkleTree;

import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveElement;
import it.unisa.dia.gas.plaf.jpbc.field.curve.CurveField;
import it.unisa.dia.gas.plaf.jpbc.field.generic.GenericElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveElement;
import it.unisa.dia.gas.plaf.jpbc.field.naive.NaiveField;
import it.unisa.dia.jzks.edb.Utils;
import it.unisa.dia.jzks.qTMC.Commitment;
import it.unisa.dia.lasd.position.InvalidPositionException;
import it.unisa.dia.lasd.position.Position;
import it.unisa.dia.lasd.tree.LinkedTree;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.thoughtworks.xstream.io.xml.XppReader;

import edu.uci.ics.jung.graph.Forest;

/**
 * Merkle tree implemented as a Linked Tree
 */
@XStreamAlias("LinkedMerkleTree")
public class LinkedMerkleTree implements MerkleTree {

	/**
	 * The tree
	 */
	@XStreamAlias("tree")
	private LinkedTree<MerkleNode> tree;

	/**
	 * Logger
	 */
	@XStreamOmitField
	Logger logger = Logger
			.getLogger("it.unisa.dia.jzks.merkleTree.LinkedMerkleTree");

	/**
	 * q parameter (default 8)
	 */
	@XStreamAsAttribute
	private int q = 8;

	/**
	 * Lambda parameter (default 160)
	 */
	@XStreamAsAttribute
	private int lambda = 160;

	/**
	 * Random seed to setup a pseudorandom generator needed to create the paths
	 * from frontiers nodes to leaves don't belong to the tree
	 */
	@XStreamAlias("seed")
	private byte[] baseSeed;

	/**
	 * Number of path bits used per node (it defines tree height)
	 */
	@XStreamAsAttribute
	private int bitNode = ((Double) (Math.log(q) / Math.log(2))).intValue();

	/**
	 * Get the number of path bits used per node
	 * 
	 * @return the bitNode
	 */
	public int getBitNode() {
		return bitNode;
	}

	/**
	 * Set the number of path bits used per node
	 * 
	 * @param bitNode
	 *            the bitNode to set
	 */
	public void setBitNode(int bitNode) {
		this.bitNode = bitNode;
	}

	/**
	 * Constructor
	 */
	public LinkedMerkleTree() {
		tree = new LinkedTree<MerkleNode>();
		tree.addRoot(addRoot());
	}

	/**
	 * Add the tree root
	 * 
	 * @return New root
	 */
	private RootMerkleNode addRoot() {
		RootMerkleNode root = new RootMerkleNode();
		root.setIndex(MerkleNode.ROOT_INDEX);
		root.setPath(MerkleNode.ROOT_PATH);
		return root;
	}

	/**
	 * Create a new Internal Merkle Node
	 * 
	 * @param index
	 *            index of new internal Merkle node
	 * @param path
	 *            path of new internal Merkle node
	 * @return the new Merkle node
	 */
	public MerkleNode createNode(int index, String path) {
		InternalMerkleNode mn = new InternalMerkleNode();
		mn.setIndex(index);
		mn.setPath(path);
		return mn;
	}

	/**
	 * Get the q parameter
	 */
	public int getQ() {
		return q;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see merkleTree.MerkleTree#insert(merkleTree.ExternalMerkleNode, byte[],
	 * int)
	 */
	public Position<MerkleNode> insert(ExternalMerkleNode node, byte[] keyHash,
			int cursor) throws InvalidExternalMerkleNodeException,
			MalformedTreeException {

		String path = new BigInteger(keyHash).abs().toString(2);
		return insert(node, path, cursor);

	}

	/**
	 * Insert a new node in the Merkle Tree
	 * 
	 * @param node
	 *            New node to insert
	 * @param completePath
	 *            New node path
	 * @param cursor
	 *            Path index
	 * @return New node position
	 * @throws InvalidExternalMerkleNodeException
	 *             New node not valid
	 * @throws MalformedTreeException
	 *             Tree not valid
	 */
	public Position<MerkleNode> insert(ExternalMerkleNode node,
			String completePath, int cursor)
			throws InvalidExternalMerkleNodeException, MalformedTreeException {
		if ((node == null) || !(node instanceof ExternalMerkleNode))
			throw new InvalidExternalMerkleNodeException(
					"The node is not valid");

		logger.finest("bitNode: " + bitNode);

		Position<MerkleNode> parent = tree.root();
		StringBuffer localKey = new StringBuffer();
		localKey.append(MerkleNode.ROOT_PATH);
		localKey.append(completePath.substring(cursor, cursor + bitNode));

		String stringLocalKey;
		StringBuffer log = new StringBuffer();

		MalformedTreeException treeNotValid = new MalformedTreeException(
				"Tree not valid.");

		while ((cursor + bitNode + bitNode) <= completePath.length()) {
			stringLocalKey = localKey.toString();
			log.delete(0, log.length());
			log.append("INSERT (");
			log.append(stringLocalKey);
			log.append("): ");
			log.append(completePath.substring(0, cursor));
			log.append("(");
			log.append(completePath.substring(cursor, cursor + bitNode));
			log.append(")");
			log.append(completePath.substring(cursor + bitNode));
			logger.finer(log.toString());
			Position<MerkleNode> newParent = null;
			if (tree.numberOfChild(parent) == 0)
				newParent = addChildren(parent, stringLocalKey, bitNode);
			else if (tree.numberOfChild(parent) == q)
				newParent = findChild(parent, stringLocalKey);
			else
				throw treeNotValid;

			if (newParent == null)
				throw treeNotValid;

			parent = newParent;
			cursor += bitNode;
			localKey.append(completePath.substring(cursor, cursor + bitNode));
		}
		Position<MerkleNode> newNode = null;
		stringLocalKey = localKey.toString();
		String stringPath;
		StringBuffer path = new StringBuffer();
		if (tree.numberOfChild(parent) == 0)
			for (int i = 0; i < q; i++) {
				stringPath = indexToPath(i, path, parent.element().getPath());
				if (stringPath.equals(stringLocalKey)) {
					logger.finer("0 - Leaf Full - " + stringPath);
					node.setIndex(i + 1);
					node.setPath(stringPath);
					newNode = tree.add(parent, node);
				} else {
					logger.finer("0 - Leaf Empty - " + stringPath);
					ExternalMerkleNode empty = new ExternalMerkleNode();
					empty.setKey(MerkleNode.EMPTY_KEY);
					empty.setIndex(i + 1);
					empty.setPath(stringPath);
					tree.add(parent, empty);
				}
			}
		else if (tree.numberOfChild(parent) == q) {
			Iterator<Position<MerkleNode>> children = tree.children(parent)
					.iterator();
			int i = 0;
			while (children.hasNext()) {
				Position<MerkleNode> current = children.next();
				stringPath = indexToPath(i, path, parent.element().getPath());
				if (stringPath.equals(stringLocalKey)) {
					logger.finer("q - Leaf Replace - " + stringPath);
					node.setIndex(i + 1);
					node.setPath(stringPath);
					if (((ExternalMerkleNode) current.element()).getKey()
							.equals(MerkleNode.EMPTY_KEY))
						tree.replace(current, node);
					else
						throw new MalformedTreeException("Conflict.");
					newNode = current;
					break;
				}
				i++;
			}
		} else
			throw treeNotValid;

		return newNode;
	}

	/**
	 * Build the path String from a node index and his parent path
	 * 
	 * @param index
	 *            Node index
	 * @param path
	 *            StringBuffer to build the path
	 * @param ParPath
	 *            Parent node path
	 * @return Path string of node with index "index"
	 */
	private String indexToPath(int index, StringBuffer path, String ParPath) {
		path.delete(0, path.length());
		path.append(BigInteger.valueOf(index).abs().toString(2));
		int l = path.length();
		for (int j = 0; j < (bitNode - l); j++)
			path.insert(0, "0");
		path.insert(0, ParPath);
		return path.toString();
	}

	/**
	 * Add children to a node
	 * 
	 * @param parent
	 *            Parent node
	 * @param localKey
	 *            Key to follow to obtain the path
	 * @param log
	 *            Node key length (log_2 q)
	 * @return The new parent with new children
	 */
	private Position<MerkleNode> addChildren(Position<MerkleNode> parent,
			String localKey, int log) {
		Position<MerkleNode> newParent = null;
		StringBuffer path = new StringBuffer();
		String stringPath;

		// if we want to implement an arbitrary tree height, we need to modify
		// this method:
		// here the node path is built by the index, and we add exactly q nodes
		// when we need to create the first one
		// if the path length is different from the index length we are not able
		// to build all the possible nodes:
		// path length 8 with q=16 (path length 4) we create: 00000000,
		// 00000001, 00000010,...,00001111
		// so in this condition it's not possible to implement an arbitrary tree
		// height
		// it's needed modify the tree building process!! (e na parola!)
		for (int i = 0; i < q; i++) {
			stringPath = indexToPath(i, path, parent.element().getPath());
			InternalMerkleNode newnode = (InternalMerkleNode) createNode(i + 1,
					stringPath);
			Position<MerkleNode> newNodePos = tree.add(parent, newnode);
			if (path.toString().equals(localKey))
				newParent = newNodePos;
		}
		return newParent;
	}

	/**
	 * Find a child of a node
	 * 
	 * @param parent
	 *            Parent node
	 * @param localKey
	 *            Key to follow to obtain the path
	 * @return Child node with localkey as path
	 */
	public Position<MerkleNode> findChild(Position<MerkleNode> parent,
			String localKey) {
		Iterator<Position<MerkleNode>> iter = null;
		try {
			iter = tree.children(parent).iterator();
		} catch (InvalidPositionException e) {
			return null;
		}
		while (iter.hasNext()) {
			Position<MerkleNode> current = iter.next();
			if (current.element().getPath().equals(localKey))
				return current;
		}
		return null;
	}

	/**
	 * Print the tree nodes
	 */
	public void printTree() {
		System.out.println("------- START -----\n");
		Position<MerkleNode> root = tree.root();
		InternalMerkleNode node = (InternalMerkleNode) root.element();
		System.out.println(node.getPath() + "(" + node.getFlag() + ")");
		printTreeAux(root);
		System.out.println("------- END -------\n");
	}

	/**
	 * Print the tree nodes (aux method for recursion)
	 * 
	 * @param current
	 *            Current node to explore
	 * @param ptree
	 *            The printed tree
	 */
	private void printTreeAux(Position<MerkleNode> current) {
		Iterator<Position<MerkleNode>> iter;
		try {
			iter = tree.children(current).iterator();
		} catch (InvalidPositionException e) {
			return;
		}
		while (iter.hasNext()) {
			Position<MerkleNode> nodePos = iter.next();
			MerkleNode node = nodePos.element();
			try {
				System.out.println(((ExternalMerkleNode) node).getPath()
						+ " => " + node.getIndex());

			} catch (ClassCastException e) {
				System.out.println(node.getPath() + "("
						+ ((InternalMerkleNode) node).getFlag() + ")");
			}
			printTreeAux(nodePos);
		}
	}

	/**
	 * Set the q parameter
	 */
	public void setQ(int q) throws InvalidQParameterException {
		if ((q > 1) && ((q & (q - 1)) == 0)) {
			this.q = q;
			bitNode = ((Double) (Math.log(q) / Math.log(2))).intValue();
		} else
			throw new InvalidQParameterException(
					"The parameter q must be > 1 and power of 2");
	}

	/**
	 * Visit the tree in post order way
	 * 
	 * @return Internal nodes iterator
	 */
	public Iterator<Position<MerkleNode>> postOrderInternal() {
		ArrayList<Position<MerkleNode>> list = new ArrayList<Position<MerkleNode>>();
		Iterator<Position<MerkleNode>> positionIter = tree.postorder();
		while (positionIter.hasNext()) {
			Position<MerkleNode> nodePos = positionIter.next();
			if (nodePos.element() instanceof InternalMerkleNode)
				list.add(nodePos);
		}
		return list.iterator();
	}

	/**
	 * Preorder visit of the tree
	 * 
	 * @return Iterator of all nodes
	 */
	public Iterator<Position<MerkleNode>> preOrder() {
		return tree.preorder();
	}

	/**
	 * Internal node children
	 * 
	 * @param node
	 *            Parent node
	 * @return Iterable structure of parent node children
	 */
	public Iterable<Position<MerkleNode>> children(Position<MerkleNode> node) {
		return tree.children(node);
	}

	/**
	 * Number of children of a node
	 * 
	 * @param node
	 *            Parent node
	 * @return Number of children
	 */
	public int numberOfChildren(Position<MerkleNode> node) {
		return tree.numberOfChild(node);
	}

	/**
	 * Check if a node is the tree root
	 * 
	 * @param node
	 *            Node to check
	 * @return Boolean value: true if the node is the tree root, false otherwise
	 */
	public boolean isRoot(Position<MerkleNode> node) {
		return tree.isRoot(node);
	}

	/**
	 * Replace the node position with a new node
	 * 
	 * @param nodePos
	 *            Node position to replace
	 * @param node
	 *            New node
	 * @return Node replaced
	 */
	public MerkleNode replace(Position<MerkleNode> nodePos, MerkleNode node) {
		return tree.replace(nodePos, node);
	}

	/**
	 * Parent of a node
	 * 
	 * @param node
	 *            Child node
	 * @return Parent node
	 */
	public Position<MerkleNode> parent(Position<MerkleNode> node) {
		return tree.parent(node);
	}

	/**
	 * Tree root
	 */
	public Position<MerkleNode> root() {
		return tree.root();
	}

	/**
	 * Tree height
	 */
	public int height() {
		return tree.height();
	}

	/**
	 * Iterable structure of all nodes at a depth
	 * 
	 * @param depth
	 *            Depth of the nodes to return
	 */
	public Iterable<Position<MerkleNode>> atDepth(int depth) {
		return tree.atDepth(depth);
	}

	/**
	 * @param lambda
	 *            the lambda to set
	 */
	public void setLambda(int lambda) {
		this.lambda = lambda;
	}

	/**
	 * @return the lambda
	 */
	public int getLambda() {
		return lambda;
	}

	/**
	 * Set the elements of the object XStream that are used into the file xml to
	 * save the Commitment Merkle Tree Information
	 * 
	 * @return XStream object
	 */
	private static XStream getXStream() {
		XStream xstream = new XStream();

		xstream.processAnnotations(LinkedMerkleTree.class);
		xstream.processAnnotations(Commitment.class);

		xstream.setMode(XStream.ID_REFERENCES);

		xstream.alias("NE", NaiveElement.class);
		xstream.alias("CF", CurveField.class);
		xstream.alias("CE", CurveElement.class);
		xstream.alias("NF", NaiveField.class);
		xstream.alias("GE", GenericElement.class);

		xstream.omitField(NaiveElement.class, "secureRandom");
		xstream.omitField(CurveElement.class, "random");

		return xstream;
	}

	/**
	 * Store all Merkle Tree information into a file XML
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

		logger.info("XML file written");
	}

	/**
	 * Read the information into the file XML and create a CommitmentMerkleTree
	 * 
	 * @param path
	 *            The path where the data are stored
	 * @throws FileNotFoundException
	 *             File not found
	 */
	public static LinkedMerkleTree loadFromXML(String path)
			throws FileNotFoundException {
		XStream xstream = getXStream();
		XppReader xmlReader = new XppReader(new FileReader(path));

		LinkedMerkleTree lmt = (LinkedMerkleTree) xstream.unmarshal(xmlReader);

		return lmt;
	}

	/**
	 * Set random seed to setup a pseudorandom generator needed to create the
	 * paths from frontiers nodes to leaves don't belong to the tree
	 * 
	 * @param baseSeed
	 *            the baseSeed to set
	 */
	public void setBaseSeed(byte[] baseSeed) {
		this.baseSeed = baseSeed;
	}

	/**
	 * Get the base seed
	 * 
	 * @return the baseSeed
	 */
	public byte[] getBaseSeed() {
		return baseSeed;
	}

	/**
	 * Create a graph for a graphical representation of the tree
	 * 
	 * @param current
	 *            Current node
	 * @param graph
	 *            Graph to create
	 */
	private void createGraph(Position<MerkleNode> current,
			Forest<MerkleNode, String> graph) {

		Iterator<Position<MerkleNode>> iter;
		try {
			iter = tree.children(current).iterator();
		} catch (InvalidPositionException e) {
			return;
		}
		while (iter.hasNext()) {
			Position<MerkleNode> nodePos = iter.next();
			MerkleNode node = nodePos.element();
			graph.addEdge(node.getPath(), current.element(), node);
			createGraph(nodePos, graph);
		}

	}

	/**
	 * Build a graphical representation of the tree
	 * 
	 * @param graph
	 *            Graph to create
	 * @return Forest of tree nodes
	 */
	public Forest<MerkleNode, String> buildVisualTree(
			Forest<MerkleNode, String> graph) {
		graph.addVertex(tree.root().element());
		Position<MerkleNode> root = tree.root();
		createGraph(root, graph);
		return graph;
	}

	/**
	 * Tree size
	 * 
	 * @return number of nodes
	 */
	public int size() {
		return tree.size();
	}

	/**
	 * Post order visit
	 * 
	 * @return tree nodes iterator
	 */
	public Iterator<Position<MerkleNode>> postOrder() {
		return tree.postorder();
	}

}
