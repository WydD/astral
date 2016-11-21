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

package fr.lig.sigma.astral.core.operators.join;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Couple;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.containers.AbstractEntityContainer;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of the basic relation join as defined inside the relational algebra
 *
 * @author Loic Petit
 */
@Component
@Provides
public class RelationalJoin extends RelationOperator implements Operator, Relation {
    private Relation left;
    private Relation right;
    private Condition optional;
    private Set<String> joinAttributes;
    private Set<String> otherAttributes;
    private AttributeSet outerAttributes;
    private AttributeSet leftOuterAttributes;


    private static Logger log = Logger.getLogger(RelationalJoin.class);

    @Property
    private String condition;

    @Property
    private Comparable defaultOuter;
    @Property
    private String outer;


    @Requires(id = "in", policy = "dynamic-priority")
    private Relation[] in;

    @Requires(id = "engine")
    private AstralEngine engine;
    private int comparisons;
    private int outerCount;
    @Property
    private Map<String, String> structure;


    public int getMaxInputs() {
        return 2;
    }

    private Batch preventSimultaneous = null;

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        if (preventSimultaneous == b) return;
        preventSimultaneous = b;

        // This part ensures that the inputs are ordered
        TupleSet leftContent = entityFactory.ensureOrderedTupleSet(left.getContent(b));
        TupleSet rightContent = entityFactory.ensureOrderedTupleSet(right.getContent(b));

        TupleSet result = entityFactory.instanciateTupleSet(getAttributes(), true);
        if (outer == null && (leftContent.size() == 0 || rightContent.size() == 0)) {
            if (log.isTraceEnabled()) {
                log.trace(b + ": Join (on " + joinAttributes + ") computed with a size of " + result.size() +
                        " (comparisons: none, left: " + leftContent.size() + ", right: " + rightContent.size() + ") ");
            }
            // Empty join !
            output.update(result, b);
            return;
        }

        comparisons = 0;
        outerCount = 0;

        if (!joinAttributes.isEmpty())
            // Compute the right hash table to quickly join with the right part
            rightContent.addIndex(joinAttributes);
        computeFullJoin(b, leftContent, rightContent, result);


