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

package fr.lig.sigma.astral.core.operators.aggregation.aggregates;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.aggregation.HolisticAggregateFunction;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides(strategy = "INSTANCE", properties = {
        @StaticServiceProperty(name= AggregateFactory.NAME_PROPERTY, type="java.lang.String", value="countd")
})
public class CountDistinctAggregate implements HolisticAggregateFunction<CountDistinctAggregate> {
    private Set<Comparable> valueSet = new HashSet<Comparable>();

    private String attribute;

    public CountDistinctAggregate(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public void addValue(Tuple t) {
        valueSet.add(t.get(attribute));
    }

    @Override
    public Comparable aggregate() {
        int res = valueSet.size();
        valueSet.clear();
        return res;
    }

    @Override
    public String toString() {
        return "countd" + "(" + attribute + ")";
    }

    @Override
    public void mergeAggregate(CountDistinctAggregate agg) {
        valueSet.addAll(agg.valueSet);
    }
}
