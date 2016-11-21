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

package fr.lig.sigma.astral.core.operators.domain;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.operators.domain.TimeTransformer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class DomainManipulatorImpl extends RelationOperator implements Operator, Relation {
    private int id;
    private static final Logger log = Logger.getLogger(DomainManipulatorImpl.class);
    private Batch lastB = new Batch(Long.MIN_VALUE, 0);
    @Requires(id = "in")
    private Relation in;
    @Property(mandatory = true)
    private TimeTransformer description;

    private EventScheduler scheduler;
    private EventProcessor[] wait;
    @Property
    private Map<String, String> structure;

    public void prepare() {
        //description = prepareTimeTransformer(at, dtype, condition, dependent);

        setOutput(createNewFrom(in, in.getAttributes(), "D" + description + "(" + in.getName() + ")", structure));
        addInput(in, true);
        if (description instanceof EventProcessor)
            wait = new EventProcessor[]{in, (EventProcessor) description};
        else
            wait = new EventProcessor[]{in};

        scheduler = getScheduler();

        if (description.nextCChange(Batch.MIN_VALUE, scheduler) != Batch.MIN_VALUE) {
            id = scheduler.registerIndependentProcessor(this);
            try {
                scheduler.pushIndependentEvent(lastB, id);
            } catch (AxiomNotVerifiedException e) {
                // Will not be threw as there is no reason to execute now the code (not in runtime !)
                throw new IllegalStateException(e);
            }
        }
    }

    @Override
    public EventProcessor[] waitFor() {
        return wait;
    }

    private boolean lastWasVoid = true;

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        if (description.c(b, scheduler)) {
            Batch tmp = description.f(b);
            log.debug("Valid time " + b + " transformed to " + tmp);
            if (lastB.compareTo(tmp) != 0) {
                lastB = tmp;
                output.update(in.getContent(lastB), b);
            }
            lastWasVoid = false;
        } else {
            if (!lastWasVoid) {
                output.update(entityFactory.instanciateTupleSet(in.getAttributes()), b);
                lastWasVoid = true;
            }
        }

        Batch nextChange = description.nextCChange(b, scheduler);
        if (nextChange != Batch.MIN_VALUE)
            scheduler.pushIndependentEvent(nextChange, id);
    }

    public int getMaxInputs() {
        return 1;
    }

    @Override
    public String toString() {
        return "Domain(" + description.getClass().getSimpleName() + ")";
    }
}
