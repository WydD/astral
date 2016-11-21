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
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.Map;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class SigmaRelationalOperation implements UnaryRelationalOperation {
    private Condition c;

    @Property(mandatory = true)
    Map<String, String> parameters;
    @Property(mandatory = true)
    AstralEngine engine;

    @Validate
    public void ready() throws Exception {
        c = engine.getGlobalEA().buildCondition(parameters.get("condition"));
    }

    @Override
    public String getOperationName() {
        return "Selection";
    }

    @Override
    public Tuple compute(Tuple t) {
        if(c.evaluate(t))
            return t;
        return null;
    }
}