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

package fr.lig.sigma.astral.common.event;

/**
 * This interface must be declared by those who wants to be notified by the scheduler of each independant event
 * @author Loic Petit
 */
public interface TaskListener {
    /**
     * A new task has been declared. The implementation MUST do an answer to the scheduler when it juges the scheduler to
     * launch the task. If not, the scheduler will wait for it forever. 
     * @param task The task descriptor
     * @see fr.lig.sigma.astral.common.event.EventScheduler#prepareAndLaunchIndependent(IndependentTask)
     */
    void newIndependantTaskDeclared(IndependentTask task);
}
