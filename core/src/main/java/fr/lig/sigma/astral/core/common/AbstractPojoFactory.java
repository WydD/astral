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

package fr.lig.sigma.astral.core.common;

import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.containers.AbstractEntityContainer;
import org.apache.felix.ipojo.ComponentInstance;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.InstanceManager;
import org.apache.felix.ipojo.Pojo;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedService;
import org.apache.felix.ipojo.handlers.providedservice.ProvidedServiceHandler;
import org.apache.log4j.Logger;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author Loic Petit
 */
public abstract class AbstractPojoFactory<E> {
    private static final Logger log = Logger.getLogger(AbstractPojoFactory.class);

    protected static Comparator<Factory> comparator = new Comparator<Factory>() {
        @Override
        public int compare(Factory factory, Factory factory1) {
            return -factory.getName().compareTo(factory1.getName());
        }
    };
    protected abstract Collection<Factory> getFactories();

    //private static HashMap<Object, ComponentInstance> instances = new HashMap<Object, ComponentInstance>();
    private static HashMap<Object, Long> serviceIds = new HashMap<Object, Long>();

    private static long id = 0;

    public E instanciatePojoFromName(String name, Dictionary prop) throws InstanceCreationException {
        Factory res = getFactoryFromName(getFactories(), name);
        if (res == null)
            throw new InstanceCreationException("Unable to find a service factory named " + name);
        return createPojo(prop, res);
    }

    protected Factory getFactoryFromName(Collection<Factory> factories, String name) {
        if(name == null) return null;
        // Quick search
        Factory res = null;
        for (Factory f : factories) {
            if (f.getName().indexOf(name) >= 0)
                res = f;
        }
        return res;
    }

    public E instanciatePojoFromSpec(String specs[], Dictionary prop) throws InstanceCreationException {
        // Quick search
        for (Factory f : getFactories()) {
            if (testInclusion(specs, f.getComponentDescription().getprovidedServiceSpecification()))
                return createPojo(prop, f);
        }
        throw new InstanceCreationException("Unable to find a service factory with specs " + Arrays.toString(specs));
    }

    @SuppressWarnings({"unchecked"})
    protected E createPojo(Dictionary prop, Factory f) throws InstanceCreationException {
        if (log.isTraceEnabled())
            log.trace("Try to create component " + f.getName() + " with properties: " + prop);
        try {
            //prop.put("instance.name", f.getName() + id++)
            ComponentInstance instance = f.createComponentInstance(prop);
            if (instance.getState() == ComponentInstance.VALID) {
                E o = (E) ((InstanceManager) instance).getPojoObject();
                ProvidedService providedService = ((ProvidedServiceHandler) ((InstanceManager) instance)
                        .getHandler("org.apache.felix.ipojo:provides"))
                        .getProvidedServices()[0];
                Long serviceId = (Long) providedService
                        .getServiceReference()
                        .getProperty("service.id");
                serviceIds.put(o, serviceId);
                return o;
            } else
                throw new InstanceCreationException("Invalid instance " + f.getName());
            /*if (instance.getState() == ComponentInstance.VALID) {
                InstanceManager instanceManager = (InstanceManager) instance;
                Object o = instanceManager.getPojoObject();
                return (E) o;
            } else
                throw new InstanceCreationException("Invalid instance " + f.getName());*/
        } catch (Exception e) {
            throw new InstanceCreationException("Invalid instance " + f.getName(), e);
        }
    }

    public static void dispose(Object o) {
        if (o instanceof AbstractEntityContainer)
            dispose(((AbstractEntityContainer) o).getOutput());
        serviceIds.remove(o);

        if (o instanceof Pojo) {
            try {
                ((Pojo) o).getComponentInstance().dispose();
            } catch (Exception e) {
                log.warn(e);
                e.printStackTrace();
            }
        }
    }

    public void reconfigure(E o, Dictionary config) {
        if (o instanceof Pojo) {
            try {
                ((Pojo) o).getComponentInstance().reconfigure(config);
            } catch (Exception e) {
                log.warn(e);
                e.printStackTrace();
            }
        }
    }

    public Map<String, Object> getServiceProperties(E pojo) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (pojo instanceof Pojo) {
            InstanceManager instance = (InstanceManager) ((Pojo) pojo).getComponentInstance();
            Set registredFields = instance.getRegistredFields();
            if (registredFields == null) return result;
            for (Object field : registredFields) {
                result.put((String) field, instance.onGet(pojo, (String) field));
            }
            return result;
        }
        for (Field field : pojo.getClass().getDeclaredFields()) {
            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                result.put(field.getName(), field.get(pojo));
                field.setAccessible(accessible);
            } catch (IllegalAccessException ignored) {
            }
        }
        return result;
    }

    private boolean testInclusion(String subset[], String superset[]) {
        for (String s : subset) {
            boolean res = false;
            for (String t : superset) {
                if (t.indexOf(s) >= 0) {
                    res = true;
                    break;
                }
            }
            if (!res) return false;
        }
        return true;
    }

    public long getServiceId(Object servicePojo) {
        return serviceIds.get(servicePojo);
    }
}
