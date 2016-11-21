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

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.TimeChangeListener;
import fr.lig.sigma.astral.common.event.WaitingEntry;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Scheduler version 2
 * <p/>
 * ! WARNING ! Do not read this class... never... ever...
 * You might say ``Aww it's commented and stuff, that's cute'' but this code is the devil himself!
 * If I commented it, it was only because it was hard as hell to keep trace of all sync conditions.
 * <p/>
 * Thou shall never debug this class.
 * <p/>
 * HoursSpentOnThis250LinesClass = Way above 400
 *
 * @author Loic Petit
 */
@Instantiate
@Component
@Provides(strategy = "INSTANCE")
public class EventScheduler2 implements EventScheduler {
    private static final Logger log = Logger.getLogger(EventScheduler2.class);
    private static final EventProcessorComparator eventProcessorComparator = new EventProcessorComparator();

    private QueryRuntime runtime;

    private PerBranchQueue perBranchQueue = new PerBranchQueue(this, eventProcessorComparator);
    private TreeMap<Integer, EventProcessor> independentProcessors = new TreeMap<Integer, EventProcessor>();
    private Set<EventProcessor> indies = new HashSet<EventProcessor>();
    private Map<EventScheduler, Batch> dependentSchedulers = new HashMap<EventScheduler, Batch>();
    private Set<EventScheduler> globalDependentSchedulers = new HashSet<EventScheduler>();
    private Set<EventScheduler> externalDependentSchedulers = new HashSet<EventScheduler>();
    private Set<EventScheduler> strongDependentSchedulers = new HashSet<EventScheduler>();
    private Map<EventScheduler, Batch> deadListeners = new HashMap<EventScheduler, Batch>();

    private List<TimeChangeListener> timeListeners = new LinkedList<TimeChangeListener>();

    private Batch globalTime = Batch.MIN_VALUE;
    private Batch pushLimit = Batch.MAX_VALUE;
    private Batch t0 = null;
    private Batch dependentBatchLimit = Batch.MAX_VALUE;
    private boolean sourceOnly = false;
    private boolean notifyTimeChangedNextRound = false;
    private boolean fullWait = true;
    private Set<EventScheduler> executeBranch = null;
    private boolean weakMode = false;

    public synchronized void pushEvent(Batch b, EventProcessor event) {
        Batch maxKey;
        // Push a new event if the push limit allows us or if the batch is lower than the highest batch in the wait list
        // (this allows to clear all event even when the push limit is set)
        if (pushLimit.compareTo(globalTime) >= 0 || b.compareTo((maxKey = perBranchQueue.maxKey()) == null ? Batch.MAX_VALUE : maxKey) <= 0) {
            if (log.isTraceEnabled())
                log.trace("Planning " + event + "@" + b);
            if (t0 == null && b != Batch.MIN_VALUE)
                t0 = b;
            perBranchQueue.addToList(b, event);
            // New event = wake up
            notify();
        } else {
            log.debug("Rejected event @" + b + " for " + event);
        }

    }


    public synchronized void togglePush(long timestamp) {
        if (timestamp == Long.MIN_VALUE)
            return; // dafuq
        // A toggle push is a call-for-dead action performed at (timestamp, MAX)
        pushEvent(new Batch(timestamp, Integer.MAX_VALUE), new EventProcessor() {
            @Override
            public void processEvent(Batch b) throws AxiomNotVerifiedException {
                // Notify that this scheduler is officially dead
                notifyTimeChanged(null);
                /*HashSet<EventScheduler> sch = new HashSet<EventScheduler>(dependentSchedulers.keySet());
                sch.removeAll(strongDependentSchedulers);
                for(EventScheduler e : sch) {
                    e.togglePush(b.getTimestamp());
                } */
            }

            @Override
            public EventProcessor[] waitFor() {
                // Be sure to be executed last
                return EventProcessor.ALL_WAIT;
            }
        });
        // No more event is hereby accepted
        pushLimit = new Batch(timestamp, 0);
        // Wake up the HASNEXT to maybe go out
        notify();
    }

