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
 * @author Loic Petit
 */
@Component
@Provides(strategy = "INSTANCE", properties = {
        @StaticServiceProperty(name= AggregateFactory.NAME_PROPERTY, type="java.lang.String", value="first")
})
public class FirstAggregate implements HolisticAggregateFunction<FirstAggregate> {
    private Comparable value = null;

    private String attribute;

    public FirstAggregate(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public void addValue(Tuple t) {
        if (this.value == null)
            this.value = t.get(attribute);
    }

    @Override
    public Comparable aggregate() {
        return value;
    }

    @Override
    public String toString() {
        return "first" + "(" + attribute + ")";
    }

    @Override
    public void mergeAggregate(FirstAggregate agg) {
        if (value == null)
            value = agg.value;
    }
}
