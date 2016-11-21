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

import edu.uci.ics.jung.graph.OrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import fr.lig.sigma.astral.query.GraphNotifier;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class JUNGEngineGraph implements GraphNotifier {
    private Logger log = Logger.getLogger(JUNGEngineGraph.class);
    private JUNGEnginePanel panel;
    private int edge = 0;
    private Map<Long, AnimateState> notices = new HashMap<Long, AnimateState>();
    private Map<Long, QueryNode> nodes = new HashMap<Long, QueryNode>();
    private OrderedSparseMultigraph<QueryNode, String> graph = new OrderedSparseMultigraph<QueryNode, String>();

    public JUNGEngineGraph() {
    }

    private QueryNode nodeCache(QueryNode vertex) {
        if(vertex == null) return null;
        long id = vertex.getId();
        /*if("source".equals(vertex.getName())) {
            String sourceId = (String) vertex.getParameters().get("source.id");
            id = Long.parseLong(sourceId);
            vertex.setId(id);
        } */
        if(nodes.containsKey(id))
            return nodes.get(id);
        nodes.put(id, vertex);
        return vertex;
    }

    public boolean addVertex(QueryNode vertex) {
        if(!notices.containsKey(vertex.getId()))
            notices.put(vertex.getId(), new AnimateState(panel.getPickedState(), vertex));
        return graph.addVertex(vertex);
    }

    public synchronized void sendGraph(Map<String, QueryNode> queries) {
        graph = new OrderedSparseMultigraph<QueryNode, String>();
        notices.clear();
        nodes.clear();
        edge = 0;
        for(QueryNode node : queries.values()) {
            node = nodeCache(node);
            addQuery(node);
        }
        panel.redraw();
    }

    private void addQuery(QueryNode node) {
        if(node == null) return;
        addVertex(node);
        for(QueryNode child : node.getChildren()) {
            child = nodeCache(child);
            addQuery(child);
            graph.addEdge(String.valueOf(edge++), child, node, EdgeType.DIRECTED);
        }
    }

    public void sendNotice(QueryNode node) {
        notices.get(nodeCache(node).getId()).notice();
    }

    public synchronized OrderedSparseMultigraph<QueryNode, String> getGraph() {
        return graph;
    }

    public void setPanel(JUNGEnginePanel panel) {
        this.panel = panel;
    }
}