    public Batch getGlobalTime() {
        return globalTime;//globalTime.compareTo(t0) > 0 ? globalTime : t0;
    }

    public Batch getT0() {
        return t0;
    }

    @Override
    public synchronized void addDependentEventScheduler(EventScheduler es, boolean external) {
        globalDependentSchedulers.add(es);
        if (!es.isWeak())
            strongDependentSchedulers.add(es);
        if (external)
            externalDependentSchedulers.add(es);
        es.addTimeChangeListener(this);
    }

    public int registerIndependentProcessor(EventProcessor processor) {
        int id = 0;
        if (!independentProcessors.isEmpty())
            id = independentProcessors.lastKey() + 1;
        independentProcessors.put(id, processor);
        indies.add(processor);
        return id;
    }

    public synchronized void pushIndependentEvent(Batch b, int id) throws AxiomNotVerifiedException {
        pushEvent(b, independentProcessors.get(id));
        // Synchro w/ dependents schedulers
    }

    @Override
    public void addTimeChangeListener(TimeChangeListener tcl) {
        timeListeners.add(tcl);
        tcl.timeChanged(this, globalTime);
    }

    @Override
    public synchronized boolean hasNext() throws InterruptedException {
        // The only place to detect that this is a source runtime
        if (globalTime == Batch.MIN_VALUE) {
            perBranchQueue.setSchedulerSet(globalDependentSchedulers);
            if (dependentSchedulers.isEmpty() && deadListeners.isEmpty()) {
                sourceOnly = true;
            }
        }
        // Notify time change when NEXT() has decided to, must be done AFTER the execution of the all processors @globalTime
        if (notifyTimeChangedNextRound && globalTime.compareTo(pushLimit) < 0 && !perBranchQueue.containsKey(globalTime)) {
            notifyTimeChangedNextRound = false;
            if (runtime != null && runtime.getDisplayName().equals("device-register"))
                System.out.println("NOTICE! " + globalTime);
            notifyTimeChanged(globalTime);
        }
        while (mustWait()) {
            if (log.isTraceEnabled()) {
                log.trace("Waiting: " + perBranchQueue);
            }
            /**
             * End of the query if: (or)
             *  - The global time is higher than the push limit
             *  - The waitList is empty and no more schedulers are attached to this runtime (with the exception of sources)
             */
            if (globalTime.compareTo(pushLimit) >= 0 || !sourceOnly && perBranchQueue.isEmpty() && dependentSchedulers.isEmpty()) {
                if (log.isTraceEnabled())
                    log.trace("... but skipping it cause it's the end :D " + globalTime + "/" + pushLimit);
                return false;
            }
            // Effectively wait
            wait();
            if (log.isTraceEnabled())
                log.trace("Release! :)");
        }
        return true;
    }

    private boolean mustWait() {
        if (t0 == null && !sourceOnly) // T0 is empty!
            return true;

        executeBranch = null;
        /**
         * Try now the same algorithm for each branch of schedulers
         */
        Set<EventScheduler> branch = computeNextBranch();
        if (branch == null) return true;
        executeBranch = branch;
        return false;
    }

    private Set<EventScheduler> computeNextBranch() {
        WaitingEntry<Batch, EventProcessor> entry;
        for (Set<EventScheduler> branch : perBranchQueue.getBranches()) {
            Batch minLimit = minTime(branch);
            WaitingQueue<Batch, EventProcessor> waitQueue = perBranchQueue.getQueue(branch);
            entry = waitQueue.peekHead();
            /**
             * Wait if: (or)
             *  - The list is empty
             *  - One dependent scheduler is late strictly speaking
             */
            boolean secondWait = waitQueue.isEmpty() ||
                    minLimit.compareTo(entry.getKey()) < 0;
            if (!secondWait) {
                // Ok this branch can execute events !
                return branch;
            }
        }
        return null;
    }

