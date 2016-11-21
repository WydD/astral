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
import fr.lig.sigma.astral.operators.relational.evaluate.TupleEvaluator;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

import java.util.Map;

/**
 * @author Loic Petit
 */
public class JSTupleEvaluatorImpl implements TupleEvaluator {
    private Script script;
    private Scriptable scope;

    public JSTupleEvaluatorImpl(Script script, Scriptable scope) {
        this.script = script;
        this.scope = scope;
    }

    @Override
    public Comparable evalTuple(Tuple t) {
        Context cx = Context.enter();
        Object res;
        try {
            for (Map.Entry<String, Comparable> entry : t.entrySet()) {
                scope.put(entry.getKey(), scope, entry.getValue());
            }
            res = script.exec(cx, scope);
            for (String key : t.keySet()) {
                scope.delete(key);
            }
        } catch (JavaScriptException e) {
            throw new RuntimeException("Wrong execution of the script", e);
        } finally {
            Context.exit();
        }
        if (res instanceof NativeJavaObject)
            res = ((NativeJavaObject) res).unwrap();
        return (Comparable) res;
    }
}
