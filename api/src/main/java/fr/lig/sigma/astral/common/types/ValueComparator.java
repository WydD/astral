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

package fr.lig.sigma.astral.common.types;

/**
 * @author Loic Petit
 */
public class ValueComparator {
    @SuppressWarnings({"unchecked"})
    public static int compare(Comparable a, Comparable b, int wrong) {
        try {
            a = Long.parseLong((String) a);
        } catch (Exception ignored) {
        }
        try {
            b = Long.parseLong((String) b);
        } catch (Exception ignored) {
        }
        if (a instanceof Number && b instanceof Number)
            return (int) (((Number) a).doubleValue() - ((Number) b).doubleValue());
        if (a instanceof String && b instanceof String)
            return a.toString().compareTo(b.toString());
        try {
            return a.compareTo(b);
        } catch (Exception e) {
            //By default compare with strings
            return a.toString().compareTo(b.toString()) == 0 ? 0 : wrong;
        }
    }
}
