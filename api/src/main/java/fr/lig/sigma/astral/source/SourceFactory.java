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

package fr.lig.sigma.astral.source;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.query.AstralCore;

import java.util.Dictionary;
import java.util.Map;

/**
 * This factory is the only component that can create sources. This component is part of the core. It is a scheduler
 * container in order to set the right scheduler to all created sources.
 * @author Loic Petit
 */
public interface SourceFactory {
    /**
     * Instanciate a new source, assign it a correct scheduler (or of), informs the scheduler its presence
     * and finally schedule the first event. 
     * @param name Name of the source class (indexOf searching)
     * @param prop Properties dictionary, contains the arguments that will be sent to the component
     * @return The source
     * @throws SourceAlreadyExistsException When a source with the same name has been created
     * @throws InstanceCreationException When any error occurs
     * @see #prepareSource(Source) 
     */
    Source createSource(String name, Dictionary prop) throws InstanceCreationException, SourceAlreadyExistsException;

     /**
     * Assign a correct scheduler (or of), informs the scheduler its presence
     * and finally schedule the first event.
     * @param s The source
     * @throws SourceAlreadyExistsException When a source with the same name has been created
     * @throws InstanceCreationException When any error occurs
     */
    void prepareSource(Source s) throws SourceAlreadyExistsException, InstanceCreationException;

    /**
     * Reconfigure a source with the given properties.
     * @param source The source object
     * @param prop The set of properties to apply
     */
    void reconfigure(Source source, Dictionary prop);

    /**
     * Returns a map containing the properties and their values of the source
     * @param source The source object
     * @return a map 
     */
    Map<String, Object> getProperties(Source source);

    /**
     * Set the engine used by this factory.
     * @param engine The query engine
     */
    void setEngine(AstralEngine engine);

    void prepareSource(Source source, AstralCore core) throws SourceAlreadyExistsException, InstanceCreationException;
}
