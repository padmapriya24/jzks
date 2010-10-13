/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb.test;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.jzks.edb.CommitmentInformations;
import it.unisa.dia.jzks.edb.CommitmentMerkleTree;
import it.unisa.dia.jzks.edb.InvalidECParameterException;
import it.unisa.dia.jzks.edb.ParameterValueMismatchException;
import it.unisa.dia.jzks.edb.SecurityParameterNotSatisfiedException;
import it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe;
import it.unisa.dia.jzks.merkleTree.InvalidQParameterException;

import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Level;

import junit.framework.TestCase;

/**
 * Commitmemt Merkle Tree test case
 */
@SuppressWarnings("unused")
public class CommitmetMerkleTreeTest extends TestCase {

	CommitmentMerkleTree commitmentMerkleTree;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		commitmentMerkleTree = new CommitmentMerkleTree(160, 512, 8, "SHA-512");
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(java.lang.String, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeStringIntLevel() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(
					"params/a_181_603.properties", 8, "SHA-512");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("Invalid EC parameters");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(java.lang.String, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeStringIntLevelInvalidQ() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(
					"params/a_181_603.properties", 1, "SHA-512");
			fail("Invalid q parameter");
		} catch (InvalidQParameterException e) {

		} catch (InvalidECParameterException e) {
			fail("Invalid EC parameters");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(java.lang.String, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeStringIntLevelInvalidEC() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(
					"params/a_181_603.prop", 8, "SHA-512");
			fail("Invalid EC parameters");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("EC parameter not valid");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(int, int, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeIntIntIntLevel() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(160, 512, 8,
					"SHA-512");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("Invalid EC parameters");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(int, int, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeIntIntIntLevelInvalidR() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(0, 512, 8,
					"SHA-512");
			fail("Invalid EC parameters");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("EC parameter not valid");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(int, int, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeIntIntIntLevelInvalidQ() {
		try {
			CommitmentMerkleTree comm = new CommitmentMerkleTree(160, 0, 8,
					"SHA-512");
			fail("Invalid EC parameters");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("EC parameter not valid");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#CommitmentMerkleTree(it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams, int)}
	 * .
	 */
	public final void testCommitmentMerkleTreeCurveParamsInt() {
		try {
			TypeACurveGeneratorSafe curveGenerator = new TypeACurveGeneratorSafe(
					160, 512);

			CurveParams curveParams = new CurveParams();
			Map<String, String> parameters = curveGenerator.generate();

			curveParams.putAll(parameters);
			CommitmentMerkleTree comm = new CommitmentMerkleTree(curveParams,
					8, "SHA-512");
		} catch (InvalidQParameterException e) {
			fail("Invalid q parameter");
		} catch (InvalidECParameterException e) {
			fail("Invalid EC parameters");
		} catch (NoSuchAlgorithmException e) {
			fail("Hash algorithm not valid");
		} catch (ParameterValueMismatchException e) {
			fail("Digest length and q value not compatible");
		} catch (SecurityParameterNotSatisfiedException e) {
			fail("The depth of the tree does not satisfy the security parameter");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#populateTreeLeaves(java.util.Hashtable)}
	 * .
	 */
	public final void testPopulateTreeLeaves() {
		Hashtable<String, Object> database = new Hashtable<String, Object>();
		String key = "key";
		String value = "value";
		for (int i = 0; i < 10; i++)
			database.put((key + i), (value + i));

		assertTrue(commitmentMerkleTree.populateTreeLeaves(database));

	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#populateTreeLeaves(java.util.Hashtable)}
	 * .
	 */
	public final void testPopulateTreeLeavesNullDB() {
		Hashtable<String, Object> database = null;

		assertFalse(commitmentMerkleTree.populateTreeLeaves(database));

	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.CommitmentMerkleTree#commit()}.
	 */
	public final void testCommit() {
		testPopulateTreeLeaves();
		CommitmentInformations commitmentInformations = commitmentMerkleTree
				.commit();
		assertFalse(commitmentInformations == null);
	}

}