        //hashTableRight.clear();
    }


    private Map<List<Comparable>, List<Tuple>> producedTuples = new HashMap<List<Comparable>, List<Tuple>>();

    private void computeIterativeJoin(Batch b, TupleSet is, TupleSet ds, TupleSet rightContent, TupleSet result) {
        int comparisons = 0;
        int outer = 0;
        for (Tuple l : is) {
            Collection<Tuple> tuples = rightContent.fetchTupleFromValue(joinAttributes, l);
            if (tuples == null) {
                outer = prepareOuter(result, outer, l, true);
                continue;
            }
            for (Tuple r : tuples) {
                comparisons++;
                Couple id = new Couple(l.getId(), r.getId());
                Tuple res = compareAndBuildTuple(l, r, id);
                if (res != null) {
                    fillTuple(l, r, res, otherAttributes);
                    result.add(res);
                } else {
                    outer = prepareOuter(result, outer, l, true);
                }
            }
        }
    }

    private void computeFullJoin(Batch b, TupleSet leftContent, TupleSet rightContent, TupleSet result) {
        // The join iterates over the left relation then on the right and a new id is built
        // As described in the AStrAL spec, we loose the symmetric property of the relational join
        for (Tuple l : leftContent) {
            Collection<Tuple> tuples = rightContent.fetchTupleFromValue(joinAttributes, l);
            if (tuples == null) {
                outerCount = prepareOuter(result, outerCount, l, true);
                continue;
            }
            boolean found = false;
            for (Tuple r : tuples) {
                comparisons++;
                Couple id = new Couple(l.getId(), r.getId());
                Tuple res = compareAndBuildTuple(l, r, id);
                if (res != null) {
                    fillTuple(l, r, res, otherAttributes);
                    result.add(res);
                    found = true;
                }
            }
            if (!found)
                outerCount = prepareOuter(result, outerCount, l, true);
        }
        if ("full".equals(outer)) {
            leftContent.addIndex(joinAttributes);
            for (Tuple l : rightContent) {
                Collection<Tuple> tuples = leftContent.fetchTupleFromValue(joinAttributes, l);
                if (tuples == null) {
                    outerCount = prepareOuter(result, outerCount, l, false);
                    continue;
                }
                if (optional == null)
                    continue;
                boolean found = false;
                Couple id = new Couple(0, 0);
                for (Tuple r : tuples) {
                    comparisons++;
                    found |= compareAndBuildTuple(l, r, id) != null;
                }
                if (!found)
                    outerCount = prepareOuter(result, outerCount, l, false);
            }
        }
        // Considering the construction of id and the order of the left and right tuple sets the result has a
        // consistent order considering axiom 3.1
        output.update(result, b);
        if (log.isTraceEnabled()) {
            log.trace(b + ": Join (on " + joinAttributes + ") computed with a size of " + result.size() +
                    " (comparisons: " + comparisons + ", " +
                    (outer != null ? "outer: " + outerCount + ", " : "") +
                    "left: " + leftContent.size() + ", " +
                    "right: " + rightContent.size() + ")");
        }
    }

    private int prepareOuter(TupleSet result, int outer, Tuple l, boolean left) {
        if (this.outer == null) return outer;
        outer++;
        Couple id;
        AttributeSet attr;
        if (left) {
            attr = outerAttributes;
            id = new Couple(l.getId(), 0);
        } else {
            attr = leftOuterAttributes;
            id = new Couple(0, l.getId());
        }
        Tuple res = new Tuple(l, id);
        for (String t : attr)
            if (!Tuple.PHYSICAL_ID.equals(t)) {
                res.put(t, defaultOuter);
            }
        result.add(res);
        return outer;
    }

    private Tuple compareAndBuildTuple(Tuple l, Tuple r, Comparable id) {
        Tuple t = new Tuple(id);
        for (String attrib : joinAttributes) {
            Comparable left = l.get(attrib);
            if (!left.equals(r.get(attrib))) {
                //log.trace(left+" is not equals to "+r.get(attrib));
                return null;
            }
            t.put(attrib, left);
        }
        if (optional != null) {
            fillTuple(l, r, t, optional.getConcernedAttributes());
            if (!optional.evaluate(t))
                return null;
        }
        return t;
    }

    private void fillTuple(Map<String, Comparable> l, Map<String, Comparable> r, Tuple t, Set<String> attributes) {
        for (String attrib : attributes) {
            Comparable left = l.get(attrib);
            if (!Tuple.PHYSICAL_ID.equals(attrib))
                t.put(attrib, left != null ? l.get(attrib) : r.get(attrib));
        }
    }

    public void prepare() throws Exception {
        log.trace(Arrays.toString(in));
        left = in[0];
        right = in[1];
        joinAttributes = new AttributeSet(left.getAttributes());
        joinAttributes.retainAll(right.getAttributes());
        joinAttributes.remove(Tuple.PHYSICAL_ID); // DO NOT JOIN ON PHYSICAL
        // In join attribute we have everything

        Set<String> attrib = new AttributeSet(left.getAttributes());
        attrib.addAll(right.getAttributes());

        otherAttributes = new AttributeSet(attrib);
        otherAttributes.removeAll(joinAttributes);

        if (optional != null)
            otherAttributes.removeAll(optional.getConcernedAttributes());


        if (condition != null && !condition.isEmpty())
            this.optional = engine.getGlobalEA().buildCondition(condition);

        if (optional != null && !attrib.containsAll(optional.getConcernedAttributes()))
            throw new WrongAttributeException("Condition " + optional + " has attributes that does not match " + attrib + " / " + optional.getConcernedAttributes());
        if (defaultOuter != null && outer == null) outer = "left";

        if (outer != null) {
            if (defaultOuter == null) defaultOuter = Tuple.NULL_VALUE;

            outerAttributes = new AttributeSet(right.getAttributes());
            outerAttributes.removeAll(joinAttributes);
            if ("full".equals(outer)) {
                leftOuterAttributes = new AttributeSet(left.getAttributes());
                leftOuterAttributes.removeAll(joinAttributes);
            }
            try {
                if (defaultOuter instanceof String)
                    defaultOuter = Long.parseLong((String) defaultOuter);
            } catch (Exception ignored) {
            }
            try {
                if (defaultOuter instanceof String)
                    defaultOuter = Double.parseDouble((String) defaultOuter);
            } catch (Exception ignored) {
            }

        }

        setOutput(createNewFrom(left, attrib, left.getName() + "\\Join" + (optional != null ? "_{" + optional + "}" : " ") + right.getName(),structure));
        addInput(left, true);
        addInput(right, true);
    }

    @Override
    public String toString() {
        return "Join";
    }
}
