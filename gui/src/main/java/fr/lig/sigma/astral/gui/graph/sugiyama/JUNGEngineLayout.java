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

package fr.lig.sigma.astral.gui.graph.sugiyama;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.OrderedSparseMultigraph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.commons.collections15.Transformer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.max;

/**
 * @author Loic Petit
 */
public class JUNGEngineLayout extends AbstractLayout<QueryNode, String> {
    private SugiyamaLayerStack<QueryNode> stack;
    private Graph<QueryNode, String> graph;
    private int virtual = 0;

    public JUNGEngineLayout(OrderedSparseMultigraph<QueryNode, String> graph, Transformer<QueryNode, String> labelTransformer) {
        super(new SparseMultigraph<QueryNode,String>());
        this.graph = getGraph();
        for(String edge : graph.getEdges()) {
            Pair<QueryNode> vertices = graph.getEndpoints(edge);
            this.graph.addVertex(vertices.getFirst());
            this.graph.addVertex(vertices.getSecond());
            this.graph.addEdge(edge, vertices.getFirst(), vertices.getSecond(), EdgeType.DIRECTED);
        }
        stack = new SugiyamaLayerStack<QueryNode>(getGraph(), this, labelTransformer);
        initialize();
    }


    void splitIntoLayers() {
        Collection<QueryNode> sorted = topologicalSort();

        Map<QueryNode, Integer> lmap = new HashMap<QueryNode, Integer>();
        for (QueryNode n : sorted) {
            lmap.put(n, 0);
        }
        int h = 1;
        for (QueryNode n1 : sorted) {
            for (QueryNode n2 : graph.getSuccessors(n1)) {
                int inc = graph.inDegree(n2) > 1 ? 2 : 1; // need to put nodes connected with
                // > 1 edge further away
                lmap.put(n2, max(lmap.get(n1) + inc, lmap.get(n2)));
                h = max(h, lmap.get(n2) + 1);
            }
        }
        stack.init(h, sorted.size());

        for (QueryNode n : sorted) {
            stack.add(n, graph.outDegree(n) == 0 ? h-1 : lmap.get(n));
        }
    }              /*
    void splitIntoLayers() {
        BFSDistanceLabeler<Object, String> bfs = new BFSDistanceLabeler<Object, String>();
        bfs.labelDistances(graph, sources());

        Map<Object, Number> lmap = bfs.getDistanceDecorator();
        List<Object> sorted = bfs.getVerticesInOrderVisited();
        Number h = lmap.get(sorted.get(sorted.size() - 1));
        stack.init(h.intValue() + 1, graph.getVertices().size());
        for (Object n : sorted) {
            stack.add(n, lmap.get(n).intValue());
        }
    }
                       */

    void insertDummies() {
        for (String currEdge : new ArrayList<String>(graph.getEdges())) {
            QueryNode first = graph.getSource(currEdge);
            int fromLayer = stack.getLayer(first);
            QueryNode second = graph.getDest(currEdge);
            int toLayer = stack.getLayer(second);
            if (toLayer - fromLayer > 1) {
                graph.removeEdge(currEdge);
                for (int layer = fromLayer + 1; layer < toLayer; layer++) {
                    QueryNode v = new QueryNode("virtual", null, null, String.valueOf(-virtual));
                    String virtualEdge = "v" + virtual;
                    virtual++;
                    graph.addEdge(virtualEdge, first, v, EdgeType.UNDIRECTED);
                    first = v;
                    stack.add(v, layer);
                    /*Bend b = new Bend();
                    currEdge.add(b);
                    stack.add(b, layer);*/
                }
                graph.addEdge(currEdge, first, second, EdgeType.DIRECTED);
            }
        }
    }

    List<QueryNode> sources() {
        List<QueryNode> sources = new LinkedList<QueryNode>();
        for (QueryNode n : graph.getVertices()) {
            if (graph.inDegree(n) == 0) {
                sources.add(n);
            }
        }
        return sources;
    }


    List<QueryNode> topologicalSort() {
        List<QueryNode> q = sources();
        List<QueryNode> l = new ArrayList<QueryNode>(this.graph.getVertices().size());
        List<String> r = new ArrayList<String>(this.graph.getEdges().size()); // removed
        // edges
        while (q.size() > 0) {
            QueryNode n = q.remove(0);
            l.add(n);
            for (String e : graph.getOutEdges(n)) {
                QueryNode m = graph.getDest(e);
                r.add(e); // removing edge from the graph
                boolean allEdgesRemoved = true;
                // then checking if the target has any more "in" edges left
                for (String e2 : graph.getInEdges(m)) {
                    if (!r.contains(e2)) {
                        allEdgesRemoved = false;
                    }
                }
                if (allEdgesRemoved) {
                    q.add(m);
                }
            }
        }
        if (getGraph().getVertices().size() != l.size()) {
            throw new RuntimeException("Topological sort failed for " + graph + " in Sugiyama layout, " + graph.getVertices().size()
                    + " total nodes, " + l.size() + ", sorted nodes, remaining nodes: " + q);
        }
        return l;
    }


    public synchronized void initialize() {
        splitIntoLayers();
        insertDummies();
        stack.initIndexes();
        stack.reduceCrossings();
        stack.layerHeights();
        stack.xPos();
    }

    @Override
    public Dimension getSize() {
        return stack.getSize();
    }

    public void reset() {

    }
}
