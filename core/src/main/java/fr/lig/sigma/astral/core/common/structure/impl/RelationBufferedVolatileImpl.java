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
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 */
@Component
@Provides
public class RelationBufferedVolatileImpl extends RelationHashImpl {
    @Property(name = "entityname", mandatory = true)
    protected String name;

    @Property(mandatory = true)
    protected EntityFactory entityFactory;

    @Property(mandatory = true)
    protected Set<String> attributes;
    @Property(value = "true")
    protected boolean lastState;

    @SuppressWarnings({"UnusedDeclaration"})
    @Validate
    protected void ready() {
        super.name = name;
        super.entityFactory = entityFactory;
        super.attributes = attributes;
        super.ready();
    }

    @Override
    public synchronized TupleSet getContent(Batch b) {
        Batch headKey = null;
        if (b.compareTo(time) >= 0) {
            headKey = pastContent.floorKey(time);
        } else {
            headKey = pastContent.floorKey(b);
        }
        if (headKey != null) {
            Set<Batch> batches = new TreeSet<Batch>(pastContent.headMap(headKey, !lastState).keySet());
            for (Batch toRemove : batches) {
                pastContent.remove(toRemove);
            }
        }
        if (b.compareTo(time) >= 0)
            return content;
        Map.Entry<Batch, TupleSet> entry = pastContent.floorEntry(b);
        if (entry != null)
            return entry.getValue();
        throw new IllegalStateException("Trying to access to way too old content (keeping only two states)");
    }
}
