/*
 * Copyright 2012 LIG SIGMA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.lig.sigma.astral.gui.graph;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import edu.uci.ics.jung.visualization.control.MouseListenerTranslator;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.PickableVertexPaintTransformer;
import edu.uci.ics.jung.visualization.picking.MultiPickedState;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.VertexLabelAsShapeRenderer;
import fr.lig.sigma.astral.gui.graph.sugiyama.JUNGEngineLayout;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.commons.collections15.Transformer;

import javax.management.Query;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

/**
 * @author Loic Petit
 */
public class JUNGEnginePanel extends JPanel implements ComponentListener {
    private VisualizationViewer<QueryNode, String> vv;
    private Layout<QueryNode, String> layout;
    private JUNGEngineGraph g;
    private PickedState<QueryNode> ps;
    private ScalingControl scaler;
    private JUNGEngineLayout test;
    private Transformer<QueryNode, String> labelTransformer;
    private boolean debugVertices = false;

    public JUNGEnginePanel(JUNGEngineGraph graph) {
        // Graph<V, E> where V is the type of the vertices and E is the type of the edges
        labelTransformer = new Transformer<QueryNode, String>() {
            public String transform(QueryNode vertex) {

                if (vertex.getName().equals("virtual")) {
                    return "";
                }
                if (vertex.getName().equals("source")) {
                    return getDisplayLabel("<b>&nbsp;&nbsp;&nbsp;&nbsp;" +
                            vertex.getParameters().get("id") + "&nbsp;&nbsp;&nbsp;&nbsp;</b>");

                }
                if (vertex.getName().equals("handler")) {
                    return getDisplayLabel("<b>&nbsp;&nbsp;&nbsp;&nbsp;" +
                            vertex.getParameters().get("type") + "&nbsp;&nbsp;&nbsp;&nbsp;</b>");
                }
                if (vertex.getName().equals("unary")) {
                    String r = "<table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b><center>";
                    List<Map<String, Object>> operations = (List<Map<String, Object>>) vertex.getParameters().get("operations");
                    for(Map<String, Object> op : operations) {
                        r += ""+op.get("otype")+"<br/>";
                    }
                    r += "</center></b></td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr></table>";
                    //r = r.substring(0, r.length()-5);
                    return getDisplayLabel(r);
                }
                    String label = "";
                    label += "<table><tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td><td><b><center>";
                    label += vertex.getName().replaceAll("\n","<br/>");
                    label += "</center></b></td><td>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr></table>";
                    /*if (debugVertices) {
                        label += "<hr/>";
                        label += out instanceof Stream ? "Stream" : "Relation";
                        label += "<br/>";
                        label += AttributeSet.string(out.getAttributes());
                    } */
                    return getDisplayLabel(label);

            }
        };
        g = graph;
        g.setPanel(this);
        layout = new JUNGEngineLayout(g.getGraph(), labelTransformer);

        vv = new VisualizationViewer<QueryNode, String>(layout);
        vv.setPreferredSize(layout.getSize());

        vv.getRenderContext().setVertexLabelTransformer(labelTransformer);
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<QueryNode, String>());
        VertexLabelAsShapeRenderer<QueryNode, String> vlasr = new VertexLabelAsShapeRenderer<QueryNode, String>(vv.getRenderContext());

        vv.getRenderContext().setVertexShapeTransformer(vlasr);
        vv.getRenderer().setVertexRenderer(new BasicVertexRenderer<QueryNode, String>());
        vv.getRenderer().setVertexLabelRenderer(vlasr);
        vv.getRenderContext().setVertexLabelRenderer(new DefaultVertexLabelRenderer(Color.black));

        /*ConstantTransformer whiteColor = new ConstantTransformer(Color.WHITE);
      vv.getRenderContext().setEdgeDrawPaintTransformer(whiteColor);
      vv.getRenderContext().setArrowDrawPaintTransformer(whiteColor);
        */
        ps = new MultiPickedState<QueryNode>();
        vv.setPickedVertexState(ps);
        vv.getRenderContext().setVertexFillPaintTransformer(new PickableVertexPaintTransformer<QueryNode>(vv.getPickedVertexState(),
                Color.LIGHT_GRAY, new Color(255, 102, 0)));
        vv.setBackground(Color.WHITE);

        scaler = new CrossoverScalingControl();
        this.addComponentListener(this);
        setLayout(new BorderLayout());
        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();

        vv.setGraphMouse(graphMouse);

        add(panel, BorderLayout.CENTER);
        /*new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {

                }

                Dimension size = layout.getSize();
                BufferedImage bi = new BufferedImage((int)size.getWidth(),(int)size.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.createGraphics();
                vv.setSize(size);
        vv.paint(g);  //this == JComponent
        g.dispose();
        try{
            ImageIO.write(bi, "png", new File("test.png"));}catch (Exception e) {}
            }
        }).start();  */
    }

    public void addGraphMouseListener(GraphMouseListener<QueryNode> gml) {
        vv.addMouseListener(new MouseListenerTranslator<QueryNode, String>(gml, vv));
    }

    private String getDisplayLabel(String lbl) {
        String label = "<html><center><br/>";
        label += lbl;
        label += "";
        label += "</center><br/></html>";
        return label;
    }

    public synchronized void redraw() {
        layout = new JUNGEngineLayout(g.getGraph(), labelTransformer);
        
        //test.initialize();
        //layout.setSize(new Dimension(2000, 1000)); // sets the initial size of the space
        /*
        LayoutTransition<Object, String> lt =
                new LayoutTransition<Object, String>(vv, vv.getGraphLayout(),
                        layout);
        Animator animator = new Animator(lt);
        animator.start();
        vv.getRenderContext().getMultiLayerTransformer().setToIdentity();*/
        vv.setGraphLayout(layout);
        vv.repaint();
    }

    public PickedState<QueryNode> getPickedState() {
        return ps;
    }

    public void componentResized(ComponentEvent componentEvent) {                /*
        Dimension r = vv.getSize();
        Dimension d = layout.getSize();
        double ratioX = d.getWidth() * 1.0 / r.getWidth();
        double ratioY = d.getHeight() * 1.0 / r.getHeight();
        if(ratioX == 0 || ratioY == 0) return;
        scaler.scale(vv, (float) Math.min(ratioX,ratioY), vv.getCenter());     */
    }

    public void componentMoved(ComponentEvent componentEvent) {
    }

    public void componentShown(ComponentEvent componentEvent) {
    }

    public void componentHidden(ComponentEvent componentEvent) {
    }
}
