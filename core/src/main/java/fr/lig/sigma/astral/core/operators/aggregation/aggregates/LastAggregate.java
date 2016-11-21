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
        @StaticServiceProperty(name= AggregateFactory.NAME_PROPERTY, type="java.lang.String", value="last")
})
public class LastAggregate implements HolisticAggregateFunction<LastAggregate> {
    private Comparable value = null;

    private String attribute;

    public LastAggregate(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public void addValue(Tuple t) {
        this.value = t.get(attribute);
    }

    @Override
    public Comparable aggregate() {
        return value;
    }

    @Override
    public String toString() {
        return "last";
    }

    @Override
    public void mergeAggregate(LastAggregate agg) {
        if (agg.value != null)
            value = agg.value;
    }
}
