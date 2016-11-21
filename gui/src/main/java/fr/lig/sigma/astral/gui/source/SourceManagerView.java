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

package fr.lig.sigma.astral.gui.source;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Collection;

public class SourceManagerView implements ActionListener, TreeSelectionListener {
    private JTextField nameTextField;
    private JComboBox typeSelect;
    private JPanel description;
    private JTree sourceTree;
    private JButton addButton;
    private Map<String, SourceView> sources;
    private SourceView panel;
    private DefaultMutableTreeNode tree = new DefaultMutableTreeNode("Sources entities");

    public SourceManagerView(JTextField nameTextField, JComboBox typeSelect, JPanel description, JTree sourceTree, JButton addButton) {
        this.nameTextField = nameTextField;
        this.typeSelect = typeSelect;
        this.description = description;
        this.sourceTree = sourceTree;
        this.addButton = addButton;
        sources = new HashMap<String, SourceView>();
        typeSelect.addActionListener(this);
        onListChange(typeSelect);
        sourceTree.setModel(new DefaultTreeModel(tree));
        sourceTree.addTreeSelectionListener(this);
    }


    public void setData(SourceModel source) {
        nameTextField.setText(source.getName());
        typeSelect.setSelectedItem(source.getType());
    }

    public void getData(SourceModel source) {
        source.setName(nameTextField.getText()); 
        source.setType((String)typeSelect.getSelectedItem());
    }

    public Collection<SourceModel> getSources() {
        Vector<SourceModel> v = new Vector<SourceModel>();
        for (int i = 0; i < tree.getChildCount(); i++) {
            DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) tree.getChildAt(i);
            v.add((SourceModel) mutableTreeNode.getUserObject());
        }
        return v;
    }


    public void onListChange(JComboBox list) {
        String type = (String) list.getSelectedItem();

        panel = sources.get(type);
        if (panel == null) {
            if (type.indexOf("File") >= 0) {
                panel = new FileEntityView(type);
            } else if (type.indexOf("Remote") >= 0) {
                panel = new RemoteEntityView(type);
            } else if (type.indexOf("Shiva") >= 0) {
                panel = new ShivaEntityView(type);
            } else {
                panel = new SimpleEntityView(type);
            }
        }
        sources.put(type,panel);
        description.removeAll();
        description.add(panel.getPanel());
        description.validate();
        description.getParent().validate();
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            onListChange((JComboBox) e.getSource());
        } else if (e.getSource() instanceof JButton) {
            if (e.getSource() == addButton)
                addOrEditSource();
            else
                removeSource();
            ((DefaultTreeModel) sourceTree.getModel()).reload();
        }
    }

    private void removeSource() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent();
        node.removeFromParent();
    }

    private void addOrEditSource() {
        if (selectedSource == null) {
            SourceModel s = panel.createSourceModel();
            getData(s);
            tree.add(new DefaultMutableTreeNode(s));
        } else {
            if (panel.isCompatibleModel(selectedSource)) {
                panel.getData(selectedSource);
            } else {
                selectedSource = panel.createSourceModel();
                ((DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent()).setUserObject(selectedSource);
            }
            getData(selectedSource);
        }
    }

    private SourceModel selectedSource;

    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) sourceTree.getLastSelectedPathComponent();
        if (node == null || !(node.getUserObject() instanceof SourceModel)) {
            addButton.setText("Add");
            selectedSource = null;
            return;
        }
        selectedSource = (SourceModel) node.getUserObject();
        setData(selectedSource);
        panel.setData(selectedSource);
        addButton.setText("Edit");
    }
}