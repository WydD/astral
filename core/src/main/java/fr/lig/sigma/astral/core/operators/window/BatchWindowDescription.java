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

import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

/**
 *
 */
@Component
@Provides
public class BatchWindowDescription implements WindowDescription {
    @Requires(id = "in")
    private Stream in;

    @Override
    public boolean hasTemporalBounds() {
        return false;
    }

    @Override
    public boolean hasTemporalRate() {
        return false;
    }

    @Override
    public long alpha(int i, EventScheduler scheduler) {
        return in.reversetau(in.tau(i).getClose())+1;
    }

    @Override
    public long beta(int i, EventScheduler scheduler) {
        return i;
    }

    @Override
    public long r() {
        return 1;
    }

    @Override
    public String toString() {
        return "B";
    }
}
