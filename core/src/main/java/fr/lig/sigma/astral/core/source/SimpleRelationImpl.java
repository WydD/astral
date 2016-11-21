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

package fr.lig.sigma.astral.core.source;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.source.Source;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@Component
@Provides
public class SimpleRelationImpl extends RelationContainer implements Source {
    private long id = 0;
    @Property
    private final String[] schema = {"id", "temp", Tuple.TIMESTAMP_ATTRIBUTE};

    @Property(mandatory = true)
    private int rate;
    @Property(mandatory = true)
    private int card;
    @Property(mandatory = true)
    private String entityname;

    public void processEvent(Batch batch) throws AxiomNotVerifiedException {
        Set<String> attributes = getAttributes();
        TupleSet ts = entityFactory.instanciateTupleSet(attributes);
        for (int i = 0; i < (int) Math.round(Math.random() * 4); i++) {
            Map<String, Comparable> tupleContent = new HashMap<String, Comparable>();
            if (attributes.contains("id"))
                tupleContent.put("id", (Math.round(Math.random() * 5) + 1));
            if (attributes.contains("temp"))
                tupleContent.put("temp", (int) (Math.round(Math.random() * 10) + 25));
            if (attributes.contains(Tuple.TIMESTAMP_ATTRIBUTE))
                tupleContent.put(Tuple.TIMESTAMP_ATTRIBUTE, batch.getTimestamp());

            Tuple t = new Tuple(tupleContent, id++);
            ts.add(t);
        }
        update(ts, batch);
        if (card-- > 0) {
            /*try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}*/
            scheduler.pushEvent(new Batch(batch.getTimestamp() + rate,0), this);
        } else
            scheduler.togglePush(batch.getTimestamp());
    }

    public String toString() {
        return "SimpleRelation(" + rate + ", " + card + ")";
    }

    @Override
    public void firstSchedule() {
        scheduler.pushEvent(Batch.MIN_VALUE, this);
    }
}