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

/**
 * Represents a couple of two Comparable
 * It is used to respect the total order on a couple
 */
public class Couple implements Comparable<Couple> {
    Comparable a;
    Comparable b;

    public Couple(Comparable a, Comparable b) {
        if (a instanceof Integer)
            this.a = ((Integer) a).longValue();
        else
            this.a = a;
        if (b instanceof Integer)
            this.b = ((Integer) b).longValue();
        else
            this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Couple couple = (Couple) o;
        return a == couple.a && b == couple.b;
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    /**
     * Compares two couples (a,b) vs (c,d).
     * This order ensures (a,b) <= (c,d) <<==>> a < c || a = c && b < d
     *
     * @param o The comparable couple
     * @return An int representing the order of those couples
     */
    @Override
    public int compareTo(Couple o) {
        int comp = a.compareTo(o.a);
        if (comp == 0) return b.compareTo(o.b);
        return comp;
    }

    @Override
    public String toString() {
        return "(" + a.toString() + "," + b.toString() + ")";
    }
}
