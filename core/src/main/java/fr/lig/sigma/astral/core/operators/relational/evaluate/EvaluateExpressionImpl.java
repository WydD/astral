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

package fr.lig.sigma.astral.core.operators.relational.evaluate;

import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpression;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpressionParseException;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.operators.relational.evaluate.TupleEvaluator;

import java.util.Set;

public class EvaluateExpressionImpl implements EvaluateExpression {
    private String target;
    private TupleEvaluator evaluator;
    private Set<String> attribs;
    //private static Logger log = Logger.getLogger(EvaluateExpressionImpl.class);


    public EvaluateExpressionImpl(String eval, String target, ExpressionAnalyzer ea) throws EvaluateExpressionParseException {
        evaluator = ea.getEvaluator(eval);
        attribs = ea.getArguments(eval);
        this.target = target;
    }

    @Override
    public String getTargetAttribute() {
        return target;
    }

    @Override
    public Comparable evaluate(Tuple t) {
        return evaluator.evalTuple(t);
    }

    @Override
    public Set<String> getConcernedAttributes() {
        return attribs;
    }

}
