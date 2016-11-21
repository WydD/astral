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

package fr.lig.sigma.astral.gui.query;

import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.gui.AstralRuntime;
import fr.lig.sigma.astral.gui.MainGui;
import fr.lig.sigma.astral.gui.controller.RelationController;
import fr.lig.sigma.astral.gui.controller.StreamController;
import fr.lig.sigma.astral.gui.graph.QueryPlanViewer;
import fr.lig.sigma.astral.gui.result.EntityView;
import fr.lig.sigma.astral.gui.result.RelationView;
import fr.lig.sigma.astral.gui.result.StreamView;
import fr.lig.sigma.astral.gui.source.SourceFactoryFromModel;
import fr.lig.sigma.astral.gui.source.SourceManagerView;
import fr.lig.sigma.astral.gui.source.SourceModel;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.Source;
import org.w3c.dom.Document;

import javax.swing.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * @author Loic Petit
 */
public class QueryBuilder implements Runnable {
    private String query;
    private QueryView queryView;
    private MainGui guiView;
    private SourceManagerView sourceView;
    private AstralRuntime gui;

    private boolean parseOnly;
    private Thread currentThread;
    private AstralCore core;

    public QueryBuilder(QueryView queryView, MainGui guiView, SourceManagerView sourceView, AstralRuntime gui) {
        this.queryView = queryView;
        this.guiView = guiView;
        this.sourceView = sourceView;
        this.gui = gui;
    }

    public void actionPerformed(int action) {
        if (action == 1) {
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
            }
            return;
        }

        query = queryView.getQuery();
        parseOnly = action == 2;
        new Thread(this).start();
    }

    @SuppressWarnings({"unchecked"})
    public void run() {
        core = gui.getEngine().createCore();
        queryView.setStatus("Creating sources");
        if (!setSources()) return;

        queryView.setStatus("Parsing query");
        QueryRuntime runtime = parseQuery(parseOnly);
        if (parseOnly)
            queryView.setStatus("Parsing done");
        if (runtime == null)
            return;

        // If export, then create a server
        /*int port = queryView.getExportPort();
        EntityExporter socket = null;
        if (port > 0) {
            try {
                Entity duplicate = e;
                if (e instanceof Stream) {
                    Duplicate<Stream> dup = (Duplicate<Stream>) core.getOf().instanciateOperator("Duplicate", "Stream");
                    dup.setInput((Stream) e);
                    duplicate = dup.getDuplicate();
                    e = dup;
                }
                socket = gui.getNf().createExportServer(duplicate, port);
            } catch (Exception e1) {
                queryView.setStatus("<html><b><font color=#ff0000>Could not create the server</font></b></html>");
                return;
            }
        } */

        EntityView view = prepareController(runtime.getOut());
        //QueryRuntime runtime = gui.getEngine().declareQuery(core, e);
        runtime.start();
        queryView.attach(runtime, view);
        queryView.setStatus("Query started");
    }

    private EntityView prepareController(Entity e) {
        EntityView entityView;
        if (e instanceof Stream) {
            entityView = new StreamView();
            new StreamController((StreamView) entityView, (Stream) e);
        } else {
            entityView = new RelationView();
            new RelationController((RelationView) entityView, (Relation) e);
        }

        queryView.setStatus("Controller ready");
        queryView.setPanel(guiView.getResultPanel(), entityView.getPanel());
        return entityView;
    }

    /**
     * Redo the whole query building in order to see the optimizer
     * @param parseOnly Only parse and never builds the entity
     * @return The created entity, null if parseOnly = true
     */
    private QueryRuntime parseQuery(boolean parseOnly) {
        QueryRuntime e = null;
        try {
            Document doc = gui.getInter().parse(query);
            QueryPlanViewer qpv = new QueryPlanViewer(doc);
            queryView.setPanel(guiView.getParsePanel(), qpv.getPanel());
            gui.getInter().inferSource(doc, core);
            qpv = new QueryPlanViewer(doc);
            queryView.setPanel(guiView.getInferPanel(), qpv.getPanel());
            gui.getInter().optimize(doc,gui.getEngine());
            qpv = new QueryPlanViewer(doc);
            queryView.setPanel(guiView.getFinalPanel(), qpv.getPanel());
            if (!parseOnly)
                e = gui.getInter().build(doc, core, gui.getEngine());
        } catch (Exception ex) {
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            ex.printStackTrace(printWriter);
            queryView.setStatus("<html><b><font color=#ff0000>Parser error</font></b></html>");
            JOptionPane.showMessageDialog(null, result.toString(), "Parser Error", JOptionPane.ERROR_MESSAGE);
            e = null;
        }
        return e;
    }

    private boolean setSources() {
        Collection<SourceModel> sources = sourceView.getSources();
        for (SourceModel s : sources) {
            Source e = SourceFactoryFromModel.createSource(s, //gui.getNf(),
                    core.getSf());
            if (e == null) return false;
        }
        return true;
    }
}
