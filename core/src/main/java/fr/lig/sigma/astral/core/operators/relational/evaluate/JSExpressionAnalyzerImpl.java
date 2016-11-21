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

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.core.operators.relational.sigma.EvalCondition;
import fr.lig.sigma.astral.operators.relational.evaluate.EvaluateExpressionParseException;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.operators.relational.evaluate.TupleEvaluator;
import fr.lig.sigma.astral.operators.relational.evaluate.UserFunction;
import fr.lig.sigma.astral.operators.relational.sigma.Condition;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.debug.DebuggableScript;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Loic Petit
 */

@Component(immediate = true)
@Provides
@Instantiate
public class JSExpressionAnalyzerImpl implements ExpressionAnalyzer {
    private Scriptable scope;
    private Context cx;
    private static final Logger log = Logger.getLogger(JSExpressionAnalyzerImpl.class);
    private HashMap<String, Semaphore> locks = new HashMap<String, Semaphore>();

    private class Region {
        int begin;
        int end;

        private Region(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }
    }

    @Validate
    private void validate() {
        cx = Context.enter();
        scope = cx.initStandardObjects(null);
        Context.exit();
    }

    private Iterator<Region> computeStrings(String eval) {
        Matcher match = Pattern.compile("'([^']\\'?)*'").matcher(eval);
        List<Region> regions = new LinkedList<Region>();
        while (match.find()) {
            regions.add(new Region(match.start(), match.end()));
        }
        return regions.iterator();
    }

    private synchronized String analyze(String eval, Set<String> attributes) {
        if (attributes == null) {
            return eval;
        }
        Iterator<Region> regions = computeStrings(eval);
        Region region = null;
        if (regions.hasNext()) region = regions.next();
        Matcher match = Pattern.compile("[a-zA-Z][.a-zA-Z_0-9]*").matcher(eval);
        while (match.find()) {
            String group = match.group();
            while (region != null && region.end < match.end()) {
                if (regions.hasNext()) region = regions.next();
                else region = null;
            }
            String base = null;
            if (group.contains(".")) {
                base = group.substring(0, group.indexOf('.'));
            }

            if (!isKeyword(base) && (region == null || (region.begin > match.start()))) {
                if (eval.length() > match.end() && eval.charAt(match.end()) == '(' && !group.contains(".")) {
                    // User Function
                    waitForFunction(group);
                    continue;
                }
                attributes.add(base == null ? group : base);
            }
        }
        // Trick to convert = to == as JEval doc is false
        return eval;
    }

    private boolean isKeyword(String group) {
        return JSKeywords.isKeyword(group) || scope.has(group, scope);
    }

    @Bind(aggregate = true, optional = true)
    private void bindUserFunction(UserFunction function) {
        log.info("Adding new user function " + function.getName());
        cx = Context.enter();
        scope.put(function.getName() + "FUNC", scope, function);
        try {
            cx.evaluateString(scope, "function " + function.getName() + "() { " +
                    "return " + function.getName() + "FUNC.evaluate(Array.prototype.slice.call(arguments)); " +
                    "}", function.getName(), 1, null);
        } catch (JavaScriptException e) {
            log.error("Should never happen: wrong script source to evaluate function: ");
        } finally {
            Context.exit();
        }

        Semaphore semaphore = locks.get(function.getName());
        if (semaphore != null) {
            semaphore.release();
            locks.remove(function.getName());
        }
    }

    @Unbind
    private void unbindUserFunction(UserFunction function) {
        scope.delete(function.getName() + "FUNC");
        scope.delete(function.getName());
    }

    private void waitForFunction(String group) {
        log.warn("User function " + group + " required, waiting... although it's not correctly implemented so... skipping!");
        if (true) return;
        try {
            Semaphore semaphore = new Semaphore(0);
            locks.put(group, semaphore);
            semaphore.acquire();
        } catch (InterruptedException ie) {
            log.warn("Interrupted while waiting for user function");
        }
    }

    @Override
    public Set<String> getArguments(String eval) {
        AttributeSet attributes = new AttributeSet();
        analyze(eval, attributes);
        return attributes;
    }

    @Override
    public TupleEvaluator getEvaluator(String eval) throws EvaluateExpressionParseException {
        try {
            Context.enter();
            Script script = cx.compileReader(scope, new StringReader(eval), "<eval>", 1, null);
            Scriptable newScope = cx.newObject(scope);
            newScope.setPrototype(scope);
            newScope.setParentScope(null);
            return new JSTupleEvaluatorImpl(script, newScope);
        } catch (IOException ignored) { // Will not happen with a string reader
            throw new RuntimeException(ignored);
        } catch (Exception e) {
            throw new EvaluateExpressionParseException(e);
        } finally {
            Context.exit();
        }
    }

    @Override
    public Condition buildCondition(String eval) throws EvaluateExpressionParseException {
        return new EvalCondition(eval, this);
    }
}
