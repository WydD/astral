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

package fr.lig.sigma.astral.core.handler;

import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.handler.HandlerFactory;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryRuntime;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Unbind;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Loic Petit
 */
@Component
@Provides
@Instantiate
public class HandlerFactoryImpl extends AbstractPojoFactory<Handler> implements HandlerFactory {

    @Override
    public Handler createHandler(String type, Dictionary args, AstralCore core) throws InstanceCreationException {
        String coreId = "(service.id=" + core.getEngine().getServiceId(core) + ")";
        Properties filters = (Properties) args.get("requires.filters");
        if (filters == null)
            filters = new Properties();
        filters.put("core", coreId);
        args.put("requires.filters", filters);
        return instanciatePojoFromName(type, args);
    }


    // TODO FIIIILTRE !
    private Set<Factory> factories = new TreeSet<Factory>(comparator);
    @Bind(aggregate = true)
    private void bindFactory(Factory f) throws Exception {
        for (String s : f.getComponentDescription().getprovidedServiceSpecification()) {
            if ("fr.lig.sigma.astral.handler.Handler".equals(s)) {
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
    public Handler createAndAttachHandler(String type, Dictionary args, QueryRuntime runtime) throws Exception {
        Handler h = createHandler(type, args, runtime.getCore());
        prepareHandler(h, runtime);
        return h;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void prepareHandler(Handler handler, QueryRuntime in) throws Exception, InstanceCreationException {
        Map<String, Object> config = getServiceProperties(handler);
        String[] requirements = (String[]) config.get("requirements");
        boolean valid = false;
        try {
            if (requirements != null) {
                Set<String> inAttributes = in.getOut().getAttributes();
                for (String attribute : requirements) {
                    if (!inAttributes.contains(attribute)) {
                        throw new WrongAttributeException("Required attribute " + attribute + " is not present in the entity " + in);
                    }
                }
            }
            try {
                in.attachNewHandler(handler);
                valid = true;
            } catch (ClassCastException cce) {
                throw new InstanceCreationException("Cannot link the handler to the given input (stream->relation or inverse)");
            }
        } finally {
            if (!valid)
                dispose(handler);
        }
    }


    @Override
    protected Collection<Factory> getFactories() {
        return factories;
    }
}
