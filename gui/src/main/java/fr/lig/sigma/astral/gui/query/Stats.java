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

package fr.lig.sigma.astral.gui.query;

/**
 * @author Loic Petit
 */
public class Stats {
    long sum = 0;
    long min = Long.MAX_VALUE;
    long max = 0;
    long n = 0;

    public void putData(long t) {
        sum += t;
        if (t < min) min = t;
        if (t > max) max = t;
        n++;
    }

    public String toString() {
        String s = "Stats per timestamp :\n";
        s += "\tMin: " + min / 1000000.0 + "ms\n";
        s += "\tMax: " + max / 1000000.0 + "ms\n";
        s += "\tAvg: " + Math.round(sum / 1000.0 / n)/1000.0 + "ms\n";

        return s;
    }

}
