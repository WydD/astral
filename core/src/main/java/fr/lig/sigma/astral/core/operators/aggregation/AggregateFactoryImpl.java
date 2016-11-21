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

package fr.lig.sigma.astral.core.operators.aggregation;

import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFunction;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.annotations.*;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.log4j.Logger;

import java.util.*;

/**
 *
 */
@Component
@Provides
@Instantiate
public class AggregateFactoryImpl implements AggregateFactory {
    private HashMap<String, InstanceManager> repo = new HashMap<String, InstanceManager>();
    private HashMap<Factory, String> components = new HashMap<Factory, String>();
    private static final Logger log = Logger.getLogger(AggregateFactoryImpl.class);

    @Validate
    private void ready() {
    }

    @Bind(aggregate = true)
    private void bindFactory(Factory f) throws Exception {
        for (String s : f.getComponentDescription().getprovidedServiceSpecification()) {
            if ("fr.lig.sigma.astral.operators.aggregation.AggregateFunction".equals(s)) {
                InstanceManager component = (InstanceManager) f.createComponentInstance(new Properties());

                ProvidedService providedService = ((ProvidedServiceHandler) component
                        .getHandler("org.apache.felix.ipojo:provides"))
                        .getProvidedServices()[0];
                String functionName = (String) providedService
                        .getServiceReference()
                        .getProperty(NAME_PROPERTY);
                if (functionName == null)
                    break;
                log.debug("Registered aggregate: " + functionName + ", className:" + component.getClassName());
                components.put(f, functionName);
                repo.put(functionName, component);
                break;
            }
        }
    }

    @Unbind
    private void unbindFactory(Factory f) {
        String functionName = components.remove(f);
        if (functionName != null) {
            InstanceManager remove = repo.remove(functionName);
            remove.dispose();
        }
    }


    public String ident(StringBuffer buf) {
        int i = 0;
        int l = buf.length();
        char c = buf.charAt(i++);
        while (i < l && (c >= '0' && c <= '9' ||
                c >= 'A' && c <= 'Z' ||
                c >= 'a' && c <= 'z' ||
                c == '_' || c == '-' || c == ' ' || c == '.')) {
            c = buf.charAt(i++);
        }
        String res = buf.substring(0, i - 1).trim();
        buf.delete(0, i - 1);
        return res;
    }

    public String[] attributes(StringBuffer buf) {
        ArrayList<String> list = new ArrayList<String>(1);
        do {
            buf.delete(0, 1);
            String id = ident(buf);
            list.add(id);
        } while (buf.length() > 0 && (buf.charAt(0) == ','));
        return list.toArray(new String[list.size()]);
    }

    public AggregateFunction func(StringBuffer buf) {
        String id = ident(buf);
        String[] attr;
        if (buf.length() > 0 && buf.charAt(0) == '(') {
            attr = attributes(buf);
            if (buf.charAt(0) == '/') {
                buf.delete(0, 1);
                return new HolisticPartitionedAggregate(this, id, buf.substring(0, buf.length() - 1), attr);
            }
        } else {
            attr = new String[0];
        }
        return buildObject(id, attr);
    }

    private AggregateFunction buildObject(String id, String[] attr) {
        try {
            Class[] c = new Class[attr.length];
            for (int i = 0; i < attr.length; i++)
                c[i] = String.class;
            return (AggregateFunction) Class.forName(repo.get(id).getClassName())
                    .getDeclaredConstructor(c).newInstance(attr);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create aggregate function instance? ", e);
        }
    }

    public AggregateFunction get(String c) {
        return func(new StringBuffer(c));
    }

    @Override
    public List<String> getAttributes(String expression) {
        return getAttributes(new StringBuffer(expression));
    }

    public List<String> getAttributes(StringBuffer buf) {
        ident(buf);
        List<String> res = new ArrayList<String>();
        if (buf.length() > 0 && buf.charAt(0) == '(') {
            Collections.addAll(res, attributes(buf));
            if (buf.charAt(0) == '/') {
                buf.delete(0, 1);
                buf.deleteCharAt(buf.length() - 1);
                res.addAll(getAttributes(buf));
            }
        }
        return res;
    }
}
