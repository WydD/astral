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
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.gui.result.StreamView;

/**
 * @author Loic Petit
 */
public class StreamController implements EventProcessor {
    private Stream s;
    private EventProcessor[] waitList;
    private StreamView view;

    public StreamController(StreamView view, Stream s) {
        this.view = view;
        this.s = s;
        s.registerNotifier(this);
        waitList = new EventProcessor[]{s};     
        view.setAttributes(s.getAttributes());
    }

    public void processEvent(Batch timestamp) throws AxiomNotVerifiedException {
        Tuple t = s.pop();
        while (t != null) {
            view.insertRow(t);
            t = s.pop();
        }
    }

    public EventProcessor[] waitFor() {
        return waitList;
    }
}
