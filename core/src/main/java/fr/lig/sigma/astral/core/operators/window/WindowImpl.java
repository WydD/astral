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

package fr.lig.sigma.astral.core.operators.window;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.NotImplementedException;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.VolatileStructure;
import fr.lig.sigma.astral.operators.DynamicRelationOperator;
import fr.lig.sigma.astral.operators.window.Window;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.Map;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class WindowImpl extends DynamicRelationOperator implements Window, DynamicRelation {
    @Requires(id = "in", proxy = false)
    private Stream in;
    private TupleBuffer buffer;

    @Property(value = "[]")
    private String boundaries;
    @Property
    private Map<String, String> structure;

    private Logger log = Logger.getLogger(WindowImpl.class);
    private EventProcessor[] waiters = new EventProcessor[1];
    @Property(mandatory = true)
    private WindowDescription description;

    public int getMaxInputs() {
        return 1;
    }


    public void prepare() throws AxiomNotVerifiedException {
        setOutput((DynamicRelation) entityFactory.instanciateEntity(
                "NativeDynamicRelation",
                in.getName() + "[" + description + "]",
                in.getAttributes(),
                new Hashtable<String, Object>(structure))
        );

        addInput(in, false);

        scheduler = getScheduler();
        WindowAlgorithm algo;

        if (description.hasTemporalRate() && description.hasTemporalBounds())
            algo = new TemporalAlgorithm(this, description, in, scheduler, in, boundaries);
        else if (!description.hasTemporalBounds() && !description.hasTemporalRate())
            algo = new PositionalAlgorithm(this, description, in, scheduler, in, boundaries);
        else
            throw new NotImplementedException("Window description with positional bounds");
        //waiters[0] = in;
        waiters[0] = algo;
        buffer = new TupleBuffer(in, boundaries.charAt(0) == '[' || !description.hasTemporalBounds(),
                boundaries.charAt(1) == ']' || !description.hasTemporalBounds());

        if (in instanceof VolatileStructure) {
            ((VolatileStructure) in).forgetDataBefore(Batch.MIN_VALUE);
        }

        in.registerNotifier(algo);
        algo.init();
    }

    public void processWindow(Batch begin, Batch end, int size, Batch batch) {
        TupleSet is = entityFactory.instanciateTupleSet(getAttributes(), true);
        TupleSet ds = entityFactory.instanciateTupleSet(getAttributes(), true);
        buffer.clearBufferUntil(begin, ds);
        // Now: all buffer contains tuple that has timestamp >= begin

        buffer.fillBufferUntil(end, is);
        // Now buffer contains everything

        // remove tuples to fill the window size
        buffer.clearBuffer(size, is, ds);
        if (log.isTraceEnabled())
            log.trace("Commiting frame with #is,#ds = (" + is.size() + "," + ds.size() + ")");
        update(batch, is, ds);
    }

    @Override
    public EventProcessor[] waitFor() {
        return waiters;
    }

    @Override
    public String toString() {
        return "Window";
    }
}
