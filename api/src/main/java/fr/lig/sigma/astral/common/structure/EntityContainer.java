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

/**
 * Particular interface used to be a proxy on an other entity.
 * @see fr.lig.sigma.astral.common.structure.containers.AbstractEntityContainer
 * @author Loic Petit
 */
public interface EntityContainer<E> extends Entity {
    /**
     * Binds an entity factory in order to create the proxy entity. The implementation MUST ensure that this method
     * is called before any functional method. In the core operators, this method is called at the component binding
     * level.
     * 
     * @param entityFactory the factory
     */
    void bindEntityFactory(EntityFactory entityFactory);

    /**
     * Set the output proxy and binds the scheduler
     * @param output The proxy entity
     */
    public void setOutput(E output);
}
