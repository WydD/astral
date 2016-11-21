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

package fr.lig.sigma.astral.preference;

import fr.lig.sigma.astral.common.structure.TupleSet;

import java.util.Set;

/**
 * @author Loic Petit
 */
public interface PartialComparator<E> {
    void setContent(Set<String> attribute);
    /**
     * Similar to comparator but can return null if the two elements are not comparable
     * @param e1 First element
     * @param e2 Second element
     * @return An Integer < 0 if e1 < e2, > 0 if e1 > e2, = 0 if e1 == e2 and null if e1 and e2 are not comparable
     */
    Integer compare(E e1, E e2);
}
