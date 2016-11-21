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

package fr.lig.sigma.astral.core.common.structure;

import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.*;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.core.common.structure.impl.TupleSetHashImpl;
import fr.lig.sigma.astral.core.common.structure.impl.TupleSetTreeImpl;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Instantiate
@Component
@Provides
public class EntityFactoryImpl extends AbstractPojoFactory<Entity> implements EntityFactory {
    @Requires(optional = true, filter = "(factory.name=*common.structure.impl*)", specification = "org.apache.felix.ipojo.Factory")
    private List<Factory> factories;

    protected Collection<Factory> getFactories() {
        return factories;
    }

    public Entity instanciateEntity(String type, String name, Set<String> attribs, Dictionary<String,Object> dico) {
        dico.put("entityname", name);
        dico.put("attributes", attribs);
        dico.put("entityFactory", this);
        try {
            return instanciatePojoFromName(type, dico);
        } catch (InstanceCreationException e) {
            throw new RuntimeException(e);
        }
    }

    public Entity instanciateEntity(String type, String name, Set<String> attribs) {
        return instanciateEntity(type, name, attribs, new Hashtable<String, Object>());
    }

    public TupleSet instanciateTupleSet(Set<String> attribs, boolean ordered) {
        if(ordered) 
            return new TupleSetTreeImpl(attribs);
        else
            return new TupleSetHashImpl(attribs);
    }

    /**
     * Using this method is very slow 
     */
    @Deprecated
    public TupleSet instanciateTupleSetWithOSGi(Set<String> attribs, boolean ordered) {
        Dictionary<String, Object> dico = new Hashtable<String, Object>();
        dico.put("attributes", attribs);
        String type;
        if(ordered)
            type = "TupleSetTreeImpl";
        else
            type = "TupleSetHashImpl";

        try {
            return (TupleSet) instanciatePojoFromName(type, dico);
        } catch (InstanceCreationException e) {
            throw new RuntimeException(e);
        }
    }
    public TupleSet instanciateTupleSet(Set<String> attribs) {
        return instanciateTupleSet(attribs, true);
    }

    public TupleSet ensureOrderedTupleSet(TupleSet content) {
        TupleSet res;
        if (!content.isOrdered()) {
            // Implementation of the query builder should try to skip this step
            res = instanciateTupleSet(content.getAttributes(), true);
            res.addAll(content);
        } else
            res = content;
        return res;
    }

    public DynamicRelation ensureDynamicRelation(Relation r) {
        if(r instanceof DynamicRelation)
            return (DynamicRelation) r;
        Dictionary dic = new Hashtable<String, Object>();
        dic.put("in", r);
        Entity res = instanciateEntity("DynamicRelationFromStatic", r.getName(), r.getAttributes(), dic);
        return (DynamicRelation) res;
    }

    @Override
    public void disposeEntity(Entity o) {
        dispose(o);
    }
}
