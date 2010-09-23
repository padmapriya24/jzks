/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb.test;

import it.unisa.dia.jzks.edb.InvalidECParameterException;
import it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe;

import java.math.BigInteger;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Curve generator test case
 */
@SuppressWarnings("unused")
public class TypeACurveGeneratorSafeTest extends TestCase {

	TypeACurveGeneratorSafe curveGenerator;

	/**
	 * @param name
	 */
	public TypeACurveGeneratorSafeTest(String name) {
		super(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe#TypeACurveGeneratorSafe(int, int)}
	 * .
	 */
	public final void testTypeACurveGeneratorSafe() {
		try {
			curveGenerator = new TypeACurveGeneratorSafe(512, 1024);
		} catch (InvalidECParameterException e) {
			fail("Invalid parameters");
		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe#TypeACurveGeneratorSafe(int, int)}
	 * .
	 */
	public final void testTypeACurveGeneratorSafeInvalidR() {
		try {
			TypeACurveGeneratorSafe curveGenerator = new TypeACurveGeneratorSafe(
					0, 512);
			fail("Invalid parameters");
		} catch (InvalidECParameterException e) {

		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe#TypeACurveGeneratorSafe(int, int)}
	 * .
	 */
	public final void testTypeACurveGeneratorSafeInvalidQ() {
		try {
			TypeACurveGeneratorSafe curveGenerator = new TypeACurveGeneratorSafe(
					160, 0);
			fail("Invalid parameters");
		} catch (InvalidECParameterException e) {

		}
	}

	/**
	 * Test method for
	 * {@link it.unisa.dia.jzks.edb.TypeACurveGeneratorSafe#generate()}.
	 */
	public final void testGenerate() {
		testTypeACurveGeneratorSafe();
		Map<String, String> parameters = curveGenerator.generate();
		assertFalse(parameters == null);
		// Checks on r parameter
		BigInteger r = new BigInteger(parameters.get("r"));
		assertTrue(r.isProbablePrime(10));
		BigInteger p = (r.subtract(BigInteger.ONE)).divide(BigInteger
				.valueOf(2));
		assertTrue(p.isProbablePrime(10));
	}

}
