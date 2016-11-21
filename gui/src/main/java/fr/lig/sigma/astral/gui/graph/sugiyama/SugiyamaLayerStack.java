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

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

/**
 * A stack of layers for the Sugiyama layout
 *
 * @author avishnyakov
 */
public class SugiyamaLayerStack<E> {
    private Graph<E, ?> g;
    private Layout<E, ?> layout;
    private Transformer<E, String> labelTransformer;


    static final int MAX_SWEEPS = 100;
    static final double X_SEP = 100;
    static final double Y_SEP = 25;

    Logger log = Logger.getLogger(getClass());

    List<List<Node<E>>> layers;
    Map<Node<E>, Integer> nodeMap;
    Map<E, Node<E>> vertexToNode;

    public SugiyamaLayerStack(Graph<E, ?> g, Layout<E, ?> layout, Transformer<E, String> labelTransformer) {
        this.g = g;
        this.layout = layout;
        this.labelTransformer = labelTransformer;
    }

    void add(E n1, int layerIndex) {
        Node<E> n = new Node<E>(n1,layout, labelTransformer);
        layers.get(layerIndex).add(n);
        nodeMap.put(n, layerIndex);
        vertexToNode.put(n1,n);
    }

    double avgX(List<Node<E>> ln) {
        double m = 0d;
        for (Node<E> n : ln) {
            m += n.getCtrPos().x;
        }
        return m / ln.size();
    }

    int barycenter(List<Node<E>> ln) {
        if (ln.size() == 0) {
            return 0;
        } else {
            double bc = 0;
            for (Node<E> n : ln) {
                bc += n.getIndex();
            }
            return (int) round(bc / ln.size());
        }
    }

    List<Node<E>> getConnectedTo(Node<E> n1, int layerIndex) {
        List<Node<E>> ln = new LinkedList<Node<E>>();
        if (layerIndex < layers.size() && layerIndex >= 0) {
            for (Node<E> n2 : layers.get(layerIndex)) {
                if (g.findEdge(n1.getVertex(), n2.getVertex()) != null ||
                        g.findEdge(n2.getVertex(), n1.getVertex()) != null)
                    ln.add(n2);
            }
        }
        return ln;
    }

    int getLayer(E n) {
        Integer l = nodeMap.get(vertexToNode.get(n));
        if (l == null) {
            return 0;
        } else {
            return l;
        }
    }

    void init(int height, int nodeQty) {
        vertexToNode = new HashMap<E,Node<E>>();
        layers = new ArrayList<List<Node<E>>>(height);
        for (int i = 0; i < height; i++) {
            layers.add(new ArrayList<Node<E>>(nodeQty / height + 1));
        }
        nodeMap = new HashMap<Node<E>, Integer>(nodeQty);
    }

    void initIndexes() {
        for (List<Node<E>> l : layers) {
            Collections.sort(l, new Comparator<Node<E>>() {
                public int compare(Node<E> n1, Node<E> n2) {
                    return n1.getIndex() - n2.getIndex();
                }
            });
            setOrderedIndexes(l);
        }
    }

    void layerHeights() {
        double offset = 0d;
        for (int l = 0; l < layers.size(); l++) {
            List<Node<E>> ln = layers.get(l);
            double maxh = maxHeight(ln);
            for (Node<E> n : ln) {
                if (n.isVirtual()) {
                    n.setPos(n.getPos().x, offset + maxHeight(ln) / 2d);
                } else {
                    n.setPos(n.getPos().x, offset);
                }
            }
            offset += maxh + Y_SEP;
        }
    }

    double maxHeight(Collection<Node<E>> ln) {
        double mh = 0d;
        for (Node<E> n : ln) {
            mh = max(mh, n.getSize().y);
        }
        return mh;
    }

    Dimension getSize() {
        double mh = 0d;
        double mw = 0d;
        for (Node<E> n : nodeMap.keySet()) {
            mw = max(mw, n.getPos().x+n.getSize().x);
            mh = max(mh, n.getPos().y+n.getSize().y);
        }
        return new Dimension((int)ceil(mw),(int)ceil(mh));
    }

    void reduceCrossings() {
        for (int round = 0; round < MAX_SWEEPS; round++) {
            if (round % 2 == 0) {
                for (int l = 0; l < layers.size() - 1; l++) {
                    reduceCrossings2L(l, l + 1);
                }
            } else {
                for (int l = layers.size() - 1; l > 0; l--) {
                    reduceCrossings2L(l, l - 1);
                }
            }
        }
    }

    void reduceCrossings2L(int staticIndex, int flexIndex) {
        final List<Node<E>> flex = layers.get(flexIndex);
        for (Node<E> n : flex) {
            List<Node<E>> neighbors = getConnectedTo(n, staticIndex);
            n.setIndex(barycenter(neighbors));
        }
        Collections.sort(flex, new Comparator<Node<E>>() {
            public int compare(Node<E> n1, Node<E> n2) {
                return n1.getIndex() - n2.getIndex();
            }
        });
        setOrderedIndexes(flex);
    }

    void setOrderedIndexes(List<Node<E>> ln) {
        for (int i = 0; i < ln.size(); i++) {
            ln.get(i).setIndex(i);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getClass().getSimpleName());
        sb.append("(").append(layers.size()).append(")");
        int lc = 0;
        for (List<Node<E>> l : layers) {
            sb.append("\n\t").append(lc++).append(" # ").append(l);
        }
        return sb.toString();
    }

    void xPos() {
        for (int l = 0; l < layers.size(); l++) {
            xPosPack(l);
        }
        for (int l = 0; l < layers.size() - 1; l++) {
            xPosDown(l, l + 1);
        }
        for (int l = layers.size() - 1; l > 0; l--) {
            xPosUp(l, l - 1);
        }
    }

    void xPosDown(int staticIndex, int flexIndex) {
        List<Node<E>> flex = layers.get(flexIndex);
        for (int i = 0; i < flex.size(); i++) {
            Node<E> n = flex.get(i);
            double min = i > 0 ? flex.get(i - 1).getPos().x + flex.get(i - 1).getSize().x + X_SEP : -Double.MAX_VALUE;
            List<Node<E>> neighbors = getConnectedTo(n, staticIndex);
            double avg = avgX(neighbors);
            if (!Double.isNaN(avg)) {
                n.setPos(max(min, avg - n.getSize().x / 2d), n.getPos().y);
            }
        }
    }

    void xPosPack(int flexIndex) {
        List<Node<E>> flex = layers.get(flexIndex);
        double offset = 0d;
        for (Node<E> n : flex) {
            n.setPos(offset, n.getPos().y);
            offset = n.getPos().x + n.getSize().x + X_SEP;
        }
    }

    void xPosUp(int staticIndex, int flexIndex) {
        List<Node<E>> flex = layers.get(flexIndex);
        for (int i = flex.size() - 1; i > -1; i--) {
            Node<E> n = flex.get(i);
            double min = i > 0 ? flex.get(i - 1).getPos().x + flex.get(i - 1).getSize().x + X_SEP : -Double.MAX_VALUE;
            double max = i < flex.size() - 1 ? flex.get(i + 1).getPos().x - n.getSize().x - X_SEP : Double.MAX_VALUE;
            List<Node<E>> neighbors = getConnectedTo(n, staticIndex);
            double avg = avgX(neighbors);
            if (!Double.isNaN(avg)) {
                n.setPos(max(min, min(max, avg - n.getSize().x / 2d)), n.getPos().y);
            }
        }
    }

}