    /**
     * Computes the minimum dependent batch for a specific branch of scheduler. Note that it takes the globalDependent
     * into account
     *
     * @param branch The branch to analyze
     * @return The minimum batch
     */
    private Batch minTime(Set<EventScheduler> branch) {
        Batch minLimit = Batch.MAX_VALUE;
        for (EventScheduler scheduler : globalDependentSchedulers) {
            Batch limit = dependentSchedulers.get(scheduler);
            if (limit == null) continue;
            minLimit = minLimit.compareTo(limit) > 0 ? limit : minLimit;
        }
        for (EventScheduler scheduler : branch) {
            Batch limit = dependentSchedulers.get(scheduler);
            if (limit == null) continue;
            minLimit = minLimit.compareTo(limit) > 0 ? limit : minLimit;
        }
        return minLimit;
    }

    public synchronized void timeChanged(EventScheduler source, Batch notifyBatch) {
        // A null batch indicates that the source scheduler is leaving
        if (notifyBatch != null) {
            if (t0 == null && notifyBatch != Batch.MIN_VALUE)
                t0 = notifyBatch;
            dependentSchedulers.put(source, notifyBatch);
        } else {
            // If it is null, remove it from the schedulers
            dependentSchedulers.remove(source);
            externalDependentSchedulers.remove(source);
            strongDependentSchedulers.remove(source);
            // Retreive the last batch of the source
            Batch lastBatch = source.getGlobalTime();

            if (runtime != null)
                log.debug(runtime.getDisplayName() + " removed scheduler " + source.getRuntime().getDisplayName() + " lastbatch was " + lastBatch);
            // Keep trace of the last batch that the source had
            deadListeners.put(source, lastBatch);
        }

        updateDependentBatchLimit();
        // Compute the minimum time between all schedulers
        if (!sourceOnly && dependentSchedulers.isEmpty()) {
            //dependentBatchLimit = Batch.MAX_VALUE;
            // Ok now the runtime will switch to dead
            // Computes the push limit from the last batch that schedulers had and toggle dead
            Batch max = globalTime;
            if (!deadListeners.isEmpty())
                max = Collections.max(deadListeners.values());
            if (max != null) {
                togglePush(max.getTimestamp());
                deadListeners.clear();
            }
        }
        // Race condition blocker :)
        // When this runtime has never ran and the dependentBatchLimit > (0,0)
        if (runtime == null || runtime.getStatus() != QueryStatus.RUNNING) return;

        // If the notified batch is alive, and
        //     - The wait list is empty and the dependent schedulers ahead in time
        //    OR The potential next event is a independent processor with a time planned after the dependent schedulers
        // Just change the global time and transfer it above
        Set<EventScheduler> branch = computeNextBranch();
        WaitingEntry<Batch, EventProcessor> headEvent = branch == null ? null : perBranchQueue.getQueue(branch).peekHead();
        if (notifyBatch != null && ((headEvent == null && dependentBatchLimit.compareTo(globalTime) > 0) ||
                (headEvent != null && indies.contains(headEvent.getValue()) && dependentBatchLimit.compareTo(headEvent.getKey()) < 0))) {
            globalTime = dependentBatchLimit;
            notifyTimeChanged(globalTime);

            if (runtime != null && log.isTraceEnabled())
                log.trace(runtime.getDisplayName() + " informs that it changes to " + globalTime);
        }

        if (log.isTraceEnabled())
            log.trace("Time of one source has changed new limit: " + dependentBatchLimit + dependentSchedulers.values());
        // As the dependent schedulers has changed, notify HASNEXT
        notify();
    }

    private void updateDependentBatchLimit() {
        if (dependentSchedulers.isEmpty()) {
            dependentBatchLimit = Batch.MAX_VALUE;
            return;
        }
        if (fullWait) {
            dependentBatchLimit = Collections.min(dependentSchedulers.values());
        } else {
            Batch computingSchedulers = Batch.MAX_VALUE;
            for (Map.Entry<EventScheduler, Batch> entry : dependentSchedulers.entrySet()) {
                if (entry.getKey().getRuntime().isComputing() && entry.getValue().compareTo(computingSchedulers) < 0) {
                    computingSchedulers = entry.getValue();
                }
            }
            Batch latest = Collections.max(dependentSchedulers.values());
            dependentBatchLimit = latest.compareTo(computingSchedulers) < 0 ? latest : computingSchedulers;
        }
    }

