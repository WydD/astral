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

/**
 * @author Loic Petit
 */
public class RemoteEntityView implements SourceView {
    private JPanel panel;
    private JTextField addressTextField;
    private JTextField portTextField;
    private String type;

    public RemoteEntityView(String type) {
        this.type = type;
    }

    public void setData(SourceModel data) {
        RemoteEntityModel m = (RemoteEntityModel) data;
        addressTextField.setText(m.getAddress());
        portTextField.setText(String.valueOf(m.getPort()));
    }

    public void getData(SourceModel data) {
        RemoteEntityModel m = (RemoteEntityModel) data;
        m.setAddress(addressTextField.getText());
        m.setPort(Integer.parseInt(portTextField.getText()));
    }

    public SourceModel createSourceModel() {
        SourceModel m = new RemoteEntityModel(type);
        getData(m);
        return m;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isCompatibleModel(SourceModel selectedSource) {
        return selectedSource instanceof RemoteEntityModel;
    }
}
