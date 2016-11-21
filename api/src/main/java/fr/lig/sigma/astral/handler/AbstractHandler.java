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

package fr.lig.sigma.astral.handler;

import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.structure.Entity;

/**
 * @author Loic Petit
 */
public abstract class AbstractHandler<E extends Entity> implements Handler<E> {
    protected E in;
    protected EventProcessor[] wait;

    protected String[] requirements = null;

    public void setInput(E in) {
        this.in = in;
        in.registerNotifier(this);
        wait = new EventProcessor[] {in};
    }

    @Override
    public EventProcessor[] waitFor() {
        return wait;
    }
}
