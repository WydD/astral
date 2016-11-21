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

package fr.lig.sigma.astral.operators.domain;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.operators.OperatorDescription;

/**
 * Parameters of the domain manipulator
 * @author Loic Petit
 */
public interface TimeTransformer extends OperatorDescription {
    /**
     * Condition of execution
     * @param b Current batch
     * @param es The operator's ES
     * @return c(t) as defined
     */
    boolean c(Batch b, EventScheduler es);

    /**
     * Not inside the AStrAL spec but it enables the operator to schedule the next state of change for c
     * @param b Current batch
     * @param es The operator's ES
     * @return Next batch (t',i) that will verify c(t',i) != c((t',i)^-)
     */
    Batch nextCChange(Batch b, EventScheduler es);

    /**
     * Function that will transform the domain of the relation.
     * @param t Base timestamp
     * @return Target t
     */
    Batch f(Batch t);
}
