package fr.lig.sigma.astral.operators.aggregation;

/**
 *
 */
public interface DifferentialAggregateFunction<E extends DifferentialAggregateFunction> extends HolisticAggregateFunction<E> {
    public void removeAggregate(E agg);
}
