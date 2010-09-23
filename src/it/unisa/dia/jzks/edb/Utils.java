/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import it.unisa.dia.jzks.merkleTree.MerkleNode;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * This class contains some utility methods needed by commit and decommit
 * operations
 */
public class Utils {

	/**
	 * Message digest
	 */
	private MessageDigest md;

	/**
	 * Hash algorithm: MD5 SHA-1 SHA-256 SHA-384 and SHA-512
	 */
	private String hashAlgo = DEFAULT_HASH_ALGO;

	/**
	 * Logger to print messages to console
	 */
	private Logger logger = Logger.getLogger("it.unisa.dia.jzks.zksqTMC.Utils");

	/**
	 * Default Hash algorithm SHA-512
	 */
	public static final String DEFAULT_HASH_ALGO = "SHA-512";

	/**
	 * Pseudorandom generator algorithm
	 */
	public static final String PR_ALGO = "SHA1PRNG";

	/**
	 * Constructor
	 * 
	 * @param hashAlgo
	 *            Hash algorithm
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 */
	public Utils(String hashAlgo) throws NoSuchAlgorithmException {
		setHashAlgo(hashAlgo);
	}

	/**
	 * Constructor
	 * 
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 */
	public Utils() throws NoSuchAlgorithmException {
		this(DEFAULT_HASH_ALGO);
	}

	/**
	 * Return the digest length in bits
	 * 
	 * @return digest length
	 */
	public int getDigestLength() {
		return (md.getDigestLength() * 8);
	}

	/**
	 * Return the digest value of a byte array
	 * 
	 * @param b
	 *            Byte array to hash
	 * @return Digest value byte array
	 */
	public byte[] getDigestValue(byte[] b) {
		md.reset();
		md.update(b);
		return md.digest();
	}

	/**
	 * Generate the hash value of an object
	 * 
	 * @param value
	 *            The object
	 * @return Hash value
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not valid
	 */
	public byte[] makeHashValue(Object value) {
		byte[] bytes = null;
		md.reset();
		md.update(getBytes(value));

		bytes = md.digest();
		return bytes;
	}

	/**
	 * Return a byte array representation of an object
	 * 
	 * @param obj
	 *            Object to represent
	 * @return Byte array of the object
	 */
	public byte[] getBytes(Object obj) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			bos.close();
		} catch (IOException e) {
			logger.severe("Error during conversion object in byte[]");
			logger.log(Level.INFO, "Exception caught", e);
		}
		byte[] data = bos.toByteArray();
		return data;
	}

	/**
	 * Return the hash value of an internal node
	 * 
	 * @param node
	 *            Node to hash
	 * @return Byte array of hash value
	 */
	public byte[] internalNodeHash(MerkleNode node) {
		String stringtoHash = new String();
		stringtoHash += node.getCommitment().getC();
		stringtoHash += node.getCommitment().getV();
		return makeHashValue(stringtoHash);
	}

	/**
	 * Concatenate two byte arrays
	 * 
	 * @param b1
	 *            First array
	 * @param b2
	 *            Second array
	 * @return The two arrays concatenated
	 */
	public byte[] concatenateByte(byte[] b1, byte[] b2) {
		byte[] b = new byte[b1.length + b2.length];
		int i = 0;
		for (i = 0; i < b1.length; i++)
			b[i] = b1[i];
		for (int j = 0; j < b2.length; j++)
			b[i++] = b2[j];
		return b;
	}

	/**
	 * Get a new pseudorandom generator
	 * 
	 * @return The instance of the pseudorandom generator
	 */
	public SecureRandom getNewRandomGenerator(String PR_ALGO) {
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance(PR_ALGO);
		} catch (NoSuchAlgorithmException e) {
			logger.severe(PR_ALGO
					+ " is not a valid pseudorandom generator algorithm");
			logger.log(Level.INFO, "Exception caught", e);
		}
		return random;
	}

	/**
	 * Get the hashing algorithm
	 * 
	 * @return the hashing algorithm used
	 */
	public String getHashAlgo() {
		return hashAlgo;
	}

	/**
	 * Set the hashing algorithm
	 * 
	 * @param hashAlgo
	 *            the hashing algorithm to use
	 * @throws NoSuchAlgorithmException
	 *             Hash algorithm not found
	 */
	public void setHashAlgo(String hashAlgo) throws NoSuchAlgorithmException {
		this.hashAlgo = hashAlgo;
		md = MessageDigest.getInstance(hashAlgo);
	}

	/**
	 * Convert the array of bytes into a hex string
	 * 
	 * @param b
	 *            The array of bytes
	 * @return The hex string
	 */
	public String getHexString(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}

	/**
	 * Get the PrintWriter object to write the xml file
	 * 
	 * @param path
	 *            Xml file path
	 * @param ENCODING
	 *            Encoding to use
	 * @param xstream
	 *            Xstream object used to write the xml
	 * @param obj
	 *            Object to serialize
	 * @return PrintWriter object
	 */
	public static PrintWriter getPrintWriter(String path, String ENCODING,
			XStream xstream, Object obj) {
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(path, ENCODING);
			writer.print("<?xml version=\"1.0\" encoding=\"" + ENCODING
					+ "\"?>"); // ISO-8859-1
			writer.flush();

			CompactWriter cWriter = new CompactWriter(writer);
			xstream.marshal(obj, cWriter);

		} catch (FileNotFoundException e) {
			System.err.println("File " + path + " not found");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.err.println("Unsupported Encoding");
		}
		return writer;
	}

}
