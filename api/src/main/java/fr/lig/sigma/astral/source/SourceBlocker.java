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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Utility class that helps developers to build sources which have unknown arrivals.
 *
 * This class is an event processor that is placed inside the scheduling pipeline in order to wait for a new arrival.
 * The usage is fairly simple. Once created with an attached source, the source implementation must signal this object
 * to release the lock.
 *
 * In order to keep the global coherency, the <pre>waitFor</pre> function for the source MUST include this processor.
 *
 * Also, the <pre>firstSchedule</pre> must be called to initialize the process as well as a source
 * @author Loïc Petit
 */
public class SourceBlocker implements EventProcessor {
    private Logger log = Logger.getLogger(SourceBlocker.class);
    private SchedulerContainer source;
    private BlockingQueue<Batch> queue = new LinkedBlockingQueue<Batch>();
    private boolean first = true;
    private static final Batch SENTRY = new Batch(0,0);

    private EventScheduler scheduler;
    public SourceBlocker(EventScheduler s) {
        scheduler = s;
    }
    /**
     * Create a new Source Blocker
     * @param s The source to block
     */
    public SourceBlocker(SchedulerContainer s) {
        source = s;
    }


    /**
     * Implementation of the processEvent, will be called by the scheduler
     *
     * Its behaviour consist in getting the batch given by the release function and schedule itself for this batch.
     * Batches are managed by a blocking queue.
     * @param b the current batch
     */
    @Override
    public void processEvent(Batch b) {
        try {
            Batch nextWait = queue.take();
            if(nextWait != SENTRY)
                scheduler.pushEvent(nextWait, this);
            else
                scheduler.togglePush(b.getTimestamp());
        } catch (InterruptedException e) {
            log.warn("Interrupted waiting of new events");
        }
    }


    private EventProcessor[] waiter = ALL_WAIT;

    /**
     * Informs the scheduler to wait for everyone to finish
     * @return ALL_WAIT
     */
    public EventProcessor[] waitFor() {
        return waiter;
    }

    /**
     * Notify the blocker that the given batch has an update  
     * @param b the batch
     */
    public void release(Batch b) {
        try {
            queue.put(b);
            //log.debug("Prepare "+toString()+" @ "+b);
        } catch (InterruptedException e) {
            log.warn("Interrupted while inserting new batch");
        }
    }

    /**
     * MUST be called by the Source in order to initialize the scheduling process
     */
    private boolean initialized = false;
    public void firstSchedule() {
        if(initialized) return;
        if(scheduler == null) {
            scheduler = source.getScheduler();
        }
        //scheduler.incNumberOfSource();
        scheduler.pushEvent(Batch.MIN_VALUE, this);
        initialized = true;
    }

    @Override
    public String toString() {
        return "Blocker/"+source;
    }

    public void releaseDefinitely() {
        release(SENTRY);
    }
}
