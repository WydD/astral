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

package fr.lig.sigma.astral.gui.controller;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.gui.result.RelationView;

/**
 * @author Loic Petit
 */
public class RelationController implements EventProcessor {
    private RelationView view;
    private Relation r;
    private EventProcessor[] waitList;

    public RelationController(RelationView view, Relation r) {
        this.view = view;
        this.r = r;
        r.registerNotifier(this);
        waitList = new EventProcessor[]{r};
        view.setAttributes(r.getAttributes());
    }

    public void processEvent(Batch timestamp) throws AxiomNotVerifiedException {
        view.setContent(timestamp, r.getContent(timestamp));
    }

    public EventProcessor[] waitFor() {
        return waitList;
    }
    
}
