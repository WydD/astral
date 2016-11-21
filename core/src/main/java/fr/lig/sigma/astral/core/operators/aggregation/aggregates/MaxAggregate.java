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

/**
 *
 */
@Component
@Provides(strategy = "INSTANCE", properties = {
        @StaticServiceProperty(name= AggregateFactory.NAME_PROPERTY, type="java.lang.String", value="max")
})
public class MaxAggregate implements HolisticAggregateFunction<MaxAggregate> {
    private Comparable max;
    private String attribute;

    public MaxAggregate(String attribute) {
        this.attribute = attribute;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void addValue(Tuple t) {
        Comparable value = t.get(attribute);
        if (this.max == null) {
            max = value;
            return;
        }
        if (value.compareTo(this.max) > 0)
            max = value;
    }

    @Override
    public Comparable aggregate() {
        return max;
    }

    public String toString() {
        return "max" + "(" + attribute + ")";
    }

    @Override
    public void mergeAggregate(MaxAggregate agg) {
        if (max == null || agg.max != null && agg.max.compareTo(max) > 0)
            max = agg.max;
    }
}
