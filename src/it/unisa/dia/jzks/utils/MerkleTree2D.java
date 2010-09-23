/**
 * @author Costante Luca (dott.costante@gmail.com)
 * @author Giardino Daniele (dagix5@gmail.com)
 */
package it.unisa.dia.jzks.utils;

import it.unisa.dia.jzks.merkleTree.ExternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.InternalMerkleNode;
import it.unisa.dia.jzks.merkleTree.LinkedMerkleTree;
import it.unisa.dia.jzks.merkleTree.MerkleNode;
import it.unisa.dia.jzks.qTMC.LibertYung_qTMC;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.PolarPoint;
import edu.uci.ics.jung.algorithms.layout.RadialTreeLayout;
import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.DelegateTree;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.Tree;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * Graphical tree representation
 */
@SuppressWarnings("serial")
public class MerkleTree2D extends JApplet {

	Forest<MerkleNode, String> graph;

	Factory<DirectedGraph<MerkleNode, String>> graphFactory = new Factory<DirectedGraph<MerkleNode, String>>() {

		public DirectedGraph<MerkleNode, String> create() {
			return new DirectedSparseMultigraph<MerkleNode, String>();
		}
	};

	Factory<Tree<MerkleNode, String>> treeFactory = new Factory<Tree<MerkleNode, String>>() {

		public Tree<MerkleNode, String> create() {
			return new DelegateTree<MerkleNode, String>(graphFactory);
		}
	};

	/**
	 * the visual component and renderer for the graph
	 */
	VisualizationViewer<MerkleNode, String> vv;

	VisualizationServer.Paintable rings;

	String root;

	TreeLayout<MerkleNode, String> treeLayout;

	RadialTreeLayout<MerkleNode, String> radialLayout;

	public MerkleTree2D(final LinkedMerkleTree tr) {
		// create a simple graph for the demo
		graph = new DelegateForest<MerkleNode, String>();

		graph = tr.buildVisualTree(graph);

		treeLayout = new TreeLayout<MerkleNode, String>(graph);
		radialLayout = new RadialTreeLayout<MerkleNode, String>(graph);
		radialLayout.setSize(new Dimension(1200, 600));
		vv = new VisualizationViewer<MerkleNode, String>(treeLayout,
				new Dimension(1200, 600));
		vv.setBackground(Color.white);
		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line());

		vv.getRenderContext().setVertexLabelTransformer(
				new Transformer<MerkleNode, String>() {
					public String transform(MerkleNode vertex) {
						int l = ((Double) (Math.log(tr.getQ()) / Math.log(2)))
								.intValue();
						String l3 = vertex.getPath().substring(
								vertex.getPath().length() - l);
						return l3;
					}
				});

		vv.getRenderContext().setVertexFillPaintTransformer(
				new Transformer<MerkleNode, Paint>() {
					public Paint transform(MerkleNode node) {
						if (node instanceof InternalMerkleNode) {
							InternalMerkleNode mn = (InternalMerkleNode) node;
							if (mn.getFlag() == LibertYung_qTMC.HARD_COMMITMENT)
								return Color.black;
							else
								return Color.yellow;
						} else if (node instanceof ExternalMerkleNode) {
							ExternalMerkleNode mn = (ExternalMerkleNode) node;
							if (mn.getKey().equals(MerkleNode.EMPTY_KEY))
								return Color.black;
							else
								return Color.green;
						}
						return Color.red;
					}
				});

		// vv.setVertexToolTipTransformer(new ToStringLabeller());
		vv.setVertexToolTipTransformer(new Transformer<MerkleNode, String>() {
			public String transform(MerkleNode vertex) {
				String tooltip;
				if (vertex instanceof InternalMerkleNode) {
					InternalMerkleNode node = (InternalMerkleNode) vertex;
					tooltip = String.valueOf(node.getIndex());
				} else {
					ExternalMerkleNode node = (ExternalMerkleNode) vertex;
					tooltip = "<html><body>" + String.valueOf(node.getIndex())
							+ "<br>" + node.getKey() + "</body></html>";
				}
				return tooltip;
			}
		});
		vv.getRenderContext().setArrowFillPaintTransformer(
				new ConstantTransformer(Color.lightGray));
		rings = new Rings();

		Container content = getContentPane();
		final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
		content.add(panel);

		final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

		vv.setGraphMouse(graphMouse);

		JComboBox modeBox = graphMouse.getModeComboBox();
		modeBox.addItemListener(graphMouse.getModeListener());
		graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);

		final ScalingControl scaler = new CrossoverScalingControl();

		JButton plus = new JButton("+");
		plus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1.1f, vv.getCenter());
			}
		});
		JButton minus = new JButton("-");
		minus.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				scaler.scale(vv, 1 / 1.1f, vv.getCenter());
			}
		});

		JToggleButton radial = new JToggleButton("Radial");
		radial.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {

					LayoutTransition<MerkleNode, String> lt = new LayoutTransition<MerkleNode, String>(
							vv, treeLayout, radialLayout);
					Animator animator = new Animator(lt);
					animator.start();
					vv.getRenderContext().getMultiLayerTransformer()
							.setToIdentity();
					vv.addPreRenderPaintable(rings);
				} else {
					LayoutTransition<MerkleNode, String> lt = new LayoutTransition<MerkleNode, String>(
							vv, radialLayout, treeLayout);
					Animator animator = new Animator(lt);
					animator.start();
					vv.getRenderContext().getMultiLayerTransformer()
							.setToIdentity();
					vv.removePreRenderPaintable(rings);
				}
				vv.repaint();
			}
		});

		JPanel scaleGrid = new JPanel(new GridLayout(1, 0));
		scaleGrid.setBorder(BorderFactory.createTitledBorder("Zoom"));

		JPanel controls = new JPanel();
		scaleGrid.add(plus);
		scaleGrid.add(minus);
		controls.add(radial);
		controls.add(scaleGrid);
		controls.add(modeBox);

		content.add(controls, BorderLayout.SOUTH);
	}

	class Rings implements VisualizationServer.Paintable {

		Collection<Double> depths;

		public Rings() {
			depths = getDepths();
		}

		private Collection<Double> getDepths() {
			Set<Double> depths = new HashSet<Double>();
			Map<MerkleNode, PolarPoint> polarLocations = radialLayout
					.getPolarLocations();
			for (MerkleNode v : graph.getVertices()) {
				PolarPoint pp = polarLocations.get(v);
				depths.add(pp.getRadius());
			}
			return depths;
		}

		public void paint(Graphics g) {
			g.setColor(Color.BLACK);

			Graphics2D g2d = (Graphics2D) g;
			Point2D center = radialLayout.getCenter();

			Ellipse2D ellipse = new Ellipse2D.Double();
			for (double d : depths) {
				ellipse.setFrameFromDiagonal(center.getX() - d, center.getY()
						- d, center.getX() + d, center.getY() + d);
				Shape shape = vv.getRenderContext().getMultiLayerTransformer()
						.getTransformer(Layer.LAYOUT).transform(ellipse);
				g2d.draw(shape);
			}
		}

		public boolean useTransform() {
			return true;
		}
	}
}
