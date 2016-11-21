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

import java.util.Dictionary;
import java.util.Properties;
import java.util.Set;

/**
 * Factory in order to creates entity as well as tuple sets.
 * @author Loic Petit
 */
public interface EntityFactory {
    /**
     * Creates a new entity
     * @param type The type of the entity (its ClassName, like StreamQueue)
     * @param name The AStrAL name of the future entity
     * @param attribs And finally its common attributes
     * @return The created entity
     */
    Entity instanciateEntity(String type, String name, Set<String> attribs);

    /**
     * Creates a new entity
     * @param type The type of the entity (its ClassName, like StreamQueue)
     * @param name The AStrAL name of the future entity
     * @param attribs And finally its common attributes
     * @param props The properties that will be given to the factory
     * @return The created entity
     */
    Entity instanciateEntity(String type, String name, Set<String> attribs, Dictionary<String, Object> props);

    /**
     * Creates a new tuple set. For performances reasons it is advised to use quick call (no service detection for
     * instance) as this method is often called
     * @param attribs The common attributes
     * @param ordered Specify if the TupleSet must be ordered or not
     * @return The created TupleSet
     */
    TupleSet instanciateTupleSet(Set<String> attribs, boolean ordered);

    /**
     * Creates a new tuple set. For performances reasons it is advised to use quick call (no service detection for
     * instance) as this method is often called.
     *
     * Note: it is not advised to use this method as programmers should always specify if the tuple set needs to be
     * ordered of not.
     * @param attribs The common attributes
     * @return The created TupleSet that can be ordered or not
     */
    TupleSet instanciateTupleSet(Set<String> attribs);

    /**
     * Ensures that a tuple set is correctly ordered (based on the method isOrdered). If it is, the method just returns
     * the given content. Else a new TupleSet is built and the content is sorted to give a brand new TupleSet.
     *
     * This method call is legitimate but the optimizer should avoid the worst case. 
     *
     * @param content The tuple set to ensure
     * @return An ordered tuple set with the same content
     * @see fr.lig.sigma.astral.common.structure.TupleSet#isOrdered()
     */
    TupleSet ensureOrderedTupleSet(TupleSet content);


    /**
     * Ensures that a relation has the APIs to support dynamic operations. If it is a native dynamic relation
     * then r is returned (with cast). If not, then r is wrapped around an implementation that implements the
     * different functions with the standard relation.
     * @param r A relation
     * @return A relation that respects the DynamicRelation pattern
     */
    DynamicRelation ensureDynamicRelation(Relation r);

    void disposeEntity(Entity o);
}
