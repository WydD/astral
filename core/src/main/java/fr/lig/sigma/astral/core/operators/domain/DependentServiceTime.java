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
import fr.lig.sigma.astral.common.structure.Entity;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class DependentServiceTime extends PreviousTime implements EventProcessor {
    @Requires(id = "dependent")
    private Entity dependent;

    private Queue<Batch> modifiedBatches = new LinkedList<Batch>();
    private Batch current = Batch.MIN_VALUE;

    @Validate
    private void validate() {
        dependent.registerNotifier(this);
    }

    @Override
    public Batch f(Batch t) {
        if (!modifiedBatches.isEmpty()) {
            Batch peek = modifiedBatches.peek();
            while (peek != null && t.compareTo(peek) >= 0) {
                current = modifiedBatches.poll();
                 peek = modifiedBatches.peek();
            }
        }
        return current;
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        modifiedBatches.add(b);
    }

    @Override
    public EventProcessor[] waitFor() {
        return new EventProcessor[]{dependent};
    }
}
