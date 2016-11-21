package fr.lig.sigma.astral.operators.aggregation;

import java.util.List;

/**
 *
 */
public interface AggregateFactory {
    String NAME_PROPERTY = "name";

    AggregateFunction get(String expression);

    List<String> getAttributes(String expression);
}
