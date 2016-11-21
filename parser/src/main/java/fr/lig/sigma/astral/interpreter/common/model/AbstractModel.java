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

package fr.lig.sigma.astral.interpreter.common.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Loic Petit
 */
public abstract class AbstractModel extends LinkedList<Object> {
    private Map<String, Object> parameters = new ParameterMap();

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public AbstractModel(String name) {
        add(name);
        add(parameters);
    }

    public String getName() {
        return (String) getFirst();
    }
}
