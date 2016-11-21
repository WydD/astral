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

import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.source.Source;

import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Set;

/**
 * Runtime of a query. This class provides all utility methods to manipulate a query.
 */
public interface QueryRuntime {
    /**
     * Get the unique core assigned to the query
     *
     * @return the core
     */
    AstralCore getCore();

    /**
     * Get the top level of the query
     *
     * @return the corresponding entity
     */
    Entity getOut();

    /**
     * Attach a handler to this query
     *
     * @param h The handler
     * @throws fr.lig.sigma.astral.common.WrongAttributeException
     *          If the required attributes are not present
     */
    void attachNewHandler(Handler<Entity> h) throws Exception;

    void setQueryNode(QueryNode node);

    /**
     * Get the handler attached to this query
     *
     * @return the corresponding handler
     */
    List<Handler<Entity>> getHandlers();

    String getDisplayName();

    /**
     * Start the query. A new thread is then created and will run in background while there is events inside the event
     * stack. Once the thread is created and launched, the status of the query switch to its status to RUNNING.
     */
    void start();

    /**
     * Not yet implemented... Try to hold the evaluation of the query. Once the hold is obtained the status changes to
     * HOLD.
     */
    void hold() throws InterruptedException;

    /**
     * Not yet implemented... Resume the query. Once resumed the status is changed to RUNNING again
     */
    void release();

    /**
     * Interrupt the execution of the query. Fires immediately the status change to INTERRUPTED. Developers must focus
     * on the treatment of the InterruptedException that all wait-like procedures may fire. If this exception is ignored
     * and retries to do anything else, then the query will never be stopped.
     */
    void stop();

    /**
     * Get the current status of the query
     *
     * @return the status
     */
    QueryStatus getStatus();

    /**
     * Check if the runtime is currently computing some operation
     *
     * @return true if an operation is running, false if waiting for job
     */
    boolean isComputing();

    /**
     * Join the execution of this runtime. The process will pause the current thread, waiting for the end of the query,
     * or an interrupted exception.
     */
    void join();

    void dispose();

    QueryNode getQueryNode();

    Thread getThread();
}
