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

/**
 * Enumeration that indicates the status of a query runtime
 */
public enum QueryStatus {
    /**
     * The query is initialized. The query runtime has this value by default at the creation.
     */
    INITIALIZED,
    /**
     * The query is running normally.
     */
    RUNNING,
    /**
     * The runtime of the query is in hold mode and wait for a release.
     */
    HOLD,
    /**
     * The query execution has been interrupted.
     */
    INTERRUPTED,
    /**
     * The query has finished its treatment and has encountered no errors
     */
    FINISHED,
    /**
     * The query has thrown an exception of axiom breaking.
     */
    E_AXIOM,
    /**
     * The query has thrown an exception of illegal state.  
     */
    E_ILLEGAL,
    /**
     * The query has thrown an unknown exception that was not foreseen. This must be absolutely reported to the
     * developers of the core or the sources/operators considering the trace that had been output inside log. 
     */
    E_UNKNOWN;

    public boolean isDead() {
        return compareTo(INTERRUPTED) >= 0;
    }
}
