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

package fr.lig.sigma.astral.core.operators.window;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.DynamicRelationOperator;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class LastBatchPartitionedWindowImpl extends DynamicRelationOperator implements Operator {

    @Requires(id = "in")
    private Stream in;
    @Property(mandatory = true)
    private List<String> partitionedBy;
    @Property(mandatory = true)
    private WindowDescription description;
    @Property
    private Map<String,String> structure;
    private List<String> attributes;

    private Map<List<Comparable>, List<Tuple>> tuples = new HashMap<List<Comparable>, List<Tuple>>();
    private Map<List<Comparable>, Batch> batches = new HashMap<List<Comparable>, Batch>();

    private Map<List<Comparable>, List<Tuple>> commit = new HashMap<List<Comparable>, List<Tuple>>();

    private final EventProcessor[] waitFor = {this};
    private EventProcessor processor = new EventProcessor() {
        Batch lastTimestamp = Batch.MIN_VALUE;

        @Override
        public void processEvent(Batch timestamp) throws AxiomNotVerifiedException {
            if (lastTimestamp.equals(timestamp)) return;
            lastTimestamp = timestamp;
            log.trace("Partition union called: " + timestamp);
            /*UnionRelation.union(timestamp ,null, output, entityFactory, true);     */
            TupleSet is = entityFactory.instanciateTupleSet(getAttributes());
            TupleSet ds = entityFactory.instanciateTupleSet(getAttributes());
            for (Map.Entry<List<Comparable>, List<Tuple>> entry : commit.entrySet()) {
                List<Tuple> oldTuples = tuples.get(entry.getKey());
                if (oldTuples != null) {
                    for (Tuple t : oldTuples)
                        ds.add(t);
                    oldTuples.clear();
                } else {
                    oldTuples = new ArrayList<Tuple>(1);
                    tuples.put(entry.getKey(), oldTuples);
                }
                for (Tuple t : entry.getValue()) {
                    is.add(t);
                    oldTuples.add(t);
                }
            }
            commit.clear();

            update(timestamp, is, ds);
        }

        @Override
        public EventProcessor[] waitFor() {
            return waitFor;
        }
    };

    private boolean oneTupleOnly;
    private static Logger log = Logger.getLogger(LastBatchPartitionedWindowImpl.class);

    @Override
    public void processEvent(Batch timestamp) throws AxiomNotVerifiedException {
        while (in.hasNext(timestamp)) {
            Tuple t = in.pop();
            List<Comparable> values = new ArrayList<Comparable>(attributes.size());
            for (String attribute : attributes) {
                values.add(t.get(attribute));
            }

            List<Tuple> dest = commit.get(values);
            if (dest == null) {
                dest = new ArrayList<Tuple>(1);
                commit.put(values, dest);
            }
            if (!oneTupleOnly) {
                Batch batch = batches.get(values);
                if (batch == null)
                    batch = timestamp;
                batches.put(values, timestamp);
                if (!timestamp.equals(batch))
                    dest.clear();
            } else
                dest.clear();
            dest.add(t);
        }
        getScheduler().pushEvent(timestamp, processor);
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public void prepare() throws Exception {
        String attributes[] = partitionedBy.toArray(new String[partitionedBy.size()]);
        List<String> attrList = Arrays.asList(attributes);
        if (!in.getAttributes().containsAll(attrList))
            throw new IllegalStateException("Partitioning attribute(s) is(are) not in the attribute set");
        this.attributes = attrList;

        oneTupleOnly = description instanceof LinearPositionalDescription;

        setOutput((DynamicRelation) entityFactory.instanciateEntity(
                "NativeDynamicRelation",
                in.getName() + "[" + description + "]",
                in.getAttributes(),
                new Hashtable<String, Object>(structure))
        );
        addInput(in, true);
    }


    @Override
    public String toString() {
        return "Partitioned Window";
    }
}
