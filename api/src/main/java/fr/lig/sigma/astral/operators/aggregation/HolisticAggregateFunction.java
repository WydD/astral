package fr.lig.sigma.astral.operators.aggregation;

import java.util.Collection;

/**
 *
 */
public interface HolisticAggregateFunction<E extends HolisticAggregateFunction> extends AggregateFunction {
    public void mergeAggregate(E agg);
}
