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

package fr.lig.sigma.astral;

import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.operators.OperatorFactory;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.KnowledgeBase;
import fr.lig.sigma.astral.query.QueryChangedListener;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.SourceFactory;
import fr.lig.sigma.astral.source.SourceManager;

import java.util.Collection;
import java.util.List;

/**
 * The base engine of Astral. This component is a singleton and is the entry point of the user to create and declare
 * queries.
 */
public interface AstralEngine {
    /**
     * Build a new and independent core. Each core can run only one single query.
     *
     * @return the created core
     */
    AstralCore createCore();

    /**
     * Get the global source factory.
     *
     * @return The unique source factory
     */
    SourceFactory getGlobalSf();

    /**
     * Get the global source manager
     *
     * @return The unique source manager
     */
    SourceManager getGlobalSm();

    /**
     * Get the expression analyzer
     */
    ExpressionAnalyzer getGlobalEA();

    /**
     * Declare a query and associates a generated UUID for the displayname
     *
     * @param core the core associated to the query
     * @param out  the top-level of the query
     * @return The built runtime to control the execution of the query
     * @see fr.lig.sigma.astral.query.QueryStatus
     * @see #declareQuery(fr.lig.sigma.astral.query.AstralCore, fr.lig.sigma.astral.common.structure.Entity, String)
     * @see java.util.UUID
     */
    QueryRuntime declareQuery(AstralCore core, Entity out);

    /**
     * Declare a new query inside the engine. The core and the entity will be stored inside a structure to be retrieve
     * at each instant. Once declared the status INITIALIZED is assigned and an event is fired.
     *
     * @param core the core associated to the query
     * @param out  the top-level of the query
     * @param name Display name of the query
     * @return The built runtime to control the execution of the query
     * @see fr.lig.sigma.astral.query.QueryStatus
     */
    QueryRuntime declareQuery(AstralCore core, Entity out, String name);

    /**
     * List all the queries associated to this engine
     *
     * @return A list of their query runtime
     */
    Collection<QueryRuntime> getDeclaredQueries();

    /**
     * Get a query that has been previously declared
     *
     * @param name The query name
     * @return The QueryRuntime corresponding to the name (null if not found)
     */
    QueryRuntime getDeclaredQuery(String name);

    QueryRuntime getDeclaredQueryFromThread(Thread t);

    /**
     * Ask the engine to be notified when a query change its status
     *
     * @param listener the listener
     * @see fr.lig.sigma.astral.query.QueryStatus
     */
    void addQueryChangedListener(QueryChangedListener listener);

    long getServiceId(Object servicePojo);

    Object getServiceFromId(long id);

    KnowledgeBase getKnowledgeBase();
}
