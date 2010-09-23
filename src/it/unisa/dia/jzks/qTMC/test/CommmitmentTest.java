/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.qTMC.test;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import it.unisa.dia.jzks.qTMC.Commitment;
import it.unisa.dia.jzks.qTMC.CommitmentKeys;
import it.unisa.dia.jzks.qTMC.MessageMismatchException;
import it.unisa.dia.jzks.qTMC.OutputCommit;

import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Commitment test case
 */
public class CommmitmentTest extends TestCase {
	ArrayList<Element> messageArray;
	CommitmentKeys keys;
	Commitment commitment;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		CurveParams curveParams = new CurveParams()
				.load("params/a_181_603.properties");
		Pairing pairing = PairingFactory.getPairing(curveParams);

		Field G1 = pairing.getG1();
		Field Zr = pairing.getZr();

		commitment = new Commitment(G1, Zr, pairing);

		int q = 3;
		// Pk and Tk generation
		keys = commitment.qKeygen(q);
		Element m1 = Zr.newRandomElement();
		Element m2 = Zr.newRandomElement();
		Element m3 = Zr.newRandomElement();
		messageArray = new ArrayList<Element>();
		messageArray.add(0, m1);
		messageArray.add(1, m1);
		messageArray.add(2, m2);
		messageArray.add(3, m3);
	}

	/**
	 * Test Method for Hard Commitment
	 */
	public final void testHardCommit() {
		// Messages commitment
		OutputCommit ohc = commitment.qHCom(keys.getPk(), messageArray);

		// Message 3 opening
		Element mi = ohc.getAux().get(3);
		Element[] piGreek = null;
		try {
			piGreek = commitment.qHOpen(keys.getPk(), mi, 3, ohc.getAux());
		} catch (MessageMismatchException e) {
			fail("Message mismatch");
		}

		// Hard decommit
		assertTrue(commitment.qHVer(keys.getPk(), mi, 3, ohc.getC(),
				ohc.getV(), piGreek));
		System.out.println("Hard Verify OK");

		// Soft decommit
		Element Wis = commitment.qSOpen(keys.getPk(), messageArray.get(3), 3,
				0, ohc.getAux());
		assertTrue(commitment.qSVer(keys.getPk(), messageArray.get(3), 3, ohc
				.getC(), ohc.getV(), Wis));
		System.out.println("Soft Verify OK");
	}

	/**
	 * Test Method for Fake Commitment
	 */
	public final void testFakeCommit() {

		// Messages commitment
		OutputCommit ohc = commitment.qFake(keys.getPk(), keys.getTk());

		// Message 3 opening
		Element[] piGreek = commitment.qHEquiv(keys.getPk(), keys.getTk(),
				messageArray, 3, ohc.getAux());

		// Hard decommit
		assertTrue(commitment.qHVer(keys.getPk(), messageArray.get(3), 3, ohc
				.getC(), ohc.getV(), piGreek));
		System.out.println("Hard Verify OK");

		// Soft decommit
		Element Wis = commitment.qSEquiv(keys.getPk(), keys.getTk(),
				messageArray.get(3), 3, ohc.getAux());
		assertTrue(commitment.qSVer(keys.getPk(), messageArray.get(3), 3, ohc
				.getC(), ohc.getV(), Wis));
		System.out.println("Soft Verify OK");
	}

	/**
	 * Test Method for Soft Commitment
	 */
	public final void testSoftCommit() {

		// soft commit
		OutputCommit out = commitment.qSCom(keys.getPk());

		// message for soft-decommit
		Element m = commitment.getZr().newRandomElement();

		// soft decommit
		Element W = commitment.qSOpen(keys.getPk(), m, 1, 1, out.getAux());

		assertTrue(commitment.qSVer(keys.getPk(), m, 1, out.getC(), out.getV(),
				W));
		System.out.println("Soft Verify OK");

	}

}
