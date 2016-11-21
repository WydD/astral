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

package fr.lig.sigma.astral.core.source;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.EntityContainer;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.Source;
import fr.lig.sigma.astral.source.SourceAlreadyExistsException;
import fr.lig.sigma.astral.source.SourceFactory;
import fr.lig.sigma.astral.source.SourceFactoryContainer;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.Bind;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Unbind;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author Loic Petit
 */
@Instantiate
@Component(public_factory = false)
@Provides
public class SourceFactoryImpl extends AbstractPojoFactory<Source> implements SourceFactory {
    private final static Logger log = Logger.getLogger(SourceFactoryImpl.class);


    // TODO FIIIILTRE !
    private Set<Factory> factories = new TreeSet<Factory>(comparator);

    @Bind(aggregate = true)
    private void bindFactory(Factory f) throws Exception {
        for (String s : f.getComponentDescription().getprovidedServiceSpecification()) {
            if ("fr.lig.sigma.astral.source.Source".equals(s)) {
                factories.add(f);
                break;
            }
        }
    }

    @Unbind
    private void unbindFactory(Factory f) {
        factories.remove(f);
    }


    private AstralEngine engine;

    protected Collection<Factory> getFactories() {
        return factories;
    }

    public Source createSource(String name, Dictionary prop) throws InstanceCreationException, SourceAlreadyExistsException {
        Source s = instanciatePojoFromName(name, prop);
        prepareSource(s);
        if (prop.get("weak") != null)
            s.getScheduler().setWeakMode(true);
        return s;
    }


    public void prepareSource(Source s) throws SourceAlreadyExistsException, InstanceCreationException {
        prepareSource(s, null);
    }

    public void prepareSource(Source s, AstralCore core) throws SourceAlreadyExistsException, InstanceCreationException {
        boolean declare = core == null;
        if (declare) {
            core = engine.createCore();
        }
        s.setScheduler(core.getEs());
        //core.getEs().incNumberOfSource();
        if (s instanceof SourceFactoryContainer)
            ((SourceFactoryContainer) s).setSourceFactory(this);
        if (s instanceof EntityContainer) {
            ((EntityContainer) s).bindEntityFactory(core.getEf());

            Map<String, Object> properties = getServiceProperties(s);
            Object schema = properties.get("schema");
            if (schema != null) {
                String type = (String) properties.get("type");
                if (type == null) // Default implementation
                    type = s instanceof Relation ? "RelationBufferedVolatileImpl" : "StreamQueueImpl";
                String entityname = (String) properties.get("entityname");
                ((EntityContainer) s).setOutput(core.getEf()
                        .instanciateEntity(type, entityname, new AttributeSet((String[]) schema)));
                log.debug("Filling entity container " + s + ":" + s.getClass().getSimpleName() + " with a " + type + " attributes: " + Arrays.toString((String[]) schema));
            }
        }

        QueryRuntime qr;
        if (declare)
            qr = engine.declareQuery(core, s);
        else
            qr = core.getQR();
        core.getSm().registerSource(s, qr);

        try {
            s.firstSchedule();
        } catch (Exception e) {
            throw new InstanceCreationException(e);
        }
    }

    @Override
    public Map<String, Object> getProperties(Source source) {
        return getServiceProperties(source);
    }

    @Override
    public void setEngine(AstralEngine engine) {
        this.engine = engine;
    }

}
