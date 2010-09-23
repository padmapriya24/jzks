/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.utils;

import it.unisa.dia.jzks.edb.CommitmentMerkleTree;
import it.unisa.dia.jzks.edb.FailedZKSVerifyException;
import it.unisa.dia.jzks.edb.InvalidECParameterException;
import it.unisa.dia.jzks.edb.KeyMismatchZKSVerifyException;
import it.unisa.dia.jzks.edb.ParameterValueMismatch;
import it.unisa.dia.jzks.edb.PiGreek;
import it.unisa.dia.jzks.edb.SecurityParameterNotSatisfied;
import it.unisa.dia.jzks.edb.ZKSVerifier;
import it.unisa.dia.jzks.edb.ZeroKnowledgeSet;
import it.unisa.dia.jzks.merkleTree.InvalidQParameterException;
import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;
import it.unisa.dia.jzks.merkleTree.RootMerkleNode;

import java.io.FileNotFoundException;
import java.security.NoSuchAlgorithmException;
import java.util.Hashtable;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BuildMerkleTree {

	/**
	 * Build a Merkle Tree and perform ZKS operations
	 */
	public static void main(String[] args) throws InvalidQParameterException,
			InvalidECParameterException, FileNotFoundException,
			FailedZKSVerifyException, KeyMismatchZKSVerifyException,
			NoSuchAlgorithmException, ParameterValueMismatch,
			SecurityParameterNotSatisfied {

		Logger logger = Logger.getLogger("it.unisa.dia.jzks");
		logger.setLevel(Level.INFO);

//		ConsoleHandler handler = new ConsoleHandler();
//		handler.setLevel(Level.FINEST);
//		logger.addHandler(handler);

		Logger logger2 = Logger.getLogger("en.ciao");
		logger2.info("START");

		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		String key = "nome";

		for (int i = 0; i < 10; i++)
			ht.put((key + i), key);

		String what = new String();
		String tofind = new String();
		if (args.length == 1) {
			what = args[0];
			if (!what.equals("build")) {
				System.out
						.println("Usage: BuilderMerkleTree build|load|all [keyToFind]");
				logger2.info("END");
				return;
			}
		} else if (args.length == 2) {
			what = args[0];
			tofind = args[1];
		} else {
			System.out
					.println("Usage: BuilderMerkleTree build|load|all [keyToFind]");
			logger2.info("END");
			return;
		}

		if (what.equals("build") || what.equals("all")) {

			logger2.info("Building Merkle Tree...");
			CommitmentMerkleTree comm = new CommitmentMerkleTree(160, 512, 16,
					"SHA1");
			comm.populateTreeLeaves(ht);
			System.out.println("numero di nodi: " + comm.getTree().size());
			// CommitmentInformations mi =
			comm.commit();

			logger2.info("Saving Merkle Tree...");
			comm.saveTreeToXML("tree.xml", "UTF-8");
			logger2.info("Saving Root...");
			((RootMerkleNode) comm.getTree().root().element()).saveToXML(
					"root.xml", "UTF-8");

		}
		if (what.equals("load") || what.equals("all")) {
			logger2.info("Loading Merkle Tree...");
			LinkedMerkleTree tree = LinkedMerkleTree.loadFromXML("booh.xml");
			logger2.info("Loading Root...");
			RootMerkleNode root = new RootMerkleNode();
			// root = (RootMerkleNode) tree.root().element();
			root = RootMerkleNode.loadFromXML("root.xml");

			logger2.info("Proofing " + tofind + "...");
			ZeroKnowledgeSet zks = new ZeroKnowledgeSet(ht, tree);
			if (zks.belong(tofind))
				System.out.println("Appartiene al DB");
			else
				System.out.println("NON appartiene al DB");

			logger2.info("Saving Proof...");
			PiGreek pg = new PiGreek();
			pg = zks.getPiGreek();
			pg.saveToXML("piGreek.xml", "UTF-8");

			logger2.info("Loading Proof...");
			pg = PiGreek.loadFromXML("piGreek.xml");

			logger2.info("Verifing Proof...");
			ZKSVerifier ver = new ZKSVerifier();
			System.out.println(ver.verifier(pg, tofind, root));
		}
		logger2.info("END");
	}
}
