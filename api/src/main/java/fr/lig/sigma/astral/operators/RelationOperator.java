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

package fr.lig.sigma.astral.operators;

import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;
import org.apache.felix.ipojo.annotations.Component;

/**
 * @author Loic Petit
 */           
@Component
public abstract class RelationOperator extends RelationContainer implements Operator {
    private InputManager inputs = new InputManager(this);
    
    @Override
    public void setOutput(Relation output) {
        super.setOutput(output);
    }
    public EventProcessor[] waitFor() {
        return inputs.getInputs();
    }

    public Entity[] getInputs() {
        return inputs.getInputs();
    }

    protected void addInput(Entity in, boolean beNotifier) {
        inputs.addInput(in, beNotifier);
    }

    protected void factoryReady() {
    }
}
