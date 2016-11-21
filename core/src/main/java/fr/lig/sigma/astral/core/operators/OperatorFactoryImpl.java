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

import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.EntityContainer;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.OperatorDescription;
import fr.lig.sigma.astral.operators.OperatorFactory;
import fr.lig.sigma.astral.operators.UnaryOperationFactory;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.felix.ipojo.architecture.PropertyDescription;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;

import java.util.*;

/**
 * @author Loic Petit
 */
@Instantiate
@Component
@Provides(strategy = "INSTANCE")
public class OperatorFactoryImpl extends AbstractPojoFactory<Operator> implements OperatorFactory {

    //@Requires(optional = true, filter = "(|(factory.name=fr.lig.sigma.astral.*operators*)(factory.name=fr.lig.sigma.astral.*terminal*))")
    private Set<Factory> factories = new TreeSet<Factory>(comparator);
    private Set<Factory> descriptionFactories = new TreeSet<Factory>(comparator);

    private AstralCore core;
    private static final Logger log = Logger.getLogger(OperatorFactoryImpl.class);

    private BundleContext ctxt;

    public OperatorFactoryImpl(BundleContext ctxt) {
        this.ctxt = ctxt;
    }

    @Requires
    private UnaryOperationFactory unaryOperationFactory;

    @Bind(aggregate = true)
    private void bindFactory(Factory f) throws Exception {
        for (String s : f.getComponentDescription().getprovidedServiceSpecification()) {
            if ("fr.lig.sigma.astral.operators.Operator".equals(s)) {
                factories.add(f);
                break;
            }
            if ("fr.lig.sigma.astral.operators.OperatorDescription".equals(s)) {
                descriptionFactories.add(f);
                break;
            }
        }
    }

    @Unbind
    private void unbindFactory(Factory f) {
        factories.remove(f);
        descriptionFactories.remove(f);
    }

    protected Collection<Factory> getFactories() {
        return factories;
    }

    private Operator bindFactories(Operator o) throws InstanceCreationException {
        if (o instanceof EntityContainer)
            ((EntityContainer) o).bindEntityFactory(core.getEf());
        try {
            o.setScheduler(core.getEs());
            o.prepare();
        } catch (Exception e) {
            log.error("Error while building operator", e);
            throw new InstanceCreationException(e);
        }
        return o;
    }

    @Override
    public Operator instanciateSpecificOperator(String name, Properties prop) throws InstanceCreationException {
        Factory f = getFactoryFromName(factories, "." + name);
        PropertyDescription[] properties = f.getComponentDescription().getProperties();
        // A bit dirty
        for (PropertyDescription componentProperty : properties) {
            if (componentProperty.getName().equals("description")) {
                buildDescriptions(prop);
                break;
            }
        }
        if (log.isTraceEnabled())
            log.trace("Creating operator " + name + " with properties " + prop);
        return bindFactories(createPojo(prop, f));
    }

    private void buildDescriptions(Properties prop) throws InstanceCreationException {
        Object descriptions = prop.get("description");
        if (descriptions == null) return;
        if (!(descriptions instanceof List))
            return;
        List descriptionsList = (List) descriptions;
        Object descriptionObject;
        if (descriptionsList.size() == 1) {
            Object description = descriptionsList.get(0);
            descriptionObject = buildOperatorDescription(prop, description);
        } else {
            descriptionObject = new ArrayList<OperatorDescription>();
            for (Object description : descriptionsList)
                ((List) descriptionObject).add(buildOperatorDescription(prop, description));
        }
        prop.put("description", descriptionObject);
    }

    private OperatorDescription buildOperatorDescription(Properties prop, Object description) throws InstanceCreationException {
        if (!(description instanceof Map))
            throw new InstanceCreationException("Description is not a map " + description);

        Map descAsMap = (Map) description;
        String impl = (String) descAsMap.get("impl");
        if (impl == null)
            throw new InstanceCreationException("No implementation class found for description " + descAsMap);
        Factory f = getFactoryFromName(descriptionFactories, impl);
        if (f == null)
            throw new InstanceCreationException("No factory has been found for name " + impl);
        Hashtable<String, Object> descProp = new Hashtable<String, Object>((Map<String, Object>) description);
        Object filters = prop.get("requires.filters");
        if (filters != null)
            descProp.put("requires.filters", filters);
        return (OperatorDescription) createPojo(descProp, f);
    }

    @Override
    public UnaryOperationFactory getUnaryFactory() {
        return unaryOperationFactory;
    }

    @Override
    public void setCore(AstralCore core) {
        unaryOperationFactory.setCore(core);
        this.core = core;
    }
}
