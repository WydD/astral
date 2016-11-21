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
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

/**
 * Represents a classical positional window description
 * <p/>
 * r \in T
 * <p/>
 * \alpha(i) = a*i + b
 * \beta(i)  = c*i + d
 *
 * @author Loic Petit
 */
@Component
@Provides
public class LinearPositionalDescription implements WindowDescription {
    @Property(mandatory = true)
    private long a;
    @Property(mandatory = true)
    private long b;
    @Property(mandatory = true)
    private long c;
    @Property(mandatory = true)
    private long d;
    @Property(mandatory = true)
    private long rate;
    @Requires(id = "in")
    private Stream in;

    public boolean hasTemporalBounds() {
        return false;
    }

    public boolean hasTemporalRate() {
        return false;
    }

    public long alpha(int i, EventScheduler scheduler) {
        return Math.max(a * i + b, 0);
    }

    public long beta(int i, EventScheduler scheduler) {
        return Math.max(c * i + d, 0);
    }

    public long r() {
        return rate;
    }

    @Override
    public String toString() {
        return a + "i" + (b > 0 ? "+" + b : b < 0 ? b : "") + ", " + c + "i+" + (d > 0 ? "+" + d : d < 0 ? d : "") + ", " + rate;
    }
}