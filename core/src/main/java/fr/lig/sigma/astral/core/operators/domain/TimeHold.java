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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.operators.domain.TimeTransformer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class TimeHold implements TimeTransformer {
    @Property(value = "-1")
    private long at;
    private Batch b0;

    @Validate
    public void validate() {
        b0 = new Batch(at,0);
    }

    public boolean c(Batch b, EventScheduler es) {
        if(at < 0) {
            at = es.getT0().getTimestamp();
            b0 = new Batch(at,0);
        }
        return b.compareTo(b0) >= 0;
    }

    public Batch nextCChange(Batch b, EventScheduler es) {
        return c(b,es) ? Batch.MIN_VALUE : b0;
    }

    public Batch f(Batch b) {
        return b;
    }

    @Override
    public String toString() {
        return "_{t>="+ at +"}^t";
    }
}
