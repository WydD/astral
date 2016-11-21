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
import fr.lig.sigma.astral.common.event.EventNotifier;
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
public class RelationHashImpl extends EventNotifier implements Relation {
    protected TupleSet content;
    protected Batch time = Batch.MIN_VALUE;
    @Property(name = "entityname", mandatory = true)
    protected String name;

    @Property(mandatory = true)
    protected EntityFactory entityFactory;

    @Property(mandatory = true)
    protected Set<String> attributes;

    protected TreeMap<Batch, TupleSet> pastContent;
    private static Logger log = Logger.getLogger(RelationHashImpl.class);
    private boolean checkAttributes = true;
    private boolean checkToNotify = true;

    @SuppressWarnings({"UnusedDeclaration"})
    @Validate
    protected void ready() {
        // Def 3.2, if t < t0, R(t) = \emptyset 
        content = entityFactory.instanciateTupleSet(attributes);
        pastContent = new TreeMap<Batch, TupleSet>();
        pastContent.put(time, content);
    }

    public synchronized void update(TupleSet content, Batch b) {
        if (time.compareTo(b) > 0)
            throw new IllegalArgumentException("New timestamp" + b + " is lower than the older" + time);
        if (checkAttributes) {
            if (!content.getAttributes().equals(getAttributes()))
                throw new IllegalArgumentException("Wrong attribute set inside ");
        }
        //if no change has been made, then we do no need to notify everyone
        if (checkToNotify && this.content.equals(content)) return;
        if (log.isTraceEnabled())
            log.trace("Receiving new content for " + b + ":{\n" + content + "\n}");

        pastContent.put(time, this.content);
        this.content = entityFactory.instanciateTupleSet(attributes);
        this.content.addAll(content);

        time = b;

        notifyProcessors(b);
    }

    public Set<String> getAttributes() {
        return content.getAttributes();
    }

    public String getName() {
        return name;
    }

    public TupleSet getContent(Batch b) {
        if (b.compareTo(time) >= 0)
            return content;
        Map.Entry<Batch, TupleSet> entry = pastContent.floorEntry(b);
        return entry.getValue();
    }

    public String toString() {
        return name;
    }
}
