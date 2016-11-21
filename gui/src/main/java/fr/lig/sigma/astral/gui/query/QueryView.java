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

import fr.lig.sigma.astral.gui.AstralRuntime;
import fr.lig.sigma.astral.gui.MainGui;
import fr.lig.sigma.astral.gui.result.EntityView;
import fr.lig.sigma.astral.gui.source.SourceManagerView;
import fr.lig.sigma.astral.query.QueryChangedListener;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Loic Petit
 */
public class QueryView implements ActionListener, QueryChangedListener, ListSelectionListener {
    private static Logger log = Logger.getLogger(QueryView.class);

    static {
        log.setLevel(Level.DEBUG);
    }

    private JTextArea queryTextArea;
    private JButton evaluateButton;
    private JLabel statusLabel;
    private JLabel processTimeLabel;
    private JPanel panel;
    private JTextField portTextField;
    private JCheckBox exportPortCheckBox;
    private JButton parseOnlyButton;
    private JButton stopButton;
    private JTable queriesTable;
    private DefaultTableModel queriesTableModel = new DefaultTableModel(new String[]{"Query", "Start", "Status"}, 0);
    private MainGui guiView;
    private SourceManagerView sourceView;
    private AstralRuntime gui;
    private int selectedRow = -1;

    public QueryView(MainGui guiView, SourceManagerView sourceView, AstralRuntime gui) {
        this.guiView = guiView;
        this.sourceView = sourceView;
        this.gui = gui;
        evaluateButton.addActionListener(this);
        parseOnlyButton.addActionListener(this);
        stopButton.addActionListener(this);
        queriesTable.setModel(queriesTableModel);
        queriesTable.setRowSelectionAllowed(true);
        queriesTable.setColumnSelectionAllowed(false);
        queriesTable.getSelectionModel().addListSelectionListener(this);
        queriesTable.validate();
        gui.getEngine().addQueryChangedListener(this);
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }

    public void setProcessTime(String status) {
        processTimeLabel.setText(status);
    }

    public String getQuery() {
        return queryTextArea.getText();
    }

    public void setPanel(JPanel host, JPanel panel) {
        host.removeAll();
        host.add(panel);
        host.validate();
        host.getParent().validate();
    }

    public int getExportPort() {
        if (exportPortCheckBox.getSelectedObjects() == null)
            return -1;
        return Integer.parseInt(portTextField.getText());
    }

    public void actionPerformed(ActionEvent actionEvent) {
        int action;
        String command = actionEvent.getActionCommand().toLowerCase();
        if (command.contains("parse")) {
            action = 1;
        } else if (command.contains("stop")) {
            queryRow.get(selectedRow).stop();
            return;
        } else if (command.contains("eval")) {
            action = 3;
        } else {
            log.warn("Wrong action command inside QueryView");
            return;
        }


        QueryBuilder qb = new QueryBuilder(this, guiView, sourceView, gui);
        qb.actionPerformed(action);
    }

    // TODO Tout ça nécessiterait quand même un nettoyage de code pour passer sur un CustomTableModel non ?
    public void queryChanged(QueryRuntime runtime, QueryStatus status) {
        QueryAttachment attachment = getAttachment(runtime);
        log.debug("Query status changed for: " + runtime.getOut().getName() + " / status: " + status.name());
        if (status == QueryStatus.INITIALIZED) {
            queriesTableModel.addRow(new String[]{
                    runtime.getOut().getName(),
                    String.valueOf(runtime.getCore().getEs().getT0()),
                    status.name()
            });
            attachment.row = queriesTableModel.getRowCount() - 1;
            queryRow.put(attachment.row, runtime);
        } else {
            int row = attachment.row;
            if (row < 0) {
                log.warn("Unknown QueryRuntime");
                return;
            }
            queriesTableModel.setValueAt(String.valueOf(runtime.getCore().getEs().getT0()), row, 1);
            queriesTableModel.setValueAt(status.name(), row, 2);
        }
        queriesTable.validate();
    }

    public void attach(QueryRuntime runtime, EntityView view) {
        QueryAttachment attachment = getAttachment(runtime);
        attachment.view = view;
        attachments.put(runtime, attachment);
    }

    private Map<QueryRuntime, QueryAttachment> attachments = new HashMap<QueryRuntime, QueryAttachment>();
    private Map<Integer, QueryRuntime> queryRow = new HashMap<Integer, QueryRuntime>();

    private synchronized QueryAttachment getAttachment(QueryRuntime runtime) {
        QueryAttachment attach = attachments.get(runtime);
        if (attach == null) {
            attach = new QueryAttachment();
            attachments.put(runtime, attach);
        }
        return attach;
    }

    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        int selected = queriesTable.getSelectedRow();
        if(selected == selectedRow)
            return;
        selectedRow = selected;
        setPanel(guiView.getResultPanel(), attachments.get(queryRow.get(selectedRow)).view.getPanel());
    }

    private class QueryAttachment {
        int row = -1;
        EntityView view;
    }
}
