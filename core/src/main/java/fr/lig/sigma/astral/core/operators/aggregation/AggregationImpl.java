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

package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.types.ValueComparator;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */

@Component
@Provides
public class AggregationImpl extends RelationOperator implements Operator, Relation {
    @Requires(id = "in")
    private Relation in;

    @Requires(id = "core")
    private AstralCore core;
    @Property(mandatory = true)
    private List<Map<String, String>> aggregate;
    @Property
    private List<String> groupBy;
    @Property(mandatory = true)
    private List<String> attributes;

    private static final Logger log = Logger.getLogger(AggregationImpl.class);

    private Pane pane;
    @Property
    private Map<String, String> structure;

    public void prepare() {
        Set<String> attributes = new AttributeSet(this.attributes);
        pane = new Pane(core.getAf(), aggregate, groupBy);

        // TODO : complete name
        setOutput(createNewFrom(in, attributes, groupBy + "G", structure));
        addInput(in, true);
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        TupleSet ts = in.getContent(b);
        long t = System.currentTimeMillis();
        TupleSet result = computeAggregation(ts);
        output.update(result, b);

        t = System.currentTimeMillis() - t;
        if (log.isTraceEnabled())
            log.trace(t + "ms to compute agg with " + ts.size() + " tuples (out: " + result.size() + " tuples)");
    }

    /**
     * Build the TupleSet with the aggregated results based on a correctly sorted array of tuple
     *
     * @param input The sorted array corresponding to the group by expression
     * @return The TS
     */
    private TupleSet computeAggregation(TupleSet input) {
        TupleSet result = entityFactory.instanciateTupleSet(getAttributes());
        for (Tuple t : input) {
            pane.put(t);
        }
        pane.fillResult(result);
        pane.reset();
        return result;
    }


    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public String toString() {
        return "Aggregation";
    }
}
