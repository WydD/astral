package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 */
public class Pane {
    private List<String> groupBy;
    private List<AggregateApplier> aggregates;
    private AggregateApplier phyAggregate;
    private Batch batch = Batch.MIN_VALUE;
    private static final Logger log = Logger.getLogger(Pane.class);

    public Pane(AggregateFactory af, List<Map<String, String>> aggregate, List<String> groupBy) {
        if (groupBy == null) groupBy = new LinkedList<String>();
        this.groupBy = groupBy;

        aggregates = new ArrayList<AggregateApplier>(aggregate.size());

        phyAggregate = new AggregateApplier(
                Tuple.PHYSICAL_ID,
                "first(" + Tuple.PHYSICAL_ID + ")", af);
        for (Map<String, String> entry : aggregate) {
            aggregates.add(new AggregateApplier(
                    entry.get("to"),
                    entry.get("function"), af));
        }
    }

    public void put(Tuple t) {
        List<Comparable> values = new ArrayList<Comparable>(groupBy.size());
        for (String attribute : groupBy)
            values.add(t.get(attribute));
        phyAggregate.putTuple(values, t);
        for (AggregateApplier applier : aggregates)
            applier.putTuple(values, t);
    }

    public void fillResult(TupleSet result) {
        for (List<Comparable> groupByValue : phyAggregate.getGroupByValues()) {
            Tuple t = new Tuple(phyAggregate.fetchAggregateAttribute(groupByValue));
            for (int iGroupBy = 0, groupBySize = groupBy.size(); iGroupBy < groupBySize; iGroupBy++)
                t.put(groupBy.get(iGroupBy), groupByValue.get(iGroupBy));
            for (AggregateApplier applier : aggregates) {
                applier.addAggregateAttribute(groupByValue, t);
            }
            log.debug("Adding tuple to the result " + t);
            result.add(t);
        }
    }

    public void mergePane(Pane p) {
        Iterator<AggregateApplier> it = p.aggregates.iterator();
        for (AggregateApplier applier : aggregates) {
            applier.mergeTables(it.next());
        }
        phyAggregate.mergeTables(p.phyAggregate);
    }

    public void reset() {
        for (AggregateApplier applier : aggregates) {
            applier.reset();
        }
        phyAggregate.reset();
    }

    public Batch getBatch() {
        return batch;
    }

    public String toString() {
        return "Pane@" + batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}
