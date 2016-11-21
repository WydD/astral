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

package fr.lig.sigma.astral.common.event;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;

/**
 * Interface to process an event
 */
public interface EventProcessor {
    /**
     * Process the event 
     * @param b the batch
     * @throws AxiomNotVerifiedException If any axiom has been broken during the treatment
     */
    void processEvent(Batch b) throws AxiomNotVerifiedException;

    /**
     * Tell the scheduler which processors must be executed before this one
     * @return An array of processors (will be queried each time)
     */
    EventProcessor[] waitFor();
    /**
     * Special wait: if the scheduler gets this value whenever it calls waitFor, it considers that there is no
     * dependant processors.
     */
    public static final EventProcessor[] NO_WAIT = new EventProcessor[0];
    /**
     * Special wait: if the scheduler gets this value whenever it calls waitFor, it considers that this processor is
     * to be executed after all other processors.
     */
    public static final EventProcessor[] ALL_WAIT = new EventProcessor[0];
}
