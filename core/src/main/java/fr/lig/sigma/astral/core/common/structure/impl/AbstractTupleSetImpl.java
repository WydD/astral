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

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.structure.Index;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.types.ValueComparator;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
public abstract class AbstractTupleSetImpl extends EventNotifier implements TupleSet {
    protected Set<String> attributes;
    private Set<Tuple> content;
    private Map<Set<String>, Index> index = new HashMap<Set<String>, Index>();
    private boolean checkAttributes = true;
    private static final Logger log = Logger.getLogger(AbstractTupleSetImpl.class);

    @Override
    public void clear() {
        content.clear();
    }

    protected void setContent(Set<Tuple> content) {
        this.content = content;
    }

    protected void setAttributes(Set<String> attributes) {
        this.attributes = attributes;
        this.attributes.add(Tuple.PHYSICAL_ID); // ENSURE PHYID IS THERE
    }

    public Set<String> getAttributes() {
        return attributes;
    }

    @Override
    public boolean remove(Tuple t) {
        for (Index idx : index.values()) {
            idx.removeTuple(t);
        }
        return content.remove(t);
    }

    public boolean add(Tuple t) {
        verifyAttributes(t.keySet());
        for (Index idx : index.values()) {
            idx.addTuple(t);
        }
        return content.add(t);
    }

    public boolean addAll(TupleSet ts) {
        verifyAttributes(ts.getAttributes());
        boolean res = true;
        for (Tuple t : ts)
            res &= content.add(t);
        return res;
    }

    public boolean contains(Tuple t) {
        return content.contains(t);
    }

    public int size() {
        return content.size();
    }

    private void verifyAttributes(Set<String> tupleAttribs) {
        if (!checkAttributes)
            return;
        if (!attributes.equals(tupleAttribs))
            throw new IllegalArgumentException("Tuple has not the same attribute set" + "\n" + attributes + " : " + tupleAttribs);
    }

    @Override
    public void addIndex(Set<String> attributes) {
        if (index.containsKey(attributes)) return;
        HashIndexImpl hashIndex = new HashIndexImpl(attributes);
        for (Tuple t : content) {
            hashIndex.addTuple(t);
        }
        index.put(attributes, hashIndex);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractTupleSetImpl)) return false;

        AbstractTupleSetImpl that = (AbstractTupleSetImpl) o;

        if (attributes != null ? !attributes.equals(that.attributes) : that.attributes != null) return false;

        Iterator itThis = this.iterator();
        Iterator itThat = that.iterator();
        while (itThis.hasNext() && itThat.hasNext()) {
            Tuple tThis = (Tuple) itThis.next();
            Tuple tThat = (Tuple) itThat.next();
            for (String a : attributes) {
                if (Tuple.PHYSICAL_ID.equals(a)) continue;
                if (ValueComparator.compare(tThis.get(a), tThat.get(a), 1) != 0)
                    return false;
            }
        }
        return !itThat.hasNext() && !itThis.hasNext();
    }

    @Override
    public int hashCode() {
        int result = attributes != null ? attributes.hashCode() : 0;
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String s = "";
        for (Tuple t : this) {
            s += "" + t + "\n";
        }
        if (s.length() > 0)
            s = s.substring(0, s.length() - 1);
        return s;
    }

    public Iterator<Tuple> iterator() {
        return content.iterator();
    }

    @Override
    public Collection<Tuple> fetchTupleFromValue(Set<String> args, Map<String, Comparable> values) {
        if (!attributes.containsAll(args)) return null;
        Index i = index.get(args);
        if (i == null) {
            List<Tuple> tuples = new LinkedList<Tuple>();
            fullSearch(args, values, tuples);
            return tuples.isEmpty() ? null : tuples;
        }
        return i.fetchData(values);
    }

    private void fullSearch(Set<String> args, Map<String, Comparable> values, List<Tuple> tuples) {
        for (Tuple t : content) {
            boolean valid = true;
            for (String a : args) {
                valid &= values.get(a).equals(t.get(a));
            }
            if (valid)
                tuples.add(t);
        }
    }
}
