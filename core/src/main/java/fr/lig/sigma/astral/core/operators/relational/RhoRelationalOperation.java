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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class RhoRelationalOperation implements UnaryRelationalOperation {
    private Map<String, String> rename;

    @Property(mandatory = true)
    Map<String, List<Map<String,Object>>> parameters;
    @Property(mandatory = true)
    AstralEngine engine;

    @Validate
    public void ready() throws Exception {
        List<Map<String, Object>> renameList = parameters.get("rename");
        rename = new HashMap<String, String>();
        for (Map<String, Object> renameItem : renameList) {
            rename.put((String) renameItem.get("from"), (String) renameItem.get("to"));
        }
    }

    @Override
    public String getOperationName() {
        return "Rename";
    }

    @Override
    public Tuple compute(Tuple t) {
        Tuple n = new Tuple(t.getId());
        for (Map.Entry<String, Comparable> e : t.entrySet()) {
            if (Tuple.PHYSICAL_ID.equals(e.getKey())) continue;
            String newKey = rename.get(e.getKey());
            if (newKey != null)
                n.put(newKey, e.getValue());
            else
                n.put(e.getKey(), e.getValue());
        }
        return n;
    }
}