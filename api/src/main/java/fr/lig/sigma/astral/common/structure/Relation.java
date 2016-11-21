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

import fr.lig.sigma.astral.common.Batch;

/**
 * Basic relation concept that will be shared among the framework.
 * It specifies the relation R defined in AStrAL
 * @author Loic Petit
 */
public interface Relation extends Entity {
    /**
     * Update the content of the relation for a specific batch. In AStrAL it will verify R(t,i) = {...}
     * @param content The TupleSet representing the content
     * @param b the specific batch
     */
    void update(TupleSet content, Batch b);

    /**
     * Get the content of the relation.
     * If the batch is lower than the first seen batch, returns an empty TupleSet.
     * If the batch does not correspond to a seen batch, then it must return the value for the highest batch
     * lower than this one.
     * It SHOULD be available for each batch lower than the last seen batch. But in many this
     * feature is not needed and a <i>only last content</i> implemented is sufficient.
     * @param b the batch
     * @return Returns R(t) as described before
     */
    TupleSet getContent(Batch b);
}
