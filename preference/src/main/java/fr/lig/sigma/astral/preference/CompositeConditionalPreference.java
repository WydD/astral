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
import fr.lig.sigma.astral.query.KnowledgeBase;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class CompositeConditionalPreference implements PartialComparator<Tuple> {
    //private KnowledgeBase kb;
    private Set<PartialComparator<Tuple>> comparators = new HashSet<PartialComparator<Tuple>>();
    //private KnowledgeBase originalKB;
    private static final Logger log = Logger.getLogger(CompositeConditionalPreference.class);
        /*
    public CompositeConditionalPreference(KnowledgeBase originalKB) {
        this.originalKB = originalKB;
    }     */
                        /*
    private void initKB() throws Exception {
        kb = originalKB.createOtherKB(false);
        kb.putKnowledge("max(C,Z):- Z=java.util.Collections.max(C).\n" +
                "source(E):- pref(Z,E), !, 1=0.\n" +
                "source(E).\n" +
                "dominated(E):- pref(Z,E).\n" +
                "dominated(E1, E2):- pref(E1,E2).\n" +
                "dominated(E1, E2):- pref(E1,Inter), dominated(Inter,E2).\n" +
                "level(E, Z):- pref(Inter,E), cache(levelmax(Inter,Lvl)), Z=Lvl+1.\n" +
                "level(E, 0):- source(E).\n" +
                "levelmax(E, Z):- findall(X,level(E,X),L),max(L,Z).\n");
    }                     */

    public void addComparator(PartialComparator<Tuple> comparator) {
        comparators.add(comparator);
    }

    public void setContent(Set<String> ts) {
        for (PartialComparator<Tuple> comp : comparators)
            comp.setContent(ts);
       /* String graph = "";
        List<Tuple> buffer = new LinkedList<Tuple>();

        for (Tuple t1 : ts) {
            for (Tuple t2 : buffer) {
                graph += computeRelation(t1, t2);
            }
            buffer.add(t1);
        }
        log.info(graph);
        try {
            initKB();
            kb.putKnowledge(graph);
        } catch (Exception e) {
            // Should never happen
            throw new RuntimeException("Cannot initialize tuple graph", e);
        }*/
    }
                                /*
    private String computeRelation(Tuple tuple, Tuple tuple1) {
        Integer res = nonTransitiveCompare(tuple, tuple1);
        if (res != null) {
            String graph = "pref(";
            if (res < 0)
                graph += tuple.getId() + "," + tuple1.getId();
            else
                graph += tuple1.getId() + "," + tuple.getId();
            graph += ").\n";
            return graph;
        }
        return "";
    }

    public boolean isDominated(Tuple t) {
        try {
            Map<String, Object> res = kb.consult(":- solve(dominated(" + t.getId() + ")).");
            return res != null;
        } catch (Exception e) {
            // Should never happen
            throw new RuntimeException("Cannot compute tuple graph", e);
        }
    }
                 */
    public Integer compare(Tuple e1, Tuple e2) {
        for (PartialComparator<Tuple> comp : comparators) {
            Integer res = comp.compare(e1, e2);
            if (res != null)
                return res;
        }
        return null;
    }
                      /*

    public Integer compare(Tuple e1, Tuple e2) {
        try {
            Map<String, Object> res = kb.consult(":- solve(dominated(" + e1.getId() + "," + e2.getId() + ")).");
            if (res != null)
                return -1;

            res = kb.consult(":- solve(dominated(" + e2.getId() + "," + e1.getId() + ")).");
            if (res != null)
                return 1;
            return null;
        } catch (Exception e) {
            // Should never happen
            throw new RuntimeException("Cannot compute tuple graph", e);
        }
    }

    public int level(Tuple e1) {
        try {
            Map<String, Object> res = kb.consult(":- solve(cache(levelmax(" + e1.getId() + ",Z))).");
            return Integer.parseInt(res.get("Z").toString());
        } catch (Exception e) {
            // Should never happen
            throw new RuntimeException("Cannot compute tuple graph", e);
        }
    }  */
}
