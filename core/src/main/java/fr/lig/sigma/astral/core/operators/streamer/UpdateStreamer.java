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

package fr.lig.sigma.astral.core.operators.streamer;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.StreamOperator;
import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Loic Petit
 */
public abstract class UpdateStreamer extends StreamOperator {
    private Relation in;
    private boolean fetchOld;
    protected static final Set<String> phyAttributeSet = new HashSet<String>();

    static {
        phyAttributeSet.add(Tuple.PHYSICAL_ID);
    }

    protected String operatorName;
    private static Logger log = Logger.getLogger(UpdateStreamer.class);

    public void setInput(Relation in, boolean fetchOld) {
        this.in = in;
        this.fetchOld = fetchOld;
        setOutput((Stream) entityFactory.instanciateEntity("StreamQueueImpl", operatorName + "(" + in.getName() + ")", new AttributeSet(in.getAttributes(), Tuple.TIMESTAMP_ATTRIBUTE)));
        // Creates a "StreamQueueImpl" <= I do not like that !
        addInput(in, true);
        commit = entityFactory.instanciateTupleSet(getAttributes(), true);
    }

    public int getMaxInputs() {
        return 1;
    }

    private SortedMap<Comparable, Tuple> toCommit = new TreeMap<Comparable, Tuple>();
    private TupleSet commit;

    private long id = 0;

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        TupleSet old = null;
        if (fetchOld) old = in.getContent(b.getClose());// R(t-)
        TupleSet current = in.getContent(b);// R(t)
        if (log.isTraceEnabled())
            log.trace("Comparison between " + b.getClose() + "/" + b + "\n" + old + "\n\n" + current);
        toCommit.clear();
        commit.clear();
        processUpdate(old, current, b);
        for (Map.Entry<Comparable, Tuple> t : toCommit.entrySet()) {
            commit.add(new Tuple(t.getValue(), id++));
        }
        if (log.isTraceEnabled())
            log.trace("Try to commit " + commit + " at " + b);
        putAll(commit, b.getId());
    }

    public abstract void processUpdate(TupleSet old, TupleSet current, Batch b);

    public void put(Tuple t, long timestamp) {
        // A duplicate is made in order to put the timestamp without problems 
        t = (Tuple) t.clone();
        t.put(Tuple.TIMESTAMP_ATTRIBUTE, timestamp);
        // Unfortunately we must provide the stream in the right order at the end so we put the tuple inside a SortedMap
        toCommit.put(t.getId(), t);
    }

    @Override
    public String toString() {
        return operatorName;
    }
}
