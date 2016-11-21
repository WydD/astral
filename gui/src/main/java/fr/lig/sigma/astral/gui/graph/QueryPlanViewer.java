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

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import fr.lig.sigma.astral.interpreter.common.XMLUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

import javax.swing.*;
import java.awt.*;

/**
 *
 */
public class QueryPlanViewer {
    private static Logger log = Logger.getLogger(QueryPlanViewer.class);
    private mxGraph graph;
    private Object parent;
    private JPanel panel;
    private int sourceId = 0;

    public QueryPlanViewer(Document doc) {
        graph = new mxGraph();
        graph.setHtmlLabels(true);
        parent = graph.getDefaultParent();
        graph.setAutoSizeCells(true);
        graph.getModel().beginUpdate();

        Element query = (Element) XMLUtils.seek("//query[1]", doc);
        buildGraph((Element) query.getFirstChild());
        graph.getModel().endUpdate();
        mxHierarchicalLayout layout = new mxHierarchicalLayout(graph, SwingConstants.WEST);
        layout.execute(parent);
        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setEventsEnabled(false);
        graphComponent.getViewport().setOpaque(false);
        graphComponent.setBorder(BorderFactory.createEmptyBorder());
        //graphComponent.setBackground(Color.WHITE);
        graphComponent.setConnectable(false);
        graphComponent.setEnabled(false);
        panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(graphComponent, BorderLayout.CENTER);
    }

    private Object buildGraph(Element e) {
        if ("condition".equals(e.getTagName()) ||
                "rename".equals(e.getTagName()) ||
                "description".equals(e.getTagName()) ||
                "aggregate".equals(e.getTagName()))
            return null;
        String style = mxConstants.STYLE_SPACING + "=5;" +
                mxConstants.STYLE_ROUNDED + "=true;";
        NodeList list = e.getChildNodes();
        String content = getHtmlContent(e, list);

        Object root = graph.insertVertex(parent, null, content, 200, 200, 200, 200, style);
        graph.updateCellSize(root);
        for (int i = 0; i < list.getLength(); i++) {
            Object v = buildGraph((Element) list.item(i));
            if (v != null)
                graph.insertEdge(parent, null, "", v, root);
        }
        return root;
    }

    private String getHtmlContent(Element e, NodeList list) {
        String content = "<html><center><b>" + e.getNodeName() + "</b></center>";
        NamedNodeMap attributes = e.getAttributes();

        String childContent = "";
        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item(i);
            if ("condition".equals(child.getTagName()))
                childContent += "<tr><td><u>condition</u>:</td><td>" + child.getAttribute("attribute") + " " +
                        child.getAttribute("operator") + " " + child.getAttribute("value") + child.getAttribute("otherAttribute") + "</td></tr>";
            else if ("rename".equals(child.getTagName()))
                childContent += "<tr><td><u>rename</u>:</td><td>" + child.getAttribute("from") + " => " +
                        child.getAttribute("to") + "</td></tr>";
            else if ("aggregate".equals(child.getTagName()))
                childContent += "<tr><td><u>aggregate</u>:</td><td>" + child.getAttribute("function") + "(" +
                        child.getAttribute("attribute") + ")</td></tr>";
            else if ("description".equals(child.getTagName())) {
                childContent += "<tr><td><u>"+child.getTagName()+"</u>:</td><td><table border='0' cellpadding='0' cellspacing='0'>";
                NamedNodeMap childAttributes = child.getAttributes();
                for (int j = 0; j < childAttributes.getLength(); j++) {
                    Attr a = (Attr) childAttributes.item(j);
                    childContent += "<tr><td>" + a.getName() + ":</td><td>" + a.getValue() + "</td></tr>";
                }
                childContent += "</table></td></tr>";
            }
        }
        if (attributes.getLength() > 0 || !childContent.isEmpty()) {
            content += "<hr/><table border='0' cellpadding='1' cellspacing='0'>";
            content += childContent;
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr a = (Attr) attributes.item(i);
                content += "<tr><td>" + a.getName() + ":</td><td>" + a.getValue() + "</td></tr>";
            }
            content += "</table>";
        }
        content += "</html>";
        return content;
    }

    public JPanel getPanel() {
        return panel;
    }
}
