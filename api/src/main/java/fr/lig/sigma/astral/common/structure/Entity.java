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

package fr.lig.sigma.astral.common.structure;

import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.SchedulerContainer;

import java.util.Set;

/**
 * Common methods of both {@link Relation} and {@link Stream}. Any entity must implements this interface. As a matter of
 * fact, many objects manipulated inside astral have those methods implemented.
 *
 * A particular attention is ported on the parent of this interface which is
 * {@link fr.lig.sigma.astral.common.event.EventProcessor}. Therefore, a entity is before everything an event processor.
 * The method {@link #registerNotifier(fr.lig.sigma.astral.common.event.EventProcessor)} confirms it.
 * @author Loic Petit
 */
public interface Entity extends EventProcessor, SchedulerContainer {
    /**
     * Register a new event processor to be notified when the entity changes.
     * @param e the processor to notify
     */
    void registerNotifier(EventProcessor e);
    /**
     * Unregister this notifier
     * @param e the processor to notify
     */
    void unregisterNotifier(EventProcessor e);
    /**
     * Get the name
     * @return The string that matches the name
     */
    String getName();

    /**
     * As any entity, it has attributes !
     * @return The attributes set 
     */
    Set<String> getAttributes();
}
