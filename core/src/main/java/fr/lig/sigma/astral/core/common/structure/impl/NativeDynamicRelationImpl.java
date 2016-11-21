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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class NativeDynamicRelationImpl extends EventNotifier implements DynamicRelation {
    private TreeMap<Batch, TupleSet> pastInsertedTuples;
    private TreeMap<Batch, TupleSet> pastDeletedTuples;
    private Batch time = Batch.MIN_VALUE;
    private TreeMap<Batch, TupleSet> pastContent;
    @Property(name = "entityname", mandatory = true)
    protected String name;

    @Property(mandatory = true)
    protected EntityFactory entityFactory;

    @Property(mandatory = true)
    protected Set<String> attributes;
    @Property(value = "true")
    protected boolean buildContent;
    @Property(value = "true")
    protected boolean keepDiff;
    @Property(value = "true")
    protected boolean lastState;

    private TupleSet emptyDiff;

    private static Logger log = Logger.getLogger(NativeDynamicRelationImpl.class);

    @Validate
    private void ready() {
        pastContent = new TreeMap<Batch, TupleSet>();
        pastInsertedTuples = new TreeMap<Batch, TupleSet>();
        pastDeletedTuples = new TreeMap<Batch, TupleSet>();
        emptyDiff = entityFactory.instanciateTupleSet(getAttributes());
    }

    @Override
    public TupleSet getInsertedTuples(Batch b) {
        Map.Entry<Batch, TupleSet> entry = pastInsertedTuples.floorEntry(b);
        clean(b);
        if (entry == null) return emptyDiff;
        return entry.getValue();
    }

    private void clean(Batch b) {
        Batch headKey = cleanMap(b, pastContent, null);
        cleanMap(b, pastInsertedTuples, null);
        cleanMap(b, pastDeletedTuples, null);
    }

    private Batch cleanMap(Batch b, TreeMap<Batch, TupleSet> pastContent, Batch headKey) {
        if (headKey == null) {
            headKey = pastContent.floorKey(b);
            if (headKey != null)
                headKey = pastContent.floorKey(headKey.getClose());
        }
        if (headKey != null) {
            Set<Batch> batches = new TreeSet<Batch>(pastContent.headMap(headKey, !lastState).keySet());
            for (Batch toRemove : batches) {
                pastContent.remove(toRemove);
            }
        }
        return headKey;
    }

    @Override
    public TupleSet getDeletedTuples(Batch b) {
        Map.Entry<Batch, TupleSet> entry = pastDeletedTuples.floorEntry(b);
        clean(b);
        if (entry == null) return emptyDiff;
        return entry.getValue();
    }

    @Override
    public void update(Batch b, TupleSet insertedTuples, TupleSet deletedTuples) {
        // no update
        if (insertedTuples.size() == 0 && deletedTuples.size() == 0)
            return;

        if (buildContent) {
            TupleSet content = entityFactory.instanciateTupleSet(getAttributes());
            TupleSet ts = pastContent.get(time);
            if (ts != null)
                content.addAll(ts);
            applyChanges(insertedTuples, deletedTuples, content);
            pastContent.put(b, content);
            if (log.isTraceEnabled())
                log.trace("Receiving new content for " + b + ":{\n" + content + "\n}");
        }
        if (keepDiff) {
            pastInsertedTuples.put(b, insertedTuples);
            pastDeletedTuples.put(b, deletedTuples);
        }
        time = b;

        notifyProcessors(b);
    }

    private void applyChanges(TupleSet insertedTuples, TupleSet deletedTuples, TupleSet content) {
        for (Tuple t : insertedTuples)
            content.add(t);
        for (Tuple t : deletedTuples)
            content.remove(t);
    }

    @Override
    public void update(TupleSet content, Batch b) {
        throw new IllegalStateException("Calling update on a native dynamic relation");
    }

    @Override
    public TupleSet getContent(Batch b) {
        Map.Entry<Batch, TupleSet> entry = pastContent.floorEntry(b);
        clean(b);
        if (entry == null) return emptyDiff;
        return entry.getValue();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getAttributes() {
        return attributes;
    }

    @Override
    public String toString() {
        return "NativeDynamicRelationImpl{" +
                "pastContent=" + pastContent.keySet() +
                '}';
    }
}
