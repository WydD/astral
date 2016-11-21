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

package fr.lig.sigma.astral.designation;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import fr.lig.sigma.astral.source.Source;

/**
 * Represents a fragmented stream. It is an entity as a single relation or stream.
 */
public interface FragmentedStream extends Source {
    /**
     * Get the 
     * @return
     */
    public String getIdAttribute();
    /**
     * Get a specific fragment
     *
     * @param id The fragment's id
     * @return
     */
    public Stream getFragment(Comparable id);

    /**
     * Get a relation representing the CS = C[id/L] 
     * @param optional
     * @param fixture
     * @return
     */
    public Relation getCS(Condition optional, Batch fixture);
}
