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

package fr.lig.sigma.astral.core.operators.spread;

import fr.lig.sigma.astral.common.*;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.StreamOperator;
import org.apache.felix.ipojo.annotations.*;

import java.util.*;

/**
 *
 */

@Component
@Provides
@SuppressWarnings({"unchecked"})
public class SingleSpreadImpl extends StreamOperator implements Operator {
    @Requires(id = "in")
    private Stream in;
    private boolean allSpread;
    private String[] attributes = null;
    private int processorId = -1;

    @Property
    private List<String> on;
    @Property
    private String all;
    @Property(value = "false")
    private boolean nospread;

    public void prepare() {
        this.allSpread = all != null;
        String[] attributes = null;
        if (on != null) attributes = on.toArray(new String[on.size()]);
        if (attributes != null && !in.getAttributes().containsAll(new AttributeSet(attributes)))
            throw new IllegalStateException("Attribute set are not compatible");
        if (attributes != null && attributes.length > 0)
            this.attributes = attributes;
        setOutput(createNewFrom(in, in.getAttributes(), "\\" + (allSpread ? "l" : "r") + "hd_{" + Arrays.toString(attributes) + "} " + in.getName()));
        addInput(in, true);
        if (allSpread)
            processorId = scheduler.registerIndependentProcessor(this);

    }

    private long id = 0;
    private int batchId = 0;
    private long lastTimestamp = Long.MIN_VALUE;


    private Map<Comparable, Set<Tuple>> toCommit = new TreeMap<Comparable, Set<Tuple>>();

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        while (in.hasNext(b)) {
            Tuple t = in.pop();
            Comparable bId;
            if (nospread)
                bId = 0;
            else if (attributes == null)
                bId = t.getId();
            else if (attributes.length == 1)
                // Can happen with something like "id"
                bId = t.get(attributes[0]);
            else if (attributes.length == 2)
                // Will rarely happen
                bId = new Couple(t.get(attributes[0]), t.get(attributes[1]));
            else
                // Will very rarely happen
                bId = new Nuplet(t, attributes);
            Set<Tuple> set;
            if (!toCommit.containsKey(bId)) {
                set = new TreeSet<Tuple>();
                toCommit.put(bId, set);
            } else
                set = toCommit.get(bId);
            set.add(t);
        }

        if (toCommit.isEmpty()) return;
        if (lastTimestamp != b.getTimestamp()) {
            batchId = 0;
            lastTimestamp = b.getTimestamp();
            if (allSpread)
                scheduler.pushIndependentEvent(new Batch(b.getTimestamp(), Integer.MAX_VALUE), processorId);
        }

        if (!allSpread || b.getId() == Integer.MAX_VALUE)
            commit(b);
    }

    private void commit(Batch b) throws AxiomNotVerifiedException {
        for (Set<Tuple> entry : toCommit.values()) {
            for (Tuple t : entry)
                output.put(new Tuple(t, id++), batchId);
            batchId++;
        }
        toCommit.clear();
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    private class Nuplet implements Comparable<Nuplet> {
        List<Comparable> list;

        public Nuplet(Tuple t, String[] attributes) {
            for (String a : attributes)
                list.add(t.get(a));
        }

        @Override
        public int compareTo(Nuplet nuplet) {
            Iterator<Comparable> it = nuplet.list.iterator();
            for (Comparable a : list) {
                // Simplified as nuplet and this were built the same way
                int c = a.compareTo(it.next());
                if (c != 0)
                    return c;
            }
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Spread";
    }
}
