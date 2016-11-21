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

package fr.lig.sigma.astral.common;

import java.io.Serializable;

/**
 *
 */
public class Batch implements Comparable<Batch>, Serializable {
    private long timestamp;
    private int id;
    public static final Batch MIN_VALUE = new Batch(Long.MIN_VALUE, Integer.MIN_VALUE);
    public static final Batch MAX_VALUE = new Batch(Long.MAX_VALUE, Integer.MAX_VALUE);

    public Batch(long timestamp, int id) {
        this.timestamp = timestamp;
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "("+ timestamp + "," + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o != null && o instanceof Batch && id == ((Batch) o).id && timestamp == ((Batch) o).timestamp;

    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + id;
        return result;
    }

    /**
     * As precised in the batch section, the order is alphabetical
     * @param batch
     * @return An int < 0 if this < batch, > 0 if this > batch and = 0 if they are equals
     */
    @Override
    public int compareTo(Batch batch) {
        if(batch == this) return 0;
        long t = batch.getTimestamp();
        if(timestamp < t) return -1;
        if(timestamp > t) return 1;
        int i = batch.getId();
        if(id < i) return -1;
        if(id > i) return 1;
        return 0;
    }

    /**
     * Get a batch infinitely close and lower to this one but not equals   
     * @return (t,i)^-
     */
    public Batch getClose() {
        if(this.compareTo(MIN_VALUE) == 0) return this;
        if(id > 0)
            return new Batch(timestamp, id-1);
        return new Batch(timestamp-1, Integer.MAX_VALUE);
    }

    public static Batch parseBatch(String s) {
        String[] parts = s.substring(1,s.length()-1).split("\\s*,\\s*");
        return new Batch(Long.parseLong(parts[0]), Integer.parseInt(parts[1]));
    }
}
