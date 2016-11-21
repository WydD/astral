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
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.util.Set;

/**
 *
 */
@Component(immediate = true)
@Provides
public class RelationVolatileImpl extends EventNotifier implements Relation {
    private TupleSet content, previousContent = null;
    private Batch time = Batch.MIN_VALUE, previousTime = null;
    @Property(name = "entityname", mandatory = true)
    private String name;


    @Property
    private boolean lastState = true;
    @Property
    private boolean checkToNotify = true;
    private boolean checkAttributes = true;


    @Property(mandatory = true)
    private EntityFactory entityFactory;

    @Property(mandatory = true)
    private Set<String> attributes;
    private static Logger log = Logger.getLogger(RelationVolatileImpl.class);
    private TupleSet emptyTupleSet;

    @SuppressWarnings({"UnusedDeclaration"})
    @Validate
    private void ready() {
        // Def 3.2, if t < t0, R(t) = \emptyset
        emptyTupleSet = entityFactory.instanciateTupleSet(attributes);
        content = emptyTupleSet;
        previousContent = emptyTupleSet;
    }

    public synchronized void update(TupleSet content, Batch b) {
        if (time.compareTo(b) > 0)
            throw new IllegalArgumentException("New timestamp" + b + " is lower than the older" + time);
        if (checkAttributes) {
            if (!content.getAttributes().equals(getAttributes()))
                throw new IllegalArgumentException("Wrong attribute set inside ");
        }
        //if no change has been made, then we do no need to notify everyone
        if (checkToNotify && this.content.equals(content)) {
            log.debug("No changes have been made...");
            if (log.isTraceEnabled()) {
                log.trace("New content...");
                for (Tuple t : content)
                    log.trace(t);

                log.trace("Old content...");
                for (Tuple t : this.content)
                    log.trace(t);
            }
            return;
        }
        if (lastState) {
            previousContent = this.content;
        }
        previousTime = time;
        this.content = content;
        time = b;
        notifyProcessors(b);
    }

    public Set<String> getAttributes() {
        return content.getAttributes();
    }

    public String getName() {
        return name;
    }

    public synchronized TupleSet getContent(Batch b) {
        if (b.compareTo(time) >= 0) {
            TupleSet content = this.content;
            if (!checkToNotify)
                this.content = emptyTupleSet;
            return content;
        }
        if (previousTime != null && b.compareTo(previousTime) >= 0)
            return previousContent;
        throw new IllegalStateException("Trying to access to way too old content (keeping only two states)");
    }

    public String toString() {
        return name;
    }
}