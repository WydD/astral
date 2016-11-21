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
import fr.lig.sigma.astral.core.operators.relational.evaluate.EvaluateExpressionImpl;
import fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpression;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.util.Map;
import java.util.Set;

@Component
@Provides
public class EvaluateRelationalOperation implements UnaryRelationalOperation {
    private Set<String> inputAttributes;
    private EvaluateExpression expr;
    @Property(mandatory = true)
    private Map<String, String> parameters;
    @Property(mandatory = true)
    private AstralEngine engine;

    @Validate
    public void ready() throws Exception {
        String eval = parameters.get("expression");
        String target = parameters.get("to");
        expr = new EvaluateExpressionImpl(eval, target, engine.getGlobalEA());
    }

    @Override
    public String getOperationName() {
        return "Evaluate";
    }

    @Override
    public Tuple compute(Tuple t) {
        Tuple n = (Tuple) t.clone();
        n.put(expr.getTargetAttribute(), expr.evaluate(t));
        return n;
    }
}
