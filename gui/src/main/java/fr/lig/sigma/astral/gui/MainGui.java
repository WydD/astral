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

import fr.lig.sigma.astral.gui.query.QueryView;
import fr.lig.sigma.astral.gui.source.SourceManagerView;

import javax.swing.*;

/**
 * @author Loic Petit
 */
public class MainGui {
    private JTree sourceTree;
    private JComboBox typeSelect;
    private JTextField nameTextField;
    private JPanel sourceDescPanel;
    private JButton addButton;
    private JButton deleteButton;
    private JPanel panel;
    private JPanel resultPanel;
    private JPanel queryPanel;
    private JPanel parsePanel;
    private JPanel inferPanel;
    private JPanel finalPanel;

    public MainGui(AstralRuntime gui) {
        SourceManagerView sourceView = new SourceManagerView(nameTextField, typeSelect, sourceDescPanel, sourceTree, addButton);
        QueryView queryView = new QueryView(this, sourceView, gui);
        queryPanel.add(queryView.getPanel());

        addButton.addActionListener(sourceView);
        deleteButton.addActionListener(sourceView);
    }


    public JPanel getPanel() {
        return panel;
    }

    public JPanel getResultPanel() {
        return resultPanel;
    }

    public JPanel getParsePanel() {
        return parsePanel;
    }

    public JPanel getInferPanel() {
        return inferPanel;
    }

    public JPanel getFinalPanel() {
        return finalPanel;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
