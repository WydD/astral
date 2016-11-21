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

package fr.lig.sigma.astral.common.structure;

import fr.lig.sigma.astral.common.Tuple;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.io.Serializable;

/**
 * Represents a set of tuples sharing the same attributes set
 * @author Loic Petit
 */
public interface TupleSet extends Iterable<Tuple>, Serializable {
    /**
     * @return The common attribute set
     */
    Set<String> getAttributes();

    /**
     * Add a tuple inside the set
     * @param t The tuple
     * @return true if this set did not already contain the specified element.
     */
    boolean add(Tuple t);
    /**
     * Add each tuple of the given tuple set inside the set
     * @param t The tuple set
     * @return true if this set changed as a result of the call.
     */
    boolean addAll(TupleSet t);

    /**
     * Verify if a tuple is inside the set
     * @param t The tuple
     * @return True if and only if an element o inside the set matches o.equals(t).
     */
    boolean contains(Tuple t);

    /**
     * Clear the content of the set.
     */
    void clear();

    /**
     * Remove a tuple from this set
     * @param t The tuple
     * @return true if it was present
     */
    boolean remove(Tuple t);

    /**
     * Get the size of the set
     * @return The cardinal of the set
     */
    int size();

    /**
     * Informs the implementation if the iteration over the set will be ordered considering the
     * {@link fr.lig.sigma.astral.common.Tuple#compareTo(fr.lig.sigma.astral.common.Tuple)} function. In some particular
     * cases, mainly joins or unions, the order must be respected to conforms to the axioms.  
     * @return True if the iterator ensures the right order of the tuple (considering compareTo, therefore, the PHYID)
     */
    boolean isOrdered();

    void addIndex(Set<String> attributes);

    Collection<Tuple> fetchTupleFromValue(Set<String> args, Map<String, Comparable> values);
}