    @Override
    public synchronized WaitingEntry<Batch, EventProcessor> next() {
        WaitingQueue<Batch, EventProcessor> waitList;
        if (executeBranch != null) {
            waitList = perBranchQueue.getQueue(executeBranch);
        } else {
            throw new IllegalStateException("The scheduler has selected no branch to execute, which must not happen");
        }
        WaitingEntry<Batch, EventProcessor> entry = waitList.popHead();
        perBranchQueue.removeEntry(entry);

        Batch oldTime = globalTime;

        Set<EventScheduler> topBranch = new HashSet<EventScheduler>(dependentSchedulers.keySet());
        topBranch.removeAll(externalDependentSchedulers);

        if (executeBranch == null || executeBranch.containsAll(topBranch) && !perBranchQueue.getQueue(executeBranch).containsKey(entry.getKey())) {
            globalTime = entry.getKey();
            // If the time has changed notify the changes only when all actions at this time has been executed
            if (!oldTime.equals(globalTime)) {
                notifyTimeChangedNextRound = true;
            }
        }

        if (log.isTraceEnabled()) {
            EventProcessor value = entry.getValue();
            //log.trace("branch: " + executeBranch);
            //log.trace("waitList: " + waitList);
            log.trace("Selecting: " + value);
        }
        return entry;
    }

    private void notifyTimeChanged(Batch time) {
        if (log.isTraceEnabled())
            log.trace("Notifies " + timeListeners.size() + " schedulers that time changed to " + time);
        if (time == null) {
            log.trace(perBranchQueue.toString());
        }
        for (TimeChangeListener tcl : timeListeners)
            tcl.timeChanged(this, time);
    }

    @Override
    public QueryRuntime getRuntime() {
        return runtime;
    }

    @Override
    public void setRuntime(QueryRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public synchronized int getQueueCount() {
        return perBranchQueue.getTotalCount();
    }

    @Override
    public Set<EventScheduler> getDependentSchedulers() {
        return dependentSchedulers.keySet();
    }

    @Override
    public void setWaitMode(boolean fullWait) {
        this.fullWait = fullWait;
    }

    @Override
    public String toString() {
        String res = "EventScheduler2@" + (runtime == null ? "DEAD" : runtime.getDisplayName());

        res += "\n\tGlobal Time: " + globalTime;
        res += "\n\tPush Limit: " + pushLimit;
        res += "\n\tWait List: " + perBranchQueue;
        res += "\n\tDependentBatchLimit " + dependentBatchLimit;
        res += "\n\tWeak: " + weakMode;
        res += "\n\tIndies: " + indies;
        res += "\n\tDependentSchedulers: ";
        for (Map.Entry<EventScheduler, Batch> entry : dependentSchedulers.entrySet()) {
            res += "\n\t\t" + entry.getValue() + "\t" + entry.getKey().getRuntime().getDisplayName();
        }
        res += "\n\tDead listeners: ";
        for (Map.Entry<EventScheduler, Batch> entry : deadListeners.entrySet()) {
            QueryRuntime runtime = entry.getKey().getRuntime();
            res += "\n\t\t" + entry.getValue() + "\t" + (runtime == null ? "DEAD" : runtime.getDisplayName());
        }

        return res;
    }

    @Override
    public void setT0(long t0) {
        this.t0 = new Batch(t0, 0);
    }

    @Override
    public void setWeakMode(boolean weakMode) {
        this.weakMode = weakMode;
    }

    @Override
    public boolean isWeak() {
        return weakMode;
    }

    @Override
    public void addDependentEventScheduler(EventScheduler es) {
        addDependentEventScheduler(es, false);
    }
}
