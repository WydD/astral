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

package fr.lig.sigma.astral.core.operators.relational;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class PiRelationalOperation implements UnaryRelationalOperation {
    private Set<String> projection;

    @Property(mandatory = true)
    private Map<String, Collection<String>> parameters;

    @Validate
    public void ready() throws Exception {
        projection = new AttributeSet(parameters.get("attributes"));
    }

    @Override
    public String getOperationName() {
        return "Projection";
    }

    @Override
    public Tuple compute(Tuple t) {
        Tuple n = new Tuple(t.getId());
        for (String attrib : projection) {
            if (!Tuple.PHYSICAL_ID.equals(attrib))
                n.put(attrib, t.get(attrib));
        }
        return n;
    }
}