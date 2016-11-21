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

package fr.lig.sigma.astral.core;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.core.query.QueryRuntimeImpl;
import fr.lig.sigma.astral.handler.*;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.KnowledgeBase;
import fr.lig.sigma.astral.query.QueryChangedListener;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import fr.lig.sigma.astral.source.Source;
import fr.lig.sigma.astral.source.SourceFactory;
import fr.lig.sigma.astral.source.SourceManager;
import org.apache.felix.ipojo.Factory;
import org.apache.felix.ipojo.annotations.*;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.*;

/**
 *
 */
@Component(immediate = true)
@Provides
@Instantiate
public class AstralEngineImpl extends AbstractPojoFactory<AstralCore> implements AstralEngine {
    @Requires
    private EntityFactory ef;
    @Requires
    private SourceManager sm;
    @Requires
    private SourceFactory sf;
    @Requires
    private HandlerFactory hf;
    @Requires
    private ExpressionAnalyzer ea;
    @Requires
    private KnowledgeBase kb;
    @Requires
    private AggregateFactory af;

    @Requires(optional = true, filter = "(factory.name=*AstralCore*)", specification = "org.apache.felix.ipojo.Factory")
    private List<Factory> factory;
    private Map<String, QueryRuntime> queries = new HashMap<String, QueryRuntime>();
    private Map<Thread, QueryRuntime> queriesFromThread = new HashMap<Thread, QueryRuntime>();
    private BundleContext bc;
    private long serviceId;

    @Validate
    public void validate() {
        // TODO If we can get rid of it, it would be nice :) (e.g. remove all instanceof on @requires objects)
        System.setProperty("ipojo.proxy", "disabled");

        sf.setEngine(this);
        kb.setGlobal("Engine", this);
    }

    public AstralEngineImpl(BundleContext bc) {
        this.bc = bc;
    }

    @Override
    public AstralCore createCore() {
        try {
            Properties args = new Properties();
            args.put("ef", ef);
            args.put("sm", sm);
            args.put("sf", sf);
            args.put("hf", hf);
            args.put("ea", ea);
            args.put("af", af);
            args.put("engine", this);
            return instanciatePojoFromName("AstralCoreImpl", args);
        } catch (InstanceCreationException e) {
            // This should never happen as it is the initialization of the core
            throw new RuntimeException("Can not create core, this should NEVER happen", e);
        }
    }

    @PostRegistration
    public void registered(ServiceReference ref) {
        serviceId = (Long) ref.getProperty("service.id");
        kb.setGlobal("EngineID", serviceId);
    }

    @Override
    public SourceFactory getGlobalSf() {
        return sf;
    }

    @Override
    public SourceManager getGlobalSm() {
        return sm;
    }

    @Override
    public ExpressionAnalyzer getGlobalEA() {
        return ea;
    }

    @Override
    public synchronized QueryRuntime getDeclaredQuery(String name) {
        return queries.get(name);
    }

    @Override
    public synchronized QueryRuntime getDeclaredQueryFromThread(Thread t) {
        return queriesFromThread.get(t);
    }

    @Override
    public synchronized QueryRuntime declareQuery(AstralCore core, Entity out) {
        return declareQuery(core, out, UUID.randomUUID().toString());
    }

    @Override
    public synchronized QueryRuntime declareQuery(AstralCore core, Entity out, String name) {
        QueryRuntimeImpl runtime = new QueryRuntimeImpl(core, out, this, name);
        core.setQR(runtime);
        queries.put(name, runtime);
        queriesFromThread.put(runtime.getThread(), runtime);
        notifyChange(runtime, runtime.getStatus());
        return runtime;
    }

    @Override
    public synchronized Collection<QueryRuntime> getDeclaredQueries() {
        return queries.values();
    }

    private List<QueryChangedListener> listeners = new LinkedList<QueryChangedListener>();

    @Override
    public void addQueryChangedListener(QueryChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public Object getServiceFromId(long id) {
        ServiceReference sr;
        try {
            sr = bc.getAllServiceReferences(null, "(service.id=" + id + ")")[0];
        } catch (Exception e) {
            return null;
        }
        if (sr == null) return null;
        return bc.getService(sr);
    }

    @Override
    public KnowledgeBase getKnowledgeBase() {
        return kb;
    }

    @Override
    public long getServiceId(Object servicePojo) {
        if (servicePojo == this) return serviceId;
        return super.getServiceId(servicePojo);
    }

    @Override
    protected Collection<Factory> getFactories() {
        return factory;
    }

    public void notifyChange(QueryRuntime runtime, QueryStatus status) {
        if (status.isDead()) {
            if (runtime.getOut() instanceof Source)  // Notify the source manager
                sm.sourceDown(runtime.getOut().getName());
            for (Handler<Entity> h : runtime.getHandlers()) {
                if (h instanceof Source)
                    sm.sourceDown(((Source) h).getName());
            }

            queries.remove(runtime.getDisplayName());
            queriesFromThread.remove(runtime.getThread());
        }
        for (QueryChangedListener listener : listeners) {
            listener.queryChanged(runtime, status);
        }
        if (status.isDead())
            runtime.dispose();
    }

    @Invalidate
    public void stop() {
        // Call to cobertura
        // Must be ignored without any crash if it is not here
/*        try {
            String className = "net.sourceforge.cobertura.coveragedata.ProjectData";
            String methodName = "saveGlobalProjectData";
            Class saveClass = Class.forName(className);
            java.lang.reflect.Method saveMethod =
                    saveClass.getDeclaredMethod(methodName, new Class[0]);
            saveMethod.invoke(null);
        } catch (Throwable ignored) {}*/
    }
}
