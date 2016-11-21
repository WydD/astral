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
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 */
@Component
@Provides
public class DynamicRelationFromStaticImpl extends EventNotifier implements DynamicRelation {
    protected TupleSet content;
    protected Batch time = Batch.MIN_VALUE;
    @Property(name = "entityname", mandatory = true)
    protected String name;
    
    @Property(mandatory = true)
    protected EntityFactory entityFactory;

    @Property(mandatory = true)
    protected Relation in;
    
    @Property(mandatory = true)
    protected Set<String> attributes;

    protected TreeMap<Batch, TupleSet> pastContent;
    private static Logger log = Logger.getLogger(DynamicRelationFromStaticImpl.class);

    @SuppressWarnings({"UnusedDeclaration"})
    @Validate
    protected void ready() {
        // Def 3.2, if t < t0, R(t) = \emptyset 
        content = entityFactory.instanciateTupleSet(attributes);
        pastContent = new TreeMap<Batch, TupleSet>();
        pastContent.put(time,content);
    }

    public void update(TupleSet content, Batch b) {
        in.update(content, b);
    }

    public Set<String> getAttributes() {
        return in.getAttributes();
    }

    public String getName() {
        return in.getName();
    }

    public TupleSet getContent(Batch b) {
        return in.getContent(b);
    }

    public String toString() {
        return getName();
    }

    @Override
    public TupleSet getInsertedTuples(Batch b) {
        TupleSet current = in.getContent(b);
        TupleSet old = in.getContent(b.getClose());
        return getDelta(current, old);
    }

    @Override
    public TupleSet getDeletedTuples(Batch b) {
        TupleSet current = in.getContent(b);
        TupleSet old = in.getContent(b.getClose());
        return getDelta(old, current);
    }

    private TupleSet getDelta(TupleSet current, TupleSet old) {
        TupleSet res = entityFactory.instanciateTupleSet(getAttributes());
        for (Tuple t : current) {
            if (!old.contains(t))
                res.add(t);
        }
        return res;
    }


    @Override
    public void update(Batch b, TupleSet insertedTuples, TupleSet deletedTuples) {
        TupleSet content = entityFactory.instanciateTupleSet(getAttributes());
        if(insertedTuples.equals(deletedTuples))
            return;
        content.addAll(getContent(b.getClose()));
        for(Tuple t : deletedTuples)
            content.remove(t);
        content.addAll(insertedTuples);
        in.update(content, b);
    }
}
