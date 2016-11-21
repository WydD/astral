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

package fr.lig.sigma.astral.source;

import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.Entity;

import java.util.Set;

/**
 * Specify a source which is an entity with a scheduler (like an Operator actually).
 * No other method is allocated, it is for doing a right typing
 */
public interface Source extends Entity, SchedulerContainer {
    /**
     * Schedule the first event, this method is called by the factory after the creation of the source.
     *
     * @throws Exception any exception threw by the code
     */
    void firstSchedule() throws Exception;
    /**
     * Set the output attributes of it
     * @param attributes the attributes
     * @return true if the tuples that will be exported correctly, else a Pi element must be installed
     */
    boolean setOutputAttributes(Set<String> attributes);
}
