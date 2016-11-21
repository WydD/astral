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

import fr.lig.sigma.astral.common.Tuple;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class AttributeSet extends HashSet<String> {
    public AttributeSet(String[] attribs) {
        for(String a : attribs)
            add(a);
    }
    
    public AttributeSet(Collection<String> copy) {
        for(String a : copy)
            add(a);
    }
    public AttributeSet(Collection<String> copy, String attrib) {
        this(copy);
        add(attrib);
    }

    public AttributeSet() {
    }

    public static String string(Iterable<String> set) {
        String s = "";
        for(String a : set) {
            if(!Tuple.PHYSICAL_ID.equals(a))
                s += a+",";
        }
        if(s.isEmpty()) return "";
        s = s.substring(0, s.length()-1);
        return s;
    }
}
