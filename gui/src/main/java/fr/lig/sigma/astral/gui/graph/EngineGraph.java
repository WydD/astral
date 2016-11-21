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

package fr.lig.sigma.astral.gui.graph;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.util.mxConstants;
import com.mxgraph.view.mxGraph;
import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventNotifier;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventProcessorVisitor;
import fr.lig.sigma.astral.common.event.EventProcessorWalk;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.gui.MonitorGui;
import fr.lig.sigma.astral.query.QueryChangedListener;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import fr.lig.sigma.astral.source.Source;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 */
public class EngineGraph extends mxGraph implements QueryChangedListener {
    private Collection<QueryRuntime> declaredQueries;
    private AstralEngine engine;
    private Map<QueryRuntime, Object> vertices = new HashMap<QueryRuntime, Object>();
    private Map<Source, Object> sourceVertices = new HashMap<Source, Object>();
    private Object parent;
    private MonitorGui monitorGui;
    private Logger log = Logger.getLogger(EngineGraph.class);
    private mxHierarchicalLayout layout;

    public EngineGraph(AstralEngine engine, MonitorGui monitorGui) {
        this.monitorGui = monitorGui;
        declaredQueries = engine.getDeclaredQueries();
        engine.addQueryChangedListener(this);
        parent = getDefaultParent();
        this.engine = engine;
        layout = new mxHierarchicalLayout(this, SwingConstants.WEST);
        //setHtmlLabels(true);
        declaredQueries = engine.getDeclaredQueries();
    }

    public void queryChanged(QueryRuntime runtime, QueryStatus status) {
        log.info("A query has changed to " + runtime.getStatus().name() + " known queries " + declaredQueries.size());
        if (status == QueryStatus.INITIALIZED) {
            rebuildGraph();
            selectVertices();
            log.info("After building the graph there is " + getSelectionCount() + " cells");
            clearSelection();
        } else if (runtime.getStatus().isDead()) {
            getModel().beginUpdate();
            if (runtime.getOut() instanceof Source)
                setCellStyle(mxConstants.STYLE_FILLCOLOR + "=#bbbbbb;", new Object[]{sourceVertices.get((Source) runtime.getOut())});
            else
                removeCells(new Object[]{vertices.get(runtime)});
            getModel().endUpdate();
        }
    }

    private void rebuildGraph() {
        getModel().beginUpdate();
        selectVertices();
        removeCells();
        getModel().endUpdate();
        vertices.clear();
        sourceVertices.clear();
        for (QueryRuntime qr : declaredQueries) {
            clearSelection();
            if (qr.getOut() instanceof Source) {
                getModel().beginUpdate();
                final Object queryVertex = buildCell(qr.getOut());
                getModel().endUpdate();
                sourceVertices.put((Source) qr.getOut(), queryVertex);
                continue;
            }
            getModel().beginUpdate();
            EventProcessorWalk.walk(new EventProcessorVisitor() {
                public Object visit(final EventProcessor e, Object root) {
                    Object v = root;
                    if (e instanceof Source) {
                        Source s = (Source) e;
                        v = sourceVertices.get(s);
                    } else if (e instanceof Entity) {
                        v = buildCell((Entity) e);
                        addSelectionCell(v);
                    }
                    if (v != null)
                        updateCellSize(v);
                    if (v != root && root != null) {
                        insertEdge(parent, null, "", v, root, mxConstants.STYLE_EDGE + "=" + mxConstants.EDGESTYLE_ENTITY_RELATION + ";");

                    }
                    return v;
                }

            }, qr.getOut(), null);
            getModel().endUpdate();
            getModel().beginUpdate();
            layout.execute(parent);
            getModel().endUpdate();
            getModel().beginUpdate();
            Object queryVertex = groupCells(null, 2 * getGridSize());
            vertices.put(qr, queryVertex);
            updateCellSize(queryVertex);
            getModel().endUpdate();
        }
        getModel().beginUpdate();
        selectVertices();
        layout.execute(getDefaultParent());
        layout.setLoggerLevel(Level.ALL);
        getModel().endUpdate();
        monitorGui.redraw();
    }

    private void installAnimation(final EventProcessor e, final Object finalV) {
        ((EventNotifier) e).registerNotifier(new EventProcessor() {
            private int status = 0;
            private Runnable runnable = new Runnable() {
                public void run() {
                    try {
                        while (status >= 0) {
                            setCellStyle(mxConstants.STYLE_FILLCOLOR + "=#ffff" + Integer.toHexString(status) + ";", new Object[]{finalV});
                            status = 0;
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException ignored) {
                    log.error("Animation interrupted",ignored);
                    }
                }
            };
            private Thread run;

            public void processEvent(Batch b) throws AxiomNotVerifiedException {
                status = 190;
                try {
                    if (run == null || !run.isAlive()) {
                        run = new Thread(runnable);
                        run.start();
                    }
                } catch (Exception ignored) {
                    log.error("Exception in animation",ignored);
                }

            }

            public EventProcessor[] waitFor() {
                return new EventProcessor[]{e};
            }
        });
    }

    private Object buildCell(Entity out) {
        String cellStyle = mxConstants.STYLE_SPACING + "=5;" +
                mxConstants.STYLE_ROUNDED + "=true;";
             /*
        String label = "<html><center><b>";
        label += out instanceof Source ? out.getName() : out.getClass().getSimpleName();
        label += "</b>";
        label += out instanceof Stream ? "Stream" : "Relation";
        label += "<br/>";
        label += AttributeSet.string(out.getAttributes());
        label += "</center></html>";
               */
        String label = out instanceof Source ? out.getName() : out.getClass().getSimpleName();

        Object v = insertVertex(parent, null, label, 0, 0, 10, 10, cellStyle);
        installAnimation(out, v);
        return v;
    }

}
