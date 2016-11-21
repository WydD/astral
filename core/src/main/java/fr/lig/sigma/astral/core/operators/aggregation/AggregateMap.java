package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFunction;
import fr.lig.sigma.astral.operators.aggregation.HolisticAggregateFunction;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
public class AggregateMap {

    private Map<List<Comparable>, AggregateFunction> aggregateTable = new HashMap<List<Comparable>, AggregateFunction>();

    private String function;
    private AggregateFactory af;

    public AggregateMap(String function, AggregateFactory af) {
        this.function = function;
        this.af = af;
    }

    public void putValue(List<Comparable> values, Tuple t) {
        AggregateFunction f = aggregateTable.get(values);
        if (f == null) {
            f = af.get(function);
            aggregateTable.put(values, f);
        }
        f.addValue(t);
    }

    public Comparable fetchAggregateAttribute(List<Comparable> values) {
        return aggregateTable.get(values).aggregate();
    }

    public Collection<AggregateFunction> values() {
        return aggregateTable.values();
    }

    public void mergeTables(AggregateMap next) {
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
