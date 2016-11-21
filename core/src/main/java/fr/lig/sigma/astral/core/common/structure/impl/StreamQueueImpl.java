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

package fr.lig.sigma.astral.core.common.structure.impl;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.VolatileStructure;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 */
@Component
@Provides
public class StreamQueueImpl extends EventNotifier implements Stream, VolatileStructure {
    private Queue<Tuple> content = new LinkedList<Tuple>();
    @Property(mandatory = true)
    protected Set<String> attributes;
    @Property(name = "entityname", mandatory = true)
    private String name;
    private TreeMap<Long, Batch> tauFunc = new TreeMap<Long, Batch>();
    private TreeMap<Batch, Long> revTauFunc = new TreeMap<Batch, Long>();
    private TreeMap<Comparable, Batch> bs = new TreeMap<Comparable, Batch>();
    private boolean autoDelete = true;

    private static final Logger log = Logger.getLogger(StreamQueueImpl.class);

    private static final int NUM_BATCH_MAX = 10;

    private boolean isWaiting = true;
    private long i = 1;

    private boolean checkAttributes = true;

    @Validate
    private void ready() {
        // Will have something there
        //this.attributes = attributes;
        if (!attributes.contains(Tuple.TIMESTAMP_ATTRIBUTE))
            throw new IllegalArgumentException("Stream has not a timestamp attribute");

        attributes.add(Tuple.PHYSICAL_ID); // ENSURE PHYID IS THERE
        //this.name = name;
    }

    private Batch lastBatch = new Batch(Long.MIN_VALUE, 0);
    private Comparable lastId = null;

    private synchronized void input(Tuple t, int j) throws AxiomNotVerifiedException {
        Batch batch = new Batch(t.getTimestamp(), j);
        Comparable id = t.getId();
        if (lastBatch.compareTo(batch) > 0)
            throw new AxiomNotVerifiedException(AxiomNotVerifiedException.CONSISTENCY_AXIOM,
                    "Tuple(" + t + ") has a lower timestamp than " + lastBatch);
        if (lastId != null && id.compareTo(lastId) < 0)
            throw new AxiomNotVerifiedException(AxiomNotVerifiedException.CONSISTENCY_AXIOM,
                    "Tuple(" + t + ") has a strictly lower id, " + id + ", than " + lastId);
        revTauFunc.put(batch, i);
        lastBatch = batch;
        lastId = id;
        tauFunc.put(i, batch);
        content.offer(t);
        bs.put(id, batch);
        i++;
    }

    public synchronized void put(Tuple t, int i) throws AxiomNotVerifiedException {
        if (checkAttributes) {
            Set<String> tupleAttribs = t.keySet();
            if (!attributes.equals(tupleAttribs))
                throw new IllegalArgumentException(getName() + "Tuple has not the same attribute set: " + tupleAttribs + "\nRequired: " + attributes);
        }
        // Safe now
        input(t, i);
        notifyProcessors(lastBatch);
    }

    @Override
    public synchronized void put(Tuple t) throws AxiomNotVerifiedException {
        put(t, getNextBatchFromTuple(t));
    }

    @Override
    public synchronized void putAll(TupleSet ts) throws AxiomNotVerifiedException {
        if (ts.size() == 0)
            return;
        Tuple t = ts.iterator().next();
        putAll(ts, getNextBatchFromTuple(t));
    }

    private int getNextBatchFromTuple(Tuple t) {
        long time = t.getTimestamp();
        int batch = 0;
        if (time == lastBatch.getTimestamp())
            batch = lastBatch.getId() + 1;
        return batch;
    }

    public synchronized void putAll(TupleSet ts, int i) throws AxiomNotVerifiedException {
        // TODO : This have to be a commit-rollback method !
        if (checkAttributes) {
            Set<String> tupleAttribs = ts.getAttributes();
            if (!attributes.equals(tupleAttribs))
                throw new IllegalArgumentException("Tuple has not the same attribute set\n" + "\n" + attributes);
        }
        // Safe now
        for (Tuple t : ts)
            input(t, i);
        notifyProcessors(lastBatch);
    }

    @Override
    public boolean hasNext(Batch b) {
        Tuple t = content.peek();
        return t != null && B(t).compareTo(b) <= 0;
    }

    public synchronized Tuple pop() {
        Tuple t = content.peek();
        if (t == null) return null;
        if (autoDelete)
            forgetDataBefore(B(t), true);
        t = content.poll();
//        if (isWaiting) {
//            isWaiting = false;
//            notifyAll();
//        }
        return t;
    }

    public synchronized Tuple peek() {
        return content.peek();
    }

    public String getName() {
        return name;
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    public synchronized Batch tau(long n) {
        Batch t = tauFunc.get(n);
        return t == null ? Batch.MIN_VALUE : t;
    }

    public synchronized long reversetau(Batch b) {
        Map.Entry<Batch, Long> n = revTauFunc.floorEntry(b);
        return n == null ? 0 : n.getValue();
    }

    @Override
    public synchronized Batch B(Tuple t) {
        Comparable id = t.getId();
        return bs.get(id);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public synchronized void forgetDataBefore(Batch batch) {
        forgetDataBefore(batch, false);
    }


    private Batch lastClean = Batch.MIN_VALUE;

    public synchronized void forgetDataBefore(Batch batch, boolean selfClean) {
        if (!selfClean)
            autoDelete = false;
        if (batch.compareTo(lastClean) <= 0) return;
        Iterator<Map.Entry<Comparable, Batch>> bsIterator = bs.entrySet().iterator();
        while (bsIterator.hasNext()) {
            Map.Entry<Comparable, Batch> entry = bsIterator.next();
            if (entry.getValue().compareTo(batch) >= 0)
                break;
            bsIterator.remove();
        }

        long limitValue = 0;
        Iterator<Map.Entry<Batch, Long>> revTauIterator = revTauFunc.entrySet().iterator();
        while (revTauIterator.hasNext()) {
            Map.Entry<Batch, Long> entry = revTauIterator.next();
            if (entry.getKey().compareTo(batch) >= 0) {
                break;
            }
            limitValue = entry.getValue();
            revTauIterator.remove();
        }
        Iterator<Map.Entry<Long, Batch>> tauIterator = tauFunc.entrySet().iterator();
        while (tauIterator.hasNext()) {
            Map.Entry<Long, Batch> entry = tauIterator.next();
            if (entry.getKey() > limitValue) {
                break;
            }
            tauIterator.remove();
        }
        lastClean = batch;
    }
}
