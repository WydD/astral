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

import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class NativeVolatileDynamicRelationImpl extends EventNotifier implements DynamicRelation {
    private TupleSet insertedTuples;
    private TupleSet deletedTuples;
    private TupleSet lastInsertedTuples;
    private TupleSet lastDeletedTuples;
    private Batch lastState = Batch.MIN_VALUE;
    private Batch previousState = Batch.MIN_VALUE;
    private TupleSet content;
    @Property(name = "entityname", mandatory = true)
    protected String name;

    @Property(mandatory = true)
    protected EntityFactory entityFactory;

    @Property(mandatory = true)
    protected Set<String> attributes;
    private TupleSet lastContent;
    private TupleSet emptyDiff;

    @Validate
    private void ready() {
        content = entityFactory.instanciateTupleSet(getAttributes());
        lastContent = entityFactory.instanciateTupleSet(getAttributes());
        emptyDiff = entityFactory.instanciateTupleSet(getAttributes());
        insertedTuples = emptyDiff;
        deletedTuples = emptyDiff;
        lastInsertedTuples = emptyDiff;
        lastDeletedTuples = emptyDiff;
    }

    @Override
    public TupleSet getInsertedTuples(Batch b) {
        if (b.compareTo(lastState) >= 0)
            return insertedTuples;
        if (b.compareTo(previousState) >= 0)
            return lastInsertedTuples;
        return emptyDiff;
    }

    @Override
    public TupleSet getDeletedTuples(Batch b) {
        if (b.compareTo(lastState) >= 0)
            return deletedTuples;
        if (b.compareTo(previousState) >= 0)
            return lastDeletedTuples;
        return emptyDiff;
    }

    @Override
    public void update(Batch b, TupleSet insertedTuples, TupleSet deletedTuples) {
        // no update
        if (insertedTuples.size() == 0 && deletedTuples.size() == 0)
            return;
        previousState = lastState;
        lastInsertedTuples = this.insertedTuples;
        lastDeletedTuples = this.deletedTuples;
        applyChanges(lastInsertedTuples, lastDeletedTuples, lastContent);

        lastState = b;

        this.insertedTuples = insertedTuples;
        this.deletedTuples = deletedTuples;

        applyChanges(insertedTuples, deletedTuples, content);

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
        if (b.compareTo(lastState) >= 0)
            return content;
        if (b.compareTo(previousState) >= 0)
            return lastContent;
        throw new IllegalStateException("Trying to access to way too old content (keeping only two state and diff)");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<String> getAttributes() {
        return attributes;
    }
}
