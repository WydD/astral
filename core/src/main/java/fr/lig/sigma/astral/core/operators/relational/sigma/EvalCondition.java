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

package fr.lig.sigma.astral.core.operators.relational.sigma;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpressionParseException;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.operators.relational.evaluate.TupleEvaluator;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;

import java.util.Set;

/**
 *
 */
public class EvalCondition implements Condition {
    private ExpressionAnalyzer ea;
    private TupleEvaluator evaluator;
    private Set<String> attribs;

    public EvalCondition(String condition, ExpressionAnalyzer ea) throws EvaluateExpressionParseException {
        evaluator = ea.getEvaluator(condition);
        attribs = ea.getArguments(condition);
    }
    @Override
    public boolean evaluate(Tuple t) {
        Comparable res = evaluator.evalTuple(t);
        if(res instanceof Boolean)
            return (Boolean) res;
        if(res instanceof Number)
            return ((Number) res).byteValue() != 0;
        return Double.parseDouble(res.toString()) != 0;
    }

    @Override
    public Set<String> getConcernedAttributes() {
        return attribs;
    }

    @Override
    public String toString() {
        return evaluator.toString();
    }
}
