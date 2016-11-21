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

package fr.lig.sigma.astral.core.common.structure.impl;

import fr.lig.sigma.astral.common.Tuple;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

@Component
@Provides
public class TupleSetTreeImpl extends AbstractTupleSetImpl {
    @Property(mandatory = true)
    private Set<String> attributes;

    public TupleSetTreeImpl(Set<String> attributes) {
        this.attributes = attributes;
        ready();
    }
    
    @Validate
    private void ready() {
        setAttributes(attributes);
        setContent(Collections.synchronizedSet(new TreeSet<Tuple>()));
    }

    public boolean isOrdered() {
        return true;
    }
}