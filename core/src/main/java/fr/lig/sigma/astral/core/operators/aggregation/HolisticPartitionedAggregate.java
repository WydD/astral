package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFunction;
import fr.lig.sigma.astral.operators.aggregation.HolisticAggregateFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Loic Petit
 */
public class HolisticPartitionedAggregate implements HolisticAggregateFunction<HolisticPartitionedAggregate> {
    private AggregateMap map;
    private String[] attributes;
    private final HolisticAggregateFunction aggregateFunction;
    private static final String TMP = "tmp";

    public HolisticPartitionedAggregate(AggregateFactory af, String function, String subFunction, String... attributes) {
        this.attributes = attributes;
        map = new AggregateMap(subFunction, af);
        aggregateFunction = (HolisticAggregateFunction) af.get(function + "(" + TMP + ")");
    }

    @Override
    public void mergeAggregate(HolisticPartitionedAggregate agg) {
        map.mergeTables(agg.map);
    }

    @Override
    public void addValue(Tuple t) {
        List<Comparable> values = new ArrayList<Comparable>(attributes.length);
        for (String attribute : attributes)
            values.add(t.get(attribute));
        map.putValue(values, t);
    }

    @Override
    public Comparable aggregate() {
        Tuple t = new Tuple(0);
        for (AggregateFunction f : map.values()) {
            t.put(TMP, f.aggregate());
            aggregateFunction.addValue(t);
        }
        return aggregateFunction.aggregate();
    }

}
