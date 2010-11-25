/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.utils;

import it.unisa.dia.jzks.edb.CommitmentMerkleTree;
import it.unisa.dia.jzks.edb.FailedZKSVerifyException;
import it.unisa.dia.jzks.edb.InvalidECParameterException;
import it.unisa.dia.jzks.edb.KeyMismatchZKSVerifyException;
import it.unisa.dia.jzks.edb.ParameterValueMismatchException;
import it.unisa.dia.jzks.edb.PiGreek;
import it.unisa.dia.jzks.edb.SecurityParameterNotSatisfiedException;
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

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class BuildMerkleTree {

	/**
	 * Build a Merkle Tree and perform ZKS operations
	 */
	public static void main(String[] args) throws InvalidQParameterException,
			InvalidECParameterException, FileNotFoundException,
			FailedZKSVerifyException, KeyMismatchZKSVerifyException,
			NoSuchAlgorithmException, ParameterValueMismatchException,
			SecurityParameterNotSatisfiedException, ParseException {

		Options opt = new Options();
		opt.addOption("h", "help", false, "Print help for this application");
		opt.addOption("b", "build", false, "Build");
		opt.addOption("q", "qbits", true, "EC q bits");
		opt.addOption("r", "rbits", true, "EC r bits");
		opt.addOption("m", "qmer", true, "q Commitment");
		opt.addOption("d", "hash", true, "Hash Algorithm");
		opt.addOption("t", "tree", true, "Tree");
		opt.addOption("o", "root", true, "Root");
		opt.addOption("c", "prove", false,
				"Check if an element belongs to database");
		opt.addOption("f", "find", true, "Key to find");
		opt.addOption("v", "verify", false, "Verify");
		opt.addOption("p", "proof", true, "Proof");
		opt.addOption("a", "all", false, "All");
		opt.addOption("V", "level", true, "Logger");
		opt.addOption("w", "wrapper", false, "Wrapper");
		opt.addOption("s", "size", true, "Database size");

		String what = new String();

		BasicParser parser = new BasicParser();
		CommandLine cl = parser.parse(opt, args);

		if (cl.hasOption('h')) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("OptionsTip", opt);
		}
		if (cl.hasOption('b'))
			what += "b";
		if (cl.hasOption('v'))
			what += "v";
		if (cl.hasOption('c'))
			what += "c";
		if (cl.hasOption('a'))
			what += "a";

		Logger logger2 = Logger.getLogger("it.unisa.dia.test");
			
		Logger logger = Logger.getLogger("it.unisa.dia.jzks");
		try {
			logger.setLevel(Level.parse(cl.getOptionValue('V')));
			// remove the default ConsoleHandler specified in JRE logging
			// configuration file
			logger.getParent().removeHandler(logger.getParent().getHandlers()[0]);

			ConsoleHandler handler = new ConsoleHandler();
			handler.setLevel(Level.FINEST);
			logger.addHandler(handler);
			logger2.addHandler(handler);
		} catch (NullPointerException e) {
			logger.setLevel(Level.OFF);
		}

		logger2.info("START");
		
		Hashtable<String, Object> ht = new Hashtable<String, Object>();
		String key = "key";
		String value = "value";
		int size = 10;
		if (cl.hasOption('s'))
			size = Integer.parseInt(cl.getOptionValue('s'));
		for (int i = 0; i < size; i++)
			ht.put((key + i), (value + i));

		LinkedMerkleTree tree = null;
		RootMerkleNode root = null;

		if ((what.indexOf("b") != -1) || (what.indexOf("a") != -1)) {

			logger2.info("Building Merkle Tree...");
			CommitmentMerkleTree comMerkleTree = new CommitmentMerkleTree(
					Integer.parseInt(cl.getOptionValue('r')), Integer
							.parseInt(cl.getOptionValue('q')), Integer
							.parseInt(cl.getOptionValue('m')), cl
							.getOptionValue('d'), cl.hasOption('w'));
			comMerkleTree.populateTreeLeaves(ht);
			System.out.println("#nodes: " + comMerkleTree.getTree().size());
			comMerkleTree.commit();

			tree = comMerkleTree.getTree();
			root = (RootMerkleNode) tree.root().element();

			logger2.info("Saving Merkle Tree...");
			comMerkleTree.saveTreeToXML(cl.getOptionValue('t'), "UTF-8");
			logger2.info("Saving Root...");
			((RootMerkleNode) comMerkleTree.getTree().root().element())
					.saveToXML(cl.getOptionValue('o'), "UTF-8");

		}

		PiGreek pg = new PiGreek();
		if (what.indexOf("c") != -1) {
			logger2.info("Loading Merkle Tree...");
			tree = LinkedMerkleTree.loadFromXML(cl.getOptionValue('t'));
		}
		if ((what.indexOf("c") != -1) || (what.indexOf("a") != -1)) {
			ZeroKnowledgeSet zks = new ZeroKnowledgeSet(ht, tree);
			logger2.info("Proving " + cl.getOptionValue('f') + "...");
			if (zks.belong(cl.getOptionValue('f')))
				System.out.println("Appartiene al DB");
			else
				System.out.println("NON appartiene al DB");

			logger2.info("Saving Proof...");
			pg = zks.getPiGreek();
			pg.saveToXML(cl.getOptionValue('p'), "UTF-8");
		}

		if (what.indexOf("v") != -1) {
			logger2.info("Loading Root...");
			root = RootMerkleNode.loadFromXML(cl.getOptionValue('o'));
			logger2.info("Loading Proof...");
			pg = PiGreek.loadFromXML(cl.getOptionValue('p'));
		}
		if ((what.indexOf("v") != -1) || (what.indexOf("a") != -1)) {
			logger2.info("Verifing Proof...");
			ZKSVerifier ver = new ZKSVerifier();
			System.out.println(ver.verifier(pg, cl.getOptionValue('f'), root));
		}
		logger2.info("END");
	}
}
