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

package fr.lig.sigma.astral.operators.misc;

import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.query.AstralCore;

import java.util.Dictionary;

/**
 * @author Loic Petit
 */
public interface Duplicate<E extends Entity> extends EventProcessor, SchedulerContainer {
    E addDuplicate(AstralCore core, Dictionary<String, Object> properties);
    int getSize();
    E getSource();

    void sourceDown();
}
