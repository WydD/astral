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

package fr.lig.sigma.astral.graph.server;

import edu.uci.ics.jung.visualization.control.GraphMouseListener;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.gui.graph.JUNGEngineGraph;
import fr.lig.sigma.astral.gui.graph.JUNGEnginePanel;
import fr.lig.sigma.astral.handler.TupleSender;
import fr.lig.sigma.astral.query.QueryNode;

import java.util.Map;

import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Loic Petit
 */
public class ClientGui extends JFrame implements TupleSender {

    private static final TableCellRenderer TABLE_CELL_RENDERER = new TableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
            return (Component) o;
        }
    };
    private static final ListCellRenderer LIST_CELL_RENDERER = new ListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList jList, Object o, int i, boolean b, boolean b1) {
            return (Component) o;
        }
    };

    public JUNGEngineGraph getGraph() {
        return graph;
    }

    private JUNGEngineGraph graph;
    private JTabbedPane pane = new JTabbedPane();

    public ClientGui() {
        graph = new JUNGEngineGraph();
        JUNGEnginePanel p = new JUNGEnginePanel(graph);
        final JLabel detail = new JLabel("Node details");
        detail.setVerticalTextPosition(SwingConstants.TOP);
        detail.setVerticalAlignment(SwingConstants.TOP);
        final JScrollPane jScrollPane = new JScrollPane(detail);
        JSplitPane main = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,p,jScrollPane);
        main.setDividerLocation(800);
        main.setResizeWeight(0.8);

        p.addGraphMouseListener(new GraphMouseListener<QueryNode>() {
            @Override
            public void graphClicked(QueryNode queryNode, MouseEvent mouseEvent) {
                jScrollPane.getViewport().setView(html(queryNode.getParameters()));
            }

            @Override
            public void graphPressed(QueryNode queryNode, MouseEvent mouseEvent) {

            }

            @Override
            public void graphReleased(QueryNode queryNode, MouseEvent mouseEvent) {

            }
        });

        pane.addTab("Graph", main);
        setTitle("Astral Monitor GUI");
        setContentPane(pane);
        setSize(1024, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private Component htmlMap(Map<?, ?> parameters) {
        DefaultTableModel dtm = new DefaultTableModel();
        dtm.addColumn("name");
        dtm.addColumn("value");

        JTable table = new JTable(dtm) {
            @Override
            public TableCellRenderer getCellRenderer(int i, int i1) {
                return TABLE_CELL_RENDERER;
            }
        };
        int max = 0;
        for(Map.Entry<?, ?> param : parameters.entrySet()) {
            Component html = html(param.getValue());
            dtm.addRow(new Object[]{html(param.getKey()), html});
            table.setRowHeight(table.getRowCount() - 1, html.getPreferredSize().height);
            max = max < html.getPreferredSize().width ? html.getPreferredSize().width : max;
        }
        table.getColumn("value").setPreferredWidth(max);
        return table;
    }

    private Component html(Object value) {
        if(value instanceof Map) {
            return htmlMap((Map<?, ?>) value);
        } else if(value instanceof List) {
            return htmlList((List<?>)value);
        }
        return new JLabel(value.toString());
    }

    private Component htmlList(List<?> list) {
        DefaultTableModel dtm = new DefaultTableModel();
        //dtm.addColumn("name");
        dtm.addColumn("value");
        JTable table = new JTable(dtm) {
            @Override
            public TableCellRenderer getCellRenderer(int i, int i1) {
                return TABLE_CELL_RENDERER;
            }
        };
    /*
        DefaultListModel dtm = new DefaultListModel();
        JList table = new JList(dtm) {
            @Override
            public ListCellRenderer getCellRenderer() {
                return LIST_CELL_RENDERER;
            }
        };        */
        for(Object v : list) {
            Component html = html(v);
            dtm.addRow(new Object[]{html});
            table.setRowHeight(table.getRowCount() - 1, html.getPreferredSize().height + 1);
        }

        return table;
    }

    public void sendTuple(String uuid, String desc, Tuple t) {
        MessagePanel panel;
        int i = pane.indexOfTab(uuid);
        if (i < 0) {
            panel = new MessagePanel(uuid, desc, t.keySet());
            pane.addTab(uuid, panel);
            
        } else
            panel = (MessagePanel) pane.getComponentAt(i);
        panel.addTuple(t);
    }
}
