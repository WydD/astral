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

package fr.lig.sigma.astral.core.query;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventProcessorVisitor;
import fr.lig.sigma.astral.common.event.EventProcessorWalk;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.event.WaitingEntry;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.core.AstralEngineImpl;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryNode;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.source.Source;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.Semaphore;


/**
 *
 */
public class QueryRuntimeImpl implements Runnable, QueryRuntime {
    private static Logger log = Logger.getLogger(QueryRuntimeImpl.class);
    private AstralCore core;
    private Entity out;
    private AstralEngineImpl engine;
    private Thread mainThread;
    private QueryStatus status = QueryStatus.INITIALIZED;
    private Semaphore sem = new Semaphore(0);
    private String name;
    private List<Handler<Entity>> handlers = new LinkedList<Handler<Entity>>();

    private int maxErrCount = 0;
    private boolean computes = false;
    private QueryNode node;

    public QueryRuntimeImpl(AstralCore core, Entity out, AstralEngineImpl engine, String name) {
        this.core = core;
        this.out = out;
        this.engine = engine;
        this.name = name;
        mainThread = new Thread(this, "QR@" + name);
        core.getEs().setRuntime(this);
    }

    @Override
    public AstralCore getCore() {
        return core;
    }

    @Override
    public Entity getOut() {
        return out;
    }

    @Override
    public void attachNewHandler(Handler<Entity> h) throws Exception {
        h.setInput(out);
        handlers.add(h);
    }

    @Override
    public void setQueryNode(QueryNode node) {
        this.node = node;
    }

    @Override
    public QueryNode getQueryNode() {
        return node;
    }

    @Override
    public Thread getThread() {
        return mainThread;
    }

    @Override
    public List<Handler<Entity>> getHandlers() {
        return handlers;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public void start() {
        if (alive) return;
        log.debug("Trying to start query " + out);
        alive = true;
        Set<EventScheduler> dependentSchedulers = new HashSet<EventScheduler>(core.getEs().getDependentSchedulers());
        for (EventScheduler es : dependentSchedulers) {
            es.getRuntime().start();
        }
        mainThread.start();
    }

    private Semaphore holdSemaphore = new Semaphore(1);

    @Override
    public void hold() throws InterruptedException {
        holdSemaphore.acquire();
    }

    @Override
    public void release() {
        holdSemaphore.release();
    }

    private boolean alive = false;

    @Override
    public void stop() {
        alive = false;
        setStatus(QueryStatus.INTERRUPTED);
        mainThread.interrupt();
    }


    public void run() {
        EventProcessor p;
        int errCount = 0;
        log.debug("Starting query " + out);
        EventScheduler es = core.getEs();
        try {
            setStatus(QueryStatus.RUNNING);
            while (es.hasNext() && alive) {
                holdSemaphore.acquire();
                holdSemaphore.release();
                setStatus(QueryStatus.RUNNING);
                WaitingEntry<Batch, EventProcessor> next = es.next();
                p = next.getValue();
                Batch time = next.getKey();
                boolean ok = false;
                computes = true;
                try {
                    p.processEvent(time);
                    ok = true;
                } catch (AxiomNotVerifiedException ex) {
                    log.error("Axiom not verified while running query: " + name + "/" + out.getName() + " while computing " + p.toString() + ": ", ex);
                    setStatus(QueryStatus.E_AXIOM);
                } catch (IllegalStateException ise) {
                    log.error("Illegal state occurred on " + name + "/" + out.getName() + " while computing " + p.toString() + ": ", ise);
                    setStatus(QueryStatus.E_ILLEGAL);
                } catch (RuntimeException re) {
                    log.error("Running exception occurred on " + name + "/" + out.getName() + " while computing " + p + ": ", re);
                    setStatus(QueryStatus.E_UNKNOWN);
                    // A runtime is a breaking rule...
                    break;
                } catch (Exception e) {
                    log.error("Unknown error occurred on query " + name + "/" + out.getName() + " while computing " + p.toString() + ": ", e);
                    setStatus(QueryStatus.E_UNKNOWN);
                }
                computes = false;
                if (!ok) {
                    errCount++;
                    if (maxErrCount >= 0 && errCount > maxErrCount)
                        // Too much error happened quitting
                        break;
                    log.debug("Error happened but try to resume anyway...");
                } else
                    errCount = 0;
            }
        } catch (InterruptedException e) {
            log.error("Interrupted execution of " + name + "/" + out.getName(), e);
        }
        log.debug("End of query execution " + out);
        setStatus(QueryStatus.FINISHED);
        sem.release();
    }

    @Override
    public String toString() {
        return name;
    }

    private void setStatus(QueryStatus status) {
        boolean notify = this.status != status;
        this.status = status;
        if (notify)
            engine.notifyChange(this, status);
    }

    @Override
    public QueryStatus getStatus() {
        return status;
    }

    @Override
    public boolean isComputing() {
        return computes;
    }

    @Override
    public void join() {
        try {
            sem.acquire();
        } catch (InterruptedException ignored) {
            mainThread.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        if (out instanceof Source)
            AbstractPojoFactory.dispose(out);
        else {
            EventProcessorWalk.walk(new EventProcessorVisitor() {
                @Override
                public Object visit(EventProcessor e, Object give) {
                    if (!(e instanceof Source))
                        AbstractPojoFactory.dispose(e);
                    return null;
                }
            }, out, null);
        }
        for (Handler h : handlers) {
            AbstractPojoFactory.dispose(h);
        }
        AbstractPojoFactory.dispose(core);
        core = null;
        out = null;
        handlers.clear();
    }

    public int getMaxErrorCount() {
        return maxErrCount;
    }

    public void setMaxErrorCount(int maxErrCount) {
        this.maxErrCount = maxErrCount;
    }
}
