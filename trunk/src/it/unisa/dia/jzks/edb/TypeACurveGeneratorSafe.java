/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.edb;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a.TypeACurveGenerator;
import it.unisa.dia.gas.plaf.jpbc.util.BigIntegerUtils;

/**
 * Type A Elliptic Curve with r parameter safe prime
 */
public class TypeACurveGeneratorSafe extends TypeACurveGenerator {

	/**
	 * Constructor
	 * 
	 * @param rbits
	 *            length of r
	 * @param qbits
	 *            length of q
	 * @throws InvalidECParameterException Invalid rbits or qbits parameter
	 */
	public TypeACurveGeneratorSafe(int rbits, int qbits) throws InvalidECParameterException {
		super(rbits, qbits);
		if ((rbits <= 0))
			throw new InvalidECParameterException("rbits must be > 0");
		if ((qbits <= 0))
			throw new InvalidECParameterException("qbits must be > 0");
	}

	/**
	 * Generate a Type A Elliptic Curve with r safe prime
	 * 
	 * @return Map of Elliptic Curve parameters
	 */
	public Map generate() {

		boolean found = false;
		boolean found2 = false;

		BigInteger q = BigInteger.ZERO;
		BigInteger r = BigInteger.ZERO;
		BigInteger h = BigInteger.ZERO;
		int exp1 = 0, exp2 = 0;
		int sign0 = 0, sign1 = 0;

		SecureRandom random = new SecureRandom();
		random.setSeed(random.generateSeed(16));

		int attempt = 0;

		exp2 = rbits - 1;

		do {
			// r is picked to be a Solinas prime, that is, r has the form 2^a +-
			// 2^b +- 1 for some integers 0 < b < a.
			r = BigInteger.ZERO;

			if ((++attempt % 10) == 0)
				exp2--;

			if (random.nextInt(Integer.MAX_VALUE) % 2 != 0) {
				sign1 = 1;
			} else {
				sign1 = -1;
			}
			// r = r.setBit(exp2);

			// Speedup: if p=2q+1 => q%3=2, so q = 2^a +- 2^b - 1 => 2^a % 3 = 2
			// and 2^b % 3 = 1 => 2^a +- 2^b - 1 % 3 = 2
			q = BigInteger.ZERO;
			q = q.setBit(exp2);
			if (q.mod(BigInteger.valueOf(3)).compareTo(BigInteger.ONE) == 0)
				exp2--;

			q = BigInteger.ZERO;

			int c = random.nextInt(Integer.MAX_VALUE);
			int j = 1;

			while (!found2 && j < exp2) {

				r = BigInteger.ZERO;
				r = r.setBit(exp2);

				q = BigInteger.ZERO;

				exp1 = ((c + j) % (exp2 - 1)) + 1;
				q = q.setBit(exp1);

				if (sign1 > 0) {
					r = r.add(q);
				} else {
					r = r.subtract(q);
				}

				// TODO If sign0 = 1 => 2r+1 = 2^a+1 +- 2^b+1 + 2. else if sign0
				// = -1 => 2r+1 = 2^a+1 +- 2^b+1 - 2 + 1
				sign0 = -1;
				r = r.subtract(BigInteger.ONE);

				j++;

				if (r.mod(BigInteger.valueOf(3)).compareTo(
						BigInteger.valueOf(2)) != 0)
					continue;

				if (!r.isProbablePrime(10))
					continue;

				r = (r.multiply(BigInteger.valueOf(2))).add(BigInteger.ONE);

				if (r.mod(BigInteger.valueOf(3)).compareTo(
						BigInteger.valueOf(2)) != 0)
					continue;

				if (r.isProbablePrime(10)) {
					exp1++;
					exp2++;
					found2 = true;
				}
			}
		} while (!found2);

		do {
			for (int i = 0; i < 10; i++) {
				q = BigInteger.ZERO;
				int bit = qbits - rbits - 4 + 1;
				if (bit < 3)
					bit = 3;
				q = q.setBit(bit);

				// we randomly generate h where where h is a multiple of four
				// and sufficiently large to
				// guarantee (hr)^2 is big enough to resist finite field
				// attacks.
				// If h is constrained to be a multiple of three as well, then
				// cube roots are extremely easy to
				// compute in Fq: for all x ? Fq we see x?(q?2)/3 is the cube
				// root of x,
				h = BigIntegerUtils.getRandom(q).multiply(
						BigIntegerUtils.TWELVE);

				// Next it is checked that q = hr ?1 is prime, if it is the case
				// we have finished.
				// Also, we choose q = -1 mod 12 so F_q2 can be implemented as
				// F_q[i] (where i = sqrt(-1)).
				// Look at the class DegreeTwoQuadraticField and
				// DegreeTwoQuadraticElement
				q = h.multiply(r).subtract(BigInteger.ONE);

				if (q.isProbablePrime(10)) {
					found = true;
					break;
				}
			}
		} while (!found);

		CurveParams params = new CurveParams();
		params.put("type", "a");
		params.put("q", q.toString());
		params.put("r", r.toString());
		params.put("h", h.toString());
		params.put("exp1", String.valueOf(exp1));
		params.put("exp2", String.valueOf(exp2));
		params.put("sign0", String.valueOf(sign0));
		params.put("sign1", String.valueOf(sign1));

		return params;
	}

}
