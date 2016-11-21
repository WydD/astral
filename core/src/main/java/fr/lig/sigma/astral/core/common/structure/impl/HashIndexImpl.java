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
import fr.lig.sigma.astral.common.structure.Index;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class HashIndexImpl implements Index {
    private List<String> attributes;
    private HashMap<List<Comparable>, Set<Tuple>> hashTableRight = new HashMap<List<Comparable>, Set<Tuple>>();

    public HashIndexImpl(Set<String> attributes) {
        this.attributes = new ArrayList<String>(attributes);
    }

    public void addTuple(Tuple t) {
        List<Comparable> values = buildListFromAttributes(t);

        Set<Tuple> list = hashTableRight.get(values);
        if (list == null) {
            list = new HashSet<Tuple>();
            hashTableRight.put(values, list);
        }
        list.add(t);
    }

    @Override
    public void removeTuple(Tuple t) {
        List<Comparable> values = buildListFromAttributes(t);

        Set<Tuple> list = hashTableRight.get(values);
        if (list == null)
            return;
        list.remove(t);
        if (list.isEmpty())
            hashTableRight.remove(values);
    }


    private List<Comparable> buildListFromAttributes(Map<String, Comparable> t) {
        List<Comparable> values = new ArrayList<Comparable>(attributes.size());
        for (String attribute : attributes) {
            values.add(t.get(attribute));
        }
        return values;
    }


    @Override
    public Collection<Tuple> fetchData(Map<String, Comparable> values) {
        List<Comparable> index = buildListFromAttributes(values);
        return hashTableRight.get(index);
    }
}
