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

import fr.lig.sigma.astral.common.Tuple;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Set;

/**
 *
 */
public class MessagePanel extends JPanel {
    private JTable table = new JTable();
    private DefaultTableModel model = new DefaultTableModel();
    private Set<String> args;

    public MessagePanel(String uuid, String desc, Set<String> args) {
        this.args = args;
        for (String s : args) {
            if (!Tuple.PHYSICAL_ID.equals(s))
                model.addColumn(s);
        }
        table.setModel(model);
        setLayout(new BorderLayout());
        JLabel lbl = new JLabel("<html><center><br/><b>"+desc+"</b><br/>&nbsp;</center></html>");

        add(lbl, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    public void addTuple(Tuple t) {
        if (model.getRowCount() == 50) {
            model.removeRow(49);
        }
        Object[] data = new Object[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            data[i] = t.get(model.getColumnName(i));
        }
        model.insertRow(0, data);
    }
}
