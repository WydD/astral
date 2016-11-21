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

package fr.lig.sigma.astral.query;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.operators.OperatorFactory;
import fr.lig.sigma.astral.handler.HandlerFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.source.SourceFactory;
import fr.lig.sigma.astral.source.SourceManager;

/**
 * An instance of the Astral Core, providing all the services needed to run a query
 */
public interface AstralCore {
    /**
     * Get the source factory
     *
     * @return the source factory
     */
    SourceFactory getSf();

    /**
     * Get the operator factory
     *
     * @return the source factory
     */
    OperatorFactory getOf();

    /**
     * Get a event scheduler
     *
     * @return the event scheduler
     */
    EventScheduler getEs();

    /**
     * Get the global entity factory
     *
     * @return the entity factory
     */
    EntityFactory getEf();

    /**
     * Get the source manager
     *
     * @return the source manager
     */
    SourceManager getSm();

    /**
     * Get the handler factory
     *
     * @return the handler factory
     */
    HandlerFactory getHf();

    /**
     * Get the expression analyzer
     *
     * @return the expression analyzer
     */
    ExpressionAnalyzer getEa();

    /**
     * Get the source factory
     *
     * @return the source factory
     */
    AggregateFactory getAf();

    /**
     * Get the engine that created this core
     *
     * @return the base engine
     */
    AstralEngine getEngine();

    QueryRuntime getQR();

    void setQR(QueryRuntime qr);
}
