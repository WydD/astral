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

import javax.swing.*;

/**
 * @author Loic Petit
 */
public class Main extends JFrame {

    public static final MainGui mainGui = new MainGui(new Gui());

    public Main() {
        setTitle("AStrAL Evaluator GUI");
        setContentPane(mainGui.getPanel());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 500);
        setLocationRelativeTo(null);
        setVisible(true);

    }

    public static void main(String args[]) {
        new Main();
    }
}
