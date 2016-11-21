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

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.interpreter.Interpreter;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;

import javax.swing.*;

/**
 * @author Loic Petit
 */
@Component
public class Gui extends JFrame implements AstralRuntime {
    @Requires
    private AstralEngine engine;
    @Requires(filter = "(instance.name=*Algebra*)")
    private Interpreter inter;
    //@Requires
    //private NetworkFactory nf;

    public MainGui mainGui = new MainGui(this);

    @Validate
    public void ready() {
        setTitle("AStrAL Evaluator GUI");
        setContentPane(mainGui.getPanel());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1024, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public Interpreter getInter() {
        return inter;
    }

    //public NetworkFactory getNf() {
    //    return nf;
    //}

    public AstralEngine getEngine() {
        return engine;
    }
}
