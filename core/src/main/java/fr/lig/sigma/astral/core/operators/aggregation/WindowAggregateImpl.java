package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.NotImplementedException;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.core.operators.window.PositionalAlgorithm;
import fr.lig.sigma.astral.core.operators.window.TemporalAlgorithm;
import fr.lig.sigma.astral.core.operators.window.WindowAlgorithm;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.operators.window.Window;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class WindowAggregateImpl extends RelationOperator implements Window {
    @Requires(id = "in", proxy = false)
    private Stream in;
    private Pane currentPane;
    private LinkedList<Pane> paneList = new LinkedList<Pane>();

    @Property(value = "[]")
    private String boundaries;
    @Requires(id = "core")
    private AstralCore core;

    @Property(mandatory = true)
    private List<Map<String, String>> aggregate;

    @Property(mandatory = true)
    private List<String> attributes;

    private Logger log = Logger.getLogger(WindowAggregateImpl.class);
    @Property(mandatory = true)
    private List<WindowDescription> description;
    @Property()
    private List<String> groupBy;

    private boolean includeLeftBoundary;
    private boolean includeRightBoundary;
    @Property
    private Map<String, String> structure;

    public int getMaxInputs() {
        return 1;
    }

    public void prepare() throws AxiomNotVerifiedException {
        includeLeftBoundary = boundaries.charAt(0) == '[';
        includeRightBoundary = boundaries.charAt(1) == ']';
        setOutput((Relation) entityFactory.instanciateEntity(
                "RelationBufferedVolatileImpl",
                in.getName() + "[" + description + "]",
                new HashSet<String>(attributes),
                new Hashtable<String, Object>(structure))
        );
        addInput(in, true);

        currentPane = new Pane(core.getAf(), aggregate, groupBy);

        WindowAlgorithm algo = buildAlgorithm(1, new Window() {
            @Override
            public void processWindow(Batch begin, Batch end, int size, Batch batch) {
                log.debug("Pane change@[" + begin + "-" + end + "]@" + batch);
                currentPane.setBatch(begin);
                paneList.addLast(currentPane);
                currentPane = new Pane(core.getAf(), aggregate, groupBy);
            }
        }, this);
        buildAlgorithm(0, this, algo);
    }

    private WindowAlgorithm buildAlgorithm(int i, Window w, EventProcessor waitingSlot) throws AxiomNotVerifiedException {
        WindowDescription description = this.description.get(i);
        scheduler = getScheduler();
        WindowAlgorithm algo;
        if (description.hasTemporalRate() && description.hasTemporalBounds())
            algo = new TemporalAlgorithm(w, description, in, scheduler, waitingSlot, boundaries);
        else if (!description.hasTemporalBounds() && !description.hasTemporalRate())
            algo = new PositionalAlgorithm(w, description, in, scheduler, waitingSlot, boundaries);
        else
            throw new NotImplementedException("Window description with positional bounds");
        in.registerNotifier(algo);
        algo.init();
        return algo;
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        while (in.hasNext(b)) {
            Tuple t = in.pop();
            if (!includeRightBoundary && in.B(t).compareTo(b) == 0)
                return;
            log.debug("New tuple@" + b + " in current pane");
            currentPane.put(t);
        }
    }

    public void processWindow(Batch begin, Batch end, int size, Batch batch) {
        // Remove useless panes
        while (paneList.peek() != null && paneList.peek().getBatch().compareTo(begin) < 0) {
            paneList.remove();
        }
        log.debug("Compute window-aggregate[" + begin + "-" + end + "]@" + batch + " current panelist " + paneList);
        Pane resultPane = new Pane(core.getAf(), aggregate, groupBy);
        for (Pane pane : paneList) {
            if (pane.getBatch().compareTo(end) > 0)
                break;
            resultPane.mergePane(pane);
        }
        TupleSet result = entityFactory.instanciateTupleSet(getAttributes());
        resultPane.fillResult(result);
        output.update(result, batch);
    }


    @Override
    public String toString() {
        return "AggregateWindow";
    }
}
