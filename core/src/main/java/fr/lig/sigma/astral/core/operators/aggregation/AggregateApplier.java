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

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFunction;
import fr.lig.sigma.astral.operators.aggregation.HolisticAggregateFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class AggregateApplier {
    private String to;
    private String function;
    private AggregateFactory af;

    private Map<List<Comparable>, AggregateFunction> aggregateTable = new HashMap<List<Comparable>, AggregateFunction>();

    public AggregateApplier(String to, String function, AggregateFactory af) {
        this.to = to;
        this.function = function;
        this.af = af;
    }

    public void putTuple(List<Comparable> values, Tuple t) {
        AggregateFunction f = aggregateTable.get(values);
        if (f == null) {
            f = af.get(function);
            aggregateTable.put(values, f);
        }
        f.addValue(t);
    }

    public Set<List<Comparable>> getGroupByValues() {
        return aggregateTable.keySet();
    }


    public void reset() {
        aggregateTable.clear();
    }

    public Tuple addAggregateAttribute(List<Comparable> values, Tuple t) {
        t.put(to, fetchAggregateAttribute(values));
        return t;
    }

    public Comparable fetchAggregateAttribute(List<Comparable> values) {
        return aggregateTable.get(values).aggregate();
    }

    public void mergeTables(AggregateApplier next) {
        for (Map.Entry<List<Comparable>, AggregateFunction> entry : next.aggregateTable.entrySet()) {
            HolisticAggregateFunction func = (HolisticAggregateFunction) aggregateTable.get(entry.getKey());
            if (func == null) {
                func = (HolisticAggregateFunction) af.get(function);
                aggregateTable.put(entry.getKey(), func);
            }
            func.mergeAggregate((HolisticAggregateFunction) entry.getValue());
        }
    }
}
