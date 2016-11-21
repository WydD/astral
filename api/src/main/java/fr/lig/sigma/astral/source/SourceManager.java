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

import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.SourceAlreadyExistsException;

import java.util.Dictionary;
import java.util.Set;

/**
 * The source manager provides to a query builder a registry of the declared source.
 */
public interface SourceManager {
    /**
     * Register a new source, the name returned by Entity.getName() will be used to retrieve the source
     * @param s The source
     * @param runtime
     * @throws SourceAlreadyExistsException The source already exists
     */
    void registerSource(Source s, QueryRuntime runtime) throws SourceAlreadyExistsException;

    /**
     * Get the registered source 
     * @param name The name  
     * @return The entity corresponding to this name
     * @throws UnknownSourceException No registered source corresponds to this name
     */
    Source getSource(String name) throws UnknownSourceException;
    
    /**
     * Get a safe entity that may be duplicated in order to give a isolated entity for each query
     * @param name Name of the source
     * @param core The core of the target query
     * @param properties The properties to give to the newly built entity (can be null)
     * @return The created duplicate
     * @throws UnknownSourceException
     */
    Entity newDuplicatedEntity(String name, AstralCore core, Dictionary<String, Object> properties) throws UnknownSourceException;

    /**
     * Get a list of the source names that had been registered 
     * @return A set containing all the registered source names
     */
    Set<String> getRegisteredSources();

    /**
     * Clear the registry
     */
    void clear();

    /**
     * Notify that the given source is down. The Source Manager MUST remove the source from its repository and it
     * must notify the duplicated entry that their is no more event coming from the source
     * @param name The source name
     */
    void sourceDown(String name);

    /**
     * Starts a source runtime
     * @param name The source
     */
    void startSource(String name);

    /**
     * Returns if the source has been registered
     * @param name The source name
     * @return True if the given source name is known
     */
    boolean isRegistered(String name);

}
