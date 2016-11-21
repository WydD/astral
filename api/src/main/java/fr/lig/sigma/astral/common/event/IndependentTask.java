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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventScheduler;

import java.io.Serializable;

/**
 * Descriptor of an independent task. This object is given by the EventScheduler service when a new
 * independent event is declared. This object can be serialized and be send through network, thanks to
 * the processorId.
 * @author Loic Petit
 */
public class IndependentTask implements Serializable, Comparable {
    private Batch batch;
    private int processorId;
    private long pushValue;

    /**
     * Creates the task description
     * @param batch the batch id
     * @param processorId processorId
     * @param scheduler scheduler 
     */
    public IndependentTask(Batch batch, int processorId, EventScheduler scheduler) {
        this.batch = batch;
        this.processorId = processorId;
        //updatePush(scheduler);
    }

    /**
     * Get the id of the independent processor
     * @return the id
     */
    public int getProcessorId() {
        return processorId;
    }

    /**
     * Set the push value based on the scheduler for push transmission reasons
     * @param scheduler scheduler
     */
    public void updatePush(EventScheduler scheduler) {
        //pushValue = scheduler.getPush();
    }

    /**
     * Get the push value
     * @return the value
     */
    public long getPush() {
        return pushValue;
    }

    /**
     * Verify the equality over timestamp and processorId
     * @param o An independant task
     * @return true if timestamp = o.timestamp & processorId = o.processorId 
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndependentTask)) return false;

        IndependentTask that = (IndependentTask) o;

        return processorId == that.processorId && !(batch != null ? !batch.equals(that.batch) : that.batch != null);

    }

    @Override
    public int hashCode() {
        int result = batch != null ? batch.hashCode() : 0;
        result = 31 * result + processorId;
        return result;
    }

    /**
     * Compares this description with another by timestamp
     * @param o An independant task
     * @return timestamp - o.timestamp
     */
    public int compareTo(Object o) {
        return batch.compareTo(((IndependentTask) o).getBatch());
    }

    @Override
    public String toString() {
        return "IndependentTask(" + batch + ", " + processorId + ", "+pushValue+")";
    }

    /**
     * Get the batch id
     * @return the i
     */
    public Batch getBatch() {
        return batch;
    }
}
