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

package fr.lig.sigma.astral.common.event;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Event notifier is an implementation of SchedulerContainer and EventProcessor.
 * It provides the primitive implementation for SchedulerContainer as well as the method registerNotifier of Entity.
 * Therefore, entities should extends this class in order to have the support of notifiers (via notifyProcessors).
 * <p/>
 * This class is poorly integrated inside the framework. It is very important, each entity extends more or less this
 * class, and it has static method, void methods etc...
 */
public class EventNotifier implements SchedulerContainer, EventProcessor {
    private Set<EventProcessor> processors = new HashSet<EventProcessor>();

    protected EventScheduler scheduler;
    private static final Logger log = Logger.getLogger(EventNotifier.class);

    public EventScheduler getScheduler() {
        return scheduler;
    }

    public void setScheduler(EventScheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * A method of Entity implemented here. Will register the event processor e as a listener. Considering the Entity
     * implementation, whenever the attached entity changes, it shall call notifyProcessors which will push processors
     * inside the scheduler.
     *
     * @param e The processor
     */
    public synchronized void registerNotifier(EventProcessor e) {
        log.trace(this + " registers " + e + " as notifier");
        processors.add(e);
        if (e instanceof SchedulerContainer && ((SchedulerContainer) e).getScheduler() == null)
            ((SchedulerContainer) e).setScheduler(scheduler);
    }

    public synchronized void unregisterNotifier(EventProcessor e) {
        log.trace(this + " unregisters " + e + " as notifier");
        processors.remove(e);
    }

    /**
     * Notify the registered processors that the state of the attached entity has changed
     *
     * @param b batch id
     */
    protected synchronized void notifyProcessors(Batch b) {
        if (processors.size() > 0 && scheduler == null)
            throw new RuntimeException("Fatal: No scheduler was attached to this component " + this);
        for (EventProcessor p : processors) {
            if (p instanceof SchedulerContainer) {
                ((SchedulerContainer) p).getScheduler().pushEvent(b, p);
            } else {
                scheduler.pushEvent(b, p);
            }
        }
    }

    /**
     * By default notify the processors that it was called
     *
     * @param b The batch
     * @throws AxiomNotVerifiedException
     */
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        notifyProcessors(b);
    }


    private EventProcessor[] uniqueChild = null;

    public void setUniqueChild(EventProcessor ep) {
        uniqueChild = new EventProcessor[]{ep};
    }

    public void setChilds(EventProcessor[] ep) {
        uniqueChild = ep;
    }

    /**
     * @return NO_WAIT by default
     */
    public EventProcessor[] waitFor() {
        if (uniqueChild == null)
            return NO_WAIT;
        return uniqueChild;
    }

}
