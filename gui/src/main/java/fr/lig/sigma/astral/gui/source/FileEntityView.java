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
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * @author Loic Petit
 */
public class FileEntityView implements ActionListener, SourceView {
    private JTextField selectedFileTextField;
    private JButton browseButton;
    private JPanel panel;
    private static final JFileChooser chooser = new JFileChooser();
    private String type;

    public FileEntityView(String type) {
        chooser.setMultiSelectionEnabled(false);
        chooser.setAcceptAllFileFilterUsed(true);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Astral Formated file", "stream", "relation");
        chooser.setFileFilter(filter);
        this.type = type;
        browseButton.addActionListener(this);
    }

    public void actionPerformed(ActionEvent actionEvent) {
        new Thread(new Runnable() {

            public void run() {
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    selectedFileTextField.setText(chooser.getSelectedFile().toString());
                }
            }
        }).start();
    }

    public void setData(SourceModel data) {
        FileEntityModel s = (FileEntityModel) data;
        chooser.setSelectedFile(s.getSourceFile());
        selectedFileTextField.setText(chooser.getSelectedFile().toString());
    }

    public void getData(SourceModel data) {
        FileEntityModel s = (FileEntityModel) data;
        s.setSourceFile(chooser.getSelectedFile());

    }

    public SourceModel createSourceModel() {
        SourceModel s = new FileEntityModel(type);
        getData(s);
        return s;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isCompatibleModel(SourceModel selectedSource) {
        return selectedSource instanceof FileEntityModel;
    }
}
