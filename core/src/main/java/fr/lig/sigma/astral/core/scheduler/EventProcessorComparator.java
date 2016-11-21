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

package fr.lig.sigma.astral.core.scheduler;

import fr.lig.sigma.astral.common.event.EventProcessor;
import org.apache.log4j.Logger;

import java.util.Comparator;

/**
 * @author Loic Petit
 */
public class EventProcessorComparator implements Comparator<EventProcessor> {
    private Logger log = Logger.getLogger(EventProcessorComparator.class);

    @Override
    public int compare(EventProcessor p, EventProcessor q) {
        int i = __compare(p,q);
        if(log.isTraceEnabled()) {
            if(i > 0)
                log.trace("Compare "+p+" > "+q);
            else
                log.trace("Compare "+q+" > "+p);
        }
        return i;
    }

    public int __compare(EventProcessor p, EventProcessor q) {
        if (p == q) return 0;
        EventProcessor[] waiters = p.waitFor();
        if (waiters == EventProcessor.NO_WAIT) {
            if(q.waitFor() == EventProcessor.NO_WAIT)
                return 0;
            return -1;
        }
        if (q.waitFor() == EventProcessor.NO_WAIT) return 1;
        if (q.waitFor() == EventProcessor.ALL_WAIT) return -1;
        if (waiters == EventProcessor.ALL_WAIT) return 1;
        if (waiters == null) {
            log.error("Waiter null ?? " + p);
            return 0;
        }
        for (EventProcessor depends : waiters) {
            if ((depends == q) || ((depends.waitFor() != EventProcessor.ALL_WAIT) && compare(depends, q) > 0)) return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        return o.getClass() == this.getClass() && this == o;
    }
}
