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

package fr.lig.sigma.astral.preference;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Loic Petit
 */
public class DominationGraph {
    private Map<Tuple, Set<Tuple>> graph = null;
    private Map<Tuple, Set<Tuple>> prec = null;
    private Set<Tuple> sources = new HashSet<Tuple>();
    private PartialComparator<Tuple> comparator;
    private int edgeCount = 0;
    private static final Logger log = Logger.getLogger(DominationGraph.class);

    public DominationGraph(PartialComparator<Tuple> comparator) {
        this(comparator, false);
    }

    public DominationGraph(PartialComparator<Tuple> comparator, boolean noGraph) {
        this.comparator = comparator;
        if (!noGraph) {
            graph = new HashMap<Tuple, Set<Tuple>>();
            prec = new HashMap<Tuple, Set<Tuple>>();
        }
    }

    public int getSourceCount() {
        return sources.size();
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getNodeCount() {
        if (prec != null)
            return sources.size() + prec.size();
        return 0;
    }

    public int getIsolatedNodeCount() {
        int c = 0;
        if (graph == null) return c;
        for (Tuple t : sources)
            if (!graph.containsKey(t))
                c++;
        return c;
    }


    private void addEdge(Tuple t, Tuple child) {
        edgeCount++;
        // Build the edges
        addSingleEdge(t, child, graph);
        addSingleEdge(child, t, prec);
    }

    public void removeNode(Tuple t) {
        Set<Tuple> dominated = graph.remove(t);
        Set<Tuple> dominator = prec.remove(t);
        sources.remove(t);
        removeSingleEdge(t, dominated, prec, true);
        removeSingleEdge(t, dominator, graph, false);
    }

    private void removeSingleEdge(Tuple from, Set<Tuple> to, Map<Tuple, Set<Tuple>> graph, boolean sourceOnEmpty) {
        if (to == null) return;
        for (Tuple tp : to) {
            edgeCount--;
            Set<Tuple> anc = graph.get(tp);
            if (anc.size() == 1) {
                if (sourceOnEmpty)
                    sources.add(tp);
                graph.remove(tp);
            } else {
                anc.remove(from);
            }
        }
    }

    public void addNode(Tuple t) {
        List<Tuple> input = new ArrayList<Tuple>(prec.size() + sources.size());
        input.addAll(prec.keySet());
        input.addAll(sources);
        sources.add(t);
        for (Tuple in : input)
            compareAndAddEdge(t, in);
    }

    private void addSingleEdge(Tuple t, Tuple child, Map<Tuple, Set<Tuple>> graph) {
        Set<Tuple> list = graph.get(t);
        if (list == null) {
            list = new HashSet<Tuple>();
            graph.put(t, list);
        }
        list.add(child);
    }


    public void buildFrom(TupleSet input) {
        if (graph == null) {
            Set<Tuple> duplicateSource = new HashSet<Tuple>();
            for (Tuple t : input) {
                sources.add(t);
                duplicateSource.add(t);
                for (Tuple tp : duplicateSource) {
                    compareAndAddEdge(t, tp);
                }
                if (!sources.contains(t)) duplicateSource.remove(t);
            }
            return;
        }
        List<Tuple> buffer = new ArrayList<Tuple>(input.size());
        for (Tuple t : input) {
            sources.add(t);
            for (Tuple tp : buffer) {
                compareAndAddEdge(t, tp);
            }
            buffer.add(t);
        }
        buffer.clear();
    }

    public void compareAndAddEdge(Tuple t, Tuple tp) {
        if (t.getId() == tp.getId()) return; // Hey!
        // Comparison in pairs of all tuples
        Integer res = comparator.compare(t, tp);
        if (res == null) return;
        // t < tp
        if (res < 0) {
            sources.remove(t);
            if (graph != null)
                addEdge(tp, t);
        } else {
            // tp > t
            sources.remove(tp);
            if (graph != null)
                addEdge(t, tp);
        }
    }

    public void fillSources(TupleSet result) {
        for (Tuple t : sources)
            result.add(t);
    }

    public void fillSources(TupleSet result, int K) {
        K = sources.size() - K;
        for (Tuple t : sources) {
            if (K == 0)
                result.add(t);
            else
                K--;
        }
    }


    public void fillTopK(TupleSet result, int K) {
        if (K <= sources.size()) {
            fillSources(result, K);
            return;
        }

        int level = 0;
        HashMap<Tuple, Integer> precCount = new HashMap<Tuple, Integer>();
        int id = 0;
        // Contains the next level of scan, as it MUST be ordered, it's a TreeSet
        Set<Tuple> nextLevel = new TreeSet<Tuple>();
        // Initialize the scan
        for (Tuple t : sources) {
            nextLevel.add(t);
        }
        int inSize = sources.size() + prec.size();
        LinkedList<Tuple> buffer = new LinkedList<Tuple>();
        // While we still need tuples in the result
        while (id < K && id < inSize) {
            // When there is no more to scan on this level, load the next level
            if (buffer.isEmpty()) {
                if (nextLevel.isEmpty()) break;
                level++;
                for (Tuple t : nextLevel)
                    buffer.add(t);
                nextLevel.clear();
            }

            Tuple t = buffer.poll();
            // Get the descending tuples
            Set<Tuple> childs = graph.get(t);

            if (childs != null) {
                for (Tuple next : childs) {
                    Integer value = precCount.get(next);
                    if (value == null) {
                        value = prec.get(next).size();
                    }
                    if (value == 1) {
                        precCount.remove(next);
                        nextLevel.add(next);
                    } else
                        // Here the preceding count is too high, we must wait for value-1 visits
                        precCount.put(next, value - 1);
                }
            }
            // The tuple is now appended to the result with a phyid change as it was reordered
            result.add(new Tuple(t, id++));
        }
        log.debug("Browsed until level " + level + " to retrieve " + id + " data");
    }

    @Override
    public String toString() {
        return "DominationGraph{" +
                "sources=" + sources +
                ", prec=" + prec +
                ", graph=" + graph +
                '}';
    }
}
