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

package fr.lig.sigma.astral.core.operators.window;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.event.EventProcessor;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.operators.window.Window;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import org.apache.log4j.Logger;

/**
 *
 */
public class PositionalAlgorithm implements WindowAlgorithm {
    private Window w;
    private WindowDescription desc;
    private Stream in;
    private EventScheduler es;
    private int processorId;
    private EventProcessor[] wait;

    private long begin;
    private long end = -1;
    private long nextEval;
    private Batch beginB;
    private Batch endB;
    private int i = 0;

    private static Logger log = Logger.getLogger(PositionalAlgorithm.class);
    private boolean keepLeft;

    public PositionalAlgorithm(Window w, WindowDescription desc, Stream in, EventScheduler es, EventProcessor waitingSlot, String boundaries) {
        this.w = w;
        this.desc = desc;
        this.in = in;
        this.es = es;
        keepLeft = boundaries.charAt(0) == '[';
        wait = new EventProcessor[]{waitingSlot};
        log.trace("Init algorithm on " + in.getName());
    }

    @Override
    public void init() throws AxiomNotVerifiedException {
        computeBounds();
        nextEval = end;
    }

    private void computeBounds() throws AxiomNotVerifiedException {
        begin = desc.alpha(i, es) + delta;
        end = desc.beta(i, es) + delta;
        log.trace("Bounds asked (" + i + ") " + begin + ":" + end);

        if (begin > end)
            throw new AxiomNotVerifiedException(AxiomNotVerifiedException.WINDOW_DESCRIPTION_HYPOTHESIS,
                    "The lower bound is higher than the upper");
    }


    private long delta = 0;

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        long rank = in.reversetau(b);
        if (rank < nextEval) return;
        int step = (int) ((rank + delta - nextEval) / desc.r());
        // If the last batch includes more than one tuple 
        i += step;
        computeBounds();

        beginB = in.tau(begin);
        endB = in.tau(end);

        delta = ((delta + in.reversetau(endB)) % desc.r());

        log.trace("Processing " + beginB + ":" + endB + ", size: " + (int) (end - begin + (keepLeft ? 1 : 0)));
        if (beginB.compareTo(endB) > 0)
            beginB = endB;
        w.processWindow(beginB, endB, (int) (end - begin + (keepLeft ? 1 : 0)), b);
        i++;
        nextEval += (step + 1) * desc.r() + delta;
    }

    @Override
    public EventProcessor[] waitFor() {
        return wait;
    }

    public String toString() {
        return "Alg/" + w.toString();
    }
}
