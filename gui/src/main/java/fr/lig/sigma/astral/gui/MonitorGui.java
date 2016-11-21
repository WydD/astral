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

package fr.lig.sigma.astral.gui;

import com.mxgraph.swing.mxGraphComponent;
import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.gui.graph.EngineGraph;
import fr.lig.sigma.astral.gui.graph.JUNGEngineGraph;
import fr.lig.sigma.astral.gui.graph.JUNGEnginePanel;
import fr.lig.sigma.astral.query.GraphBuilder;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import javax.swing.*;

/**
 *
 */
@org.apache.felix.ipojo.annotations.Component
public class MonitorGui extends JFrame {
    @Requires
    private AstralEngine engine;

    private EngineGraph graph;
    private JPanel panel;
    private mxGraphComponent graphComponent;

    @Validate
    private void ready() {  /*
        graph = new EngineGraph(engine, this);
        graphComponent = new mxGraphComponent(graph);
                  /*graphComponent.getViewport().setOpaque(false);
        graphComponent.setBorder(BorderFactory.createEmptyBorder());
        graphComponent.setBackground(Color.WHITE);
        graphComponent.setConnectable(false);
        graphComponent.setEnabled(true);     */
//        panel = new JPanel();


        JUNGEngineGraph graph = new JUNGEngineGraph();
        JPanel p = new JUNGEnginePanel(graph);
        new GraphBuilder(engine, graph);
        setTitle("AStrAL Monitor GUI");
        setContentPane(p);
        setSize(1024, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void redraw() {
        /*graphComponent.setDragEnabled(true);
        graphComponent.zoomAndCenter();
        graphComponent.refresh();       */
    }

}
