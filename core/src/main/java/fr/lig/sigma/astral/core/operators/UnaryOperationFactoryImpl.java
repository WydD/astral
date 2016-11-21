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

package fr.lig.sigma.astral.core.operators;

import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.operators.UnaryOperationFactory;
import fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * @author Loic Petit
 */

@Instantiate
@Component
@Provides(strategy = "INSTANCE")
public class UnaryOperationFactoryImpl extends AbstractPojoFactory<UnaryRelationalOperation> implements UnaryOperationFactory {
    private Set<Factory> factories = new HashSet<Factory>();
    private AstralCore core;
    private static final Logger log = Logger.getLogger(UnaryOperationFactoryImpl.class);


    @Bind(aggregate = true)
    private void bindFactory(Factory f) throws Exception {
        for (String s : f.getComponentDescription().getprovidedServiceSpecification()) {
            if ("fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation".equals(s)) {
                factories.add(f);
                break;
            }
        }
    }

    @Unbind
    private void unbindFactory(Factory f) {
        factories.remove(f);
    }

    @Override
    public UnaryRelationalOperation instanciateUnaryOperator(Map<String, Object> properties) throws Exception {
        Properties prop = new Properties();
        prop.put("parameters", properties);
        prop.put("core", core);
        prop.put("engine", core.getEngine());
        return instanciatePojoFromName((String) properties.get("impl"), prop);
    }

    @Override
    public void setCore(AstralCore core) {
        this.core = core;
    }

    protected Collection<Factory> getFactories() {
        return factories;
    }
}
