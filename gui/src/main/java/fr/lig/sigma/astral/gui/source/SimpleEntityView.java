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
public class SimpleEntityView implements SourceView {
    private JTextField rateTextField;
    private JTextField cardTextField;
    private JPanel panel;
    private String type;

    public SimpleEntityView(String type) {
        this.type = type;
    }


    public void setData(SourceModel data) {
        SimpleEntityModel s = (SimpleEntityModel) data;  
        cardTextField.setText(String.valueOf(s.getCard())); 
        rateTextField.setText(String.valueOf(s.getRate()));
    }

    public void getData(SourceModel data) {
        SimpleEntityModel s = (SimpleEntityModel) data;
        s.setCard(Integer.parseInt(cardTextField.getText())); 
        s.setRate(Integer.parseInt(rateTextField.getText()));
    }

    public SourceModel createSourceModel() {
        SourceModel s =  new SimpleEntityModel(type);
        getData(s);
        return s;
    }

    public JPanel getPanel() {
        return panel;
    }

    public boolean isCompatibleModel(SourceModel selectedSource) {
        return selectedSource instanceof SimpleEntityModel;
    }
}
