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

package fr.lig.sigma.astral.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Common representation of a tuple (or n-uplet) as described in the astral definitions. This class represents a
 * partial function (here by an HashMap) from the space of attributes (here {@link String}) and the space of values
 * (here {@link Comparable}). All tuples does have a physical identifier which is accessible by a specific function
 * {@link #getId()} but also by the function {@link #get(Object)} with the specific attribute {@link #TIMESTAMP_ATTRIBUTE}.
 * <p/>
 * The physical identifier is not a <code>long</code> as foreseen first, it is a Comparable to be able to have, for
 * instance, a couple of identifier as id.
 * <p/>
 * All methods from the HashMap are present and can be used to ease the manipulation.
 * <p/>
 * A tuple is ordered and identifiable by its physical id, therefore the class implements {@link Comparable}.
 */
public class Tuple extends HashMap<String, Comparable> implements Comparable<Tuple>, Serializable {
    /**
     * Special attribute of the physical identifier phi.
     */
    public static final String PHYSICAL_ID = "PHYID";
    /**
     * Special attribute of the timestamp attribute t
     */
    public static final String TIMESTAMP_ATTRIBUTE = "T";

    /**
     * Special value that represents the null value
     */
    public static final Comparable NULL_VALUE = "null";

    private Comparable physicalId;

    /**
     * Default constructor of a tuple. The building of such tuple is in respect with the specification seen above.
     *
     * @param physicalId The physical identifier that will be attributed inside the map
     */
    public Tuple(Comparable physicalId) {
        super();
        super.put(PHYSICAL_ID, physicalId);
        this.physicalId = physicalId;
    }

    /**
     * Build a new tuple from the data of another map (typically another tuple). The map is copied, and the physical
     * identifier is reassigned to be in coherence.
     *
     * @param map        the other map
     * @param physicalId the new physical id
     */
    public Tuple(Map<String, Comparable> map, Comparable physicalId) {
        super(map);
        super.put(PHYSICAL_ID, physicalId);
        this.physicalId = physicalId;
    }

    /**
     * Put a new value inside the tuple. For security and coherence reasons, it is not authorized to change the physical
     * identifier. If possible, it is available to break the {@link fr.lig.sigma.astral.common.AxiomNotVerifiedException#CONSISTENCY_AXIOM}
     * without control of the engine.
     *
     * @param s the attribute
     * @param o the value
     * @return the old value if this call override an entry, null else
     * @throws IllegalArgumentException if the code tries to override the physical identifier
     */
    @Override
    public Comparable put(String s, Comparable o) {
        if (PHYSICAL_ID.equals(s))
            throw new IllegalArgumentException("Trying to override the physical identifier");
        return super.put(s, o);
    }

    /**
     * The list of values in a non-deterministic order (based on the hash) separated by a ','.
     * The physical identifier is never printed.
     *
     * @return A string representing the tuple
     * @see #toString(Iterable)
     */
    @Override
    public String toString() {
        return toString(keySet());
    }

    /**
     * The list of values in the order determined by the attribute iteration given in parameter. Values are separated
     * by a ','. The physical identifier is never printed.
     *
     * @param attributes an iteration of attributes
     * @return A string representing the tuple corresponding to the attributes
     */
    public String toString(Iterable<String> attributes) {
        String s = "";
        for (String a : attributes) {
            if (!a.equals(PHYSICAL_ID))
                s += get(a) + ",";
        }
        s = s.substring(0, s.length() - 1);
        return s;
    }

    /**
     * Get the timestamp present inside the tuple.
     *
     * @return the timestamp present, null if the timestamp is not present
     */
    public Long getTimestamp() {
        return (Long) get(TIMESTAMP_ATTRIBUTE);
    }

    /**
     * Get the physical identifier of the tuple
     *
     * @return the physical identifier given at the beginning
     */
    public Comparable getId() {
        return physicalId;
    }

    /**
     * The {@link Comparable} method to describe the natural order of the tuples.
     *
     * @param o an other Tuple
     * @return the result of the comparison of this tuple's id with the other's id
     */
    public int compareTo(Tuple o) {
        return getId().compareTo(o.getId());
    }
}
