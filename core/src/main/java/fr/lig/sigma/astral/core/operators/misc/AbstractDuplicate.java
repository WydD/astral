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

package fr.lig.sigma.astral.core.operators.misc;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.operators.misc.Duplicate;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.source.SourceBlocker;
import org.apache.log4j.Logger;

import java.util.Dictionary;
import java.util.Vector;

/**
 *
 */
public class AbstractDuplicate<E extends Entity> extends EventNotifier implements Duplicate<E> {
    protected E in;
    private String implType;
    protected AstralCore core;
    protected Vector<E> duplicates = new Vector<E>();
    protected Vector<SourceBlocker> blockers = new Vector<SourceBlocker>();

    public AbstractDuplicate(E in, String implType, AstralCore core) {
        this.in = in;
        this.implType = implType;
        this.core = core;
        if (in instanceof SchedulerContainer)
            setScheduler(in.getScheduler());
        in.registerNotifier(this);
        setUniqueChild(in);
    }

    public int getMaxInputs() {
        return 1;
    }

    public int getSize() {
        return duplicates.size();
    }

    @Override
    public E getSource() {
        return in;
    }

    public synchronized E addDuplicate(AstralCore core, Dictionary<String, Object> properties) {
        EventNotifier duplicate = (EventNotifier) core.getEf().instanciateEntity(implType, in.getName(), in.getAttributes(), properties);
        duplicate.setScheduler(core.getEs());
        getScheduler().addTimeChangeListener(core.getEs());

        final EventProcessor[] waiters = {in};
        duplicate.setChilds(waiters);

        duplicates.add((E) duplicate);
        return (E) duplicate;
    }

    public synchronized void sourceDown() {
        log.debug("The source is down !");
        for (SourceBlocker block : blockers) {
            block.releaseDefinitely();
        }
    }

    private Logger log = Logger.getLogger(AbstractDuplicate.class);

    protected synchronized void release(Batch b) {
        for (E dup : duplicates) {
            dup.getScheduler().pushEvent(b, dup);
        }
    }
}
