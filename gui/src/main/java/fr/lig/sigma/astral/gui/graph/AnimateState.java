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

package fr.lig.sigma.astral.gui.graph;

import edu.uci.ics.jung.visualization.picking.PickedState;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.log4j.Logger;

/**
 * @author Loic Petit
 */
public class AnimateState implements Runnable {
    private PickedState<QueryNode> pickedState;
    private QueryNode e;
    private Logger log = Logger.getLogger(AnimateState.class);

    public AnimateState(PickedState<QueryNode> pickedState, QueryNode e) {
        this.pickedState = pickedState;
        this.e = e;
    }

    private Thread run;

    public void notice() {
        if (run == null || !run.isAlive()) {
            run = new Thread(this);
            run.start();
        }
    }

    public void run() {
        //log.info("Start animation...");
        try {
            pickedState.pick(e, true);
            Thread.sleep(500);

            pickedState.pick(e, false);
        } catch (InterruptedException ignored) {
            log.error("Animation interrupted", ignored);
        }
    }
}
