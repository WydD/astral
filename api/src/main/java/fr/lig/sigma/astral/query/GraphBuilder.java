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

package fr.lig.sigma.astral.query;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.event.EventProcessor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GraphBuilder implements QueryChangedListener {
    private Collection<QueryRuntime> declaredQueries;
    private Logger log = Logger.getLogger(GraphBuilder.class);

    private int count = 0;
    private AstralEngine engine;
    private GraphNotifier server;

    private Map<String, QueryNode> queries = new HashMap<String, QueryNode>();

    public GraphBuilder(AstralEngine engine, GraphNotifier server) {
        this.engine = engine;
        this.server = server;
        engine.addQueryChangedListener(this);
        declaredQueries = engine.getDeclaredQueries();
    }

    public synchronized void queryChanged(QueryRuntime runtime, QueryStatus status) {
        log.debug("A query has changed to " + runtime.getStatus().name() + " known queries " + declaredQueries.size());
        if (runtime.getStatus() == QueryStatus.RUNNING) {
            queries.put(runtime.getDisplayName(), runtime.getQueryNode());
            buildNotifiers(runtime.getQueryNode());
        server.sendGraph(queries);
        } else if (runtime.getStatus().isDead()) {
            queries.remove(runtime.getDisplayName());
        server.sendGraph(queries);
        }
    }

    private void buildNotifiers(final QueryNode queryNode) {
        if(queryNode == null) return;
        Object v = engine.getServiceFromId(queryNode.getId());
        if(v instanceof EventNotifier) {
            ((EventNotifier) v).registerNotifier(new EventProcessor() {
                @Override
                public void processEvent(Batch b) throws AxiomNotVerifiedException {
                    server.sendNotice(queryNode);
                }

                @Override
                public EventProcessor[] waitFor() {
                    return EventProcessor.NO_WAIT;
                }
            });
        }
        for(QueryNode node : queryNode.getChildren())
            buildNotifiers(node);
    }


    public Map<String, QueryNode> getGraph() {
        return queries;
    }
}
