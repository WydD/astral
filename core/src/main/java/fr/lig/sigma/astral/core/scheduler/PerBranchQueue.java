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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.WaitingEntry;
import fr.lig.sigma.astral.common.structure.Entity;

import java.util.*;

/**
 * @author Loic Petit
 */
public class PerBranchQueue {
    private static final Comparator<Set<EventScheduler>> SET_COMPARATOR = new Comparator<Set<EventScheduler>>() {
        @Override
        /**
         * e1 < e2 <=> e1.size < e2.size
         */
        public int compare(Set<EventScheduler> e1, Set<EventScheduler> e2) {
            if (e1 == ALL_BRANCHES)
                return 1;
            if (e2 == ALL_BRANCHES)
                return -1;
            return e1.size() - e2.size();
        }
    };
    private static final Set<EventScheduler> ALL_BRANCHES = new HashSet<EventScheduler>();
    private Map<EventProcessor, Set<EventScheduler>> cache = new HashMap<EventProcessor, Set<EventScheduler>>();
    private Map<Set<EventScheduler>, WaitingQueue<Batch, EventProcessor>> queues;
    private Map<Set<EventScheduler>, Set<Set<EventScheduler>>> branchesSupset = new HashMap<Set<EventScheduler>, Set<Set<EventScheduler>>>();
    private EventScheduler scheduler;
    private List<Set<EventScheduler>> listOfBranches = new ArrayList<Set<EventScheduler>>();
    private Comparator<EventProcessor> comparator;
    private Set<EventScheduler> dependentSchedulers;

    public PerBranchQueue(EventScheduler scheduler, Comparator<EventProcessor> comparator) {
        this.scheduler = scheduler;
        this.comparator = comparator;
        queues = new HashMap<Set<EventScheduler>, WaitingQueue<Batch, EventProcessor>>();
        lazyGetQueue(ALL_BRANCHES);
    }

    @Override
    public String toString() {
        String s = "";
        for (Map.Entry<Set<EventScheduler>, WaitingQueue<Batch, EventProcessor>> entry : queues.entrySet()) {
            s += "\n\t\t";
            s += "[";
            if (entry.getKey() == ALL_BRANCHES)
                s += "ALL_BRANCHES";
            else {
                for (EventScheduler es : entry.getKey()) {
                    s += es.getRuntime() == null ? "DEAD" : es.getRuntime().getDisplayName() + ", ";
                }
            }
            if (s.length() > 4)
                s = s.substring(0, s.length() - 2);
            s += "] -> " + entry.getValue();
        }
        return s;
    }

    public synchronized void addToList(Batch to, EventProcessor e) {
        Set<EventScheduler> s = cache.get(e);
        if (s == null) {
            s = computeOriginScheduler(e);
            cache.put(e, s);
        }

        WaitingQueue<Batch, EventProcessor> queue = lazyGetQueue(s);
        queue.addToList(to, e);
        for (Set<EventScheduler> supBranch : branchesSupset.get(s)) {
            queues.get(supBranch).addToList(to, e);
        }
    }

    public Batch maxKey() {
        Batch maxKey = null;
        for (WaitingQueue<Batch, EventProcessor> queue : queues.values()) {
            Batch batch = queue.maxKey();
            if (batch != null && (maxKey == null || maxKey.compareTo(batch) < 0))
                maxKey = batch;
        }
        return maxKey;
    }

    public synchronized WaitingQueue<Batch, EventProcessor> getQueue(Set<EventScheduler> s) {
        return queues.get(s);
    }

    public Collection<Set<EventScheduler>> getBranches() {
        return listOfBranches;
    }

    private WaitingQueue<Batch, EventProcessor> lazyGetQueue(Set<EventScheduler> s) {
        WaitingQueue<Batch, EventProcessor> queue = queues.get(s);
        if (queue == null) {
            listOfBranches.add(s);
            Collections.sort(listOfBranches, SET_COMPARATOR);
            branchesSupset.put(s, new HashSet<Set<EventScheduler>>());
            for (Set<EventScheduler> child : queues.keySet()) {
                if (child.containsAll(s)) {
                    branchesSupset.get(s).add(child);
                } else if (s.containsAll(child)) {
                    branchesSupset.get(child).add(s);
                }
            }
            queue = new MapList<Batch, EventProcessor>(comparator);
            queues.put(s, queue);
        }
        return queue;
    }

    public void removeEntry(WaitingEntry<Batch, EventProcessor> entry) {
        for (WaitingQueue<Batch, EventProcessor> queue : queues.values()) {
            queue.removeEntry(entry);
        }
    }

    private Set<EventScheduler> computeOriginScheduler(EventProcessor e) {
        if (e.waitFor() == EventProcessor.ALL_WAIT)
            return ALL_BRANCHES;
        Set<EventScheduler> schedulerSet = new HashSet<EventScheduler>();
        if (e instanceof Entity) {
            if (((Entity) e).getScheduler() != scheduler) {
                schedulerSet.add(((Entity) e).getScheduler());
                return schedulerSet;
            }
        }

        for (EventProcessor p : e.waitFor()) {
            Set<EventScheduler> res = computeOriginScheduler(p);
            schedulerSet.addAll(res);
        }
        return schedulerSet;
    }

    public void setSchedulerSet(Set<EventScheduler> eventSchedulers) {
        dependentSchedulers = eventSchedulers;
        lazyGetQueue(eventSchedulers);
    }

    public boolean isEmpty() {
        for (WaitingQueue<Batch, EventProcessor> queue : queues.values()) {
            if (!queue.isEmpty()) return false;
        }
        return true;
    }

    public boolean containsKey(Batch globalTime) {
        for (WaitingQueue<Batch, EventProcessor> queue : queues.values()) {
            if (queue.containsKey(globalTime)) return true;
        }
        return false;
    }

    public int getTotalCount() {
        int idx = listOfBranches.size() - 1;
        if (idx < 0) return 0;
        Set<EventScheduler> highestBranch = listOfBranches.get(idx);
        return queues.get(highestBranch).getCount();
    }
}
