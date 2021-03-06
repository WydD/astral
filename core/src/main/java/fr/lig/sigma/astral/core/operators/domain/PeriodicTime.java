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
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.StaticServiceProperty;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class PeriodicTime implements TimeTransformer {
    private Batch t0;

    @Property(mandatory = true)
    private int rate;

    @Override
    public boolean c(Batch b, EventScheduler es) {
        t0 = es.getT0();
        return true;
    }

    @Override
    public Batch nextCChange(Batch b, EventScheduler es) {
        t0 = es.getT0();
        return new Batch((long) ((Math.floor((b.getTimestamp()-t0.getTimestamp())/ rate)+1)* rate +t0.getTimestamp()), 0);
    }

    @Override
    public Batch f(Batch t) {
        return new Batch((long) (Math.floor((t.getTimestamp()-t0.getTimestamp())/ rate)* rate +t0.getTimestamp()), 0);
    }
}
