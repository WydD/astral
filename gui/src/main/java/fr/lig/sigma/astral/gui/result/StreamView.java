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

package fr.lig.sigma.astral.gui.result;

import fr.lig.sigma.astral.common.Tuple;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;
import java.util.Set;

/**
 * @author Loic Petit
 */
public class StreamView implements EntityView {
    private JTable contentTable;
    private JPanel panel;
    private Vector<String> attribVector;
    private DefaultTableModel tableModel;
    private long guiTime;
    private long guiTimeDiff = 0;

    public void setAttributes(Set<String> attributes) {  
        guiTime = 0;
        attribVector = new Vector<String>(attributes);
        tableModel = new DefaultTableModel(attribVector, 0);
        contentTable.setModel(tableModel);
        contentTable.validate();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void insertRow(Tuple t) {   
        long time = System.nanoTime();
        int count = attribVector.size();
        Object[] row = new Object[count];
        for (int i = 0; i < count; i++) {
            row[i] = t.get(attribVector.get(i));
        }
        tableModel.insertRow(0,row);    
        guiTime += System.nanoTime() - time;
    } 
    public long getGuiTime() {
        return guiTime/1000000;
    }

    public long getGuiTimeDiff() {
        long t = guiTime-guiTimeDiff;
        guiTimeDiff = guiTime;
        return t;
    }
}
