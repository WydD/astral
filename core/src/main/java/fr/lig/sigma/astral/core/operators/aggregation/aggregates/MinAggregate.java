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
        @StaticServiceProperty(name= AggregateFactory.NAME_PROPERTY, type="java.lang.String", value="min")
})
public class MinAggregate implements HolisticAggregateFunction<MinAggregate> {
    private Comparable min;
    private String attribute;

    public MinAggregate(String attribute) {
        this.attribute = attribute;
    }


    @SuppressWarnings({"unchecked"})
    @Override
    public void addValue(Tuple t) {
        Comparable value = t.get(attribute);
        if (this.min == null) {
            min = value;
            return;
        }
        if (value.compareTo(this.min) < 0)
            min = value;
    }

    @Override
    public Comparable aggregate() {
        return min;
    }

    public String toString() {
        return "min" + "(" + attribute + ")";
    }

    @Override
    public void mergeAggregate(MinAggregate agg) {
        if (min == null || agg.min != null && agg.min.compareTo(min) < 0)
            min = agg.min;
    }
}
