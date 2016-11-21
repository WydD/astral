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

import java.util.LinkedList;
import java.util.Queue;

/**
 *
 */
public class TemporalAlgorithm implements WindowAlgorithm {
    private Window w;
    private WindowDescription desc;
    private Stream in;
    private EventScheduler es;
    private int processorId;
    private EventProcessor[] wait;

    private long end = -1;
    private Batch beginB;
    private Batch endB;
    private int i = 0;

    private long listen = -1;
    private long nextEval;

    private Queue<FrameDesc> toCommit = new LinkedList<FrameDesc>();
    private static Logger log = Logger.getLogger(TemporalAlgorithm.class);
    private boolean keepLeft;

    public TemporalAlgorithm(Window w, WindowDescription desc, Stream in, EventScheduler es, EventProcessor waitingSlot, String boundaries) {
        this.w = w;
        this.desc = desc;
        this.in = in;
        this.es = es;
        keepLeft = boundaries.charAt(0) == '[';
        wait = new EventProcessor[]{waitingSlot};
    }

    public void init() throws AxiomNotVerifiedException {
        processorId = es.registerIndependentProcessor(this);
        es.pushIndependentEvent(Batch.MIN_VALUE, processorId);
    }

    private Batch lastB;

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        log.trace("New event in window: " + b + " listen: " + listen);
        //Init process
        if (end < 0) {
            computeBounds();
            nextEval = end;
            es.pushIndependentEvent(endB, processorId);
            es.pushIndependentEvent(new Batch(end, Integer.MAX_VALUE), processorId);
            return;
        }
        lastB = b; // After init process, in case of t0 = \beta(0)
        if (listen >= 0 && listen < b.getTimestamp()) {
            listen = -1;
            in.unregisterNotifier(this);
            log.trace("Unregistered to the input");
            if (b.getTimestamp() < nextEval)
                return;
        }

        if (b.getId() == Integer.MAX_VALUE && b.getTimestamp() == nextEval) {
            // The last frame is completed
            i++;
            computeBounds();
            nextEval = b.getTimestamp() + desc.r();
            es.pushIndependentEvent(new Batch(nextEval, 0), processorId);
            es.pushIndependentEvent(new Batch(nextEval, Integer.MAX_VALUE), processorId);
            if (end < nextEval) {
                es.pushIndependentEvent(endB, processorId);
            }
            return;
        }
        if (end == b.getTimestamp()) {
            if (listen == -1) {
                // Setup listen
                listen = b.getTimestamp();
                in.registerNotifier(this);
                log.trace("Registered to the input");
            }
            toCommit.offer(new FrameDesc(beginB, b, new Batch(nextEval, b.getId())));
        }
        if (!toCommit.isEmpty() && toCommit.peek().batch.equals(b)) {
            TemporalAlgorithm.FrameDesc frameDesc = toCommit.poll();
            log.trace("Commiting frame " + frameDesc.begin + ":" + frameDesc.end + " at " + frameDesc.batch);
            frameDesc.process(w);
        }
    }

    private void computeBounds() throws AxiomNotVerifiedException {
        long begin = desc.alpha(i, es);
        end = desc.beta(i, es);
        beginB = new Batch(begin, keepLeft ? 0 : Integer.MAX_VALUE);
        endB = new Batch(end, 0);
        if (begin > end)
            throw new AxiomNotVerifiedException(AxiomNotVerifiedException.WINDOW_DESCRIPTION_HYPOTHESIS,
                    "The lower bound is higher than the upper");
    }

    public String toString() {
        return "Alg/" + w.toString();
    }

    @Override
    public EventProcessor[] waitFor() {
        return wait;
    }

    private class FrameDesc {
        private Batch begin;
        private Batch end;
        private Batch batch;

        private FrameDesc(Batch begin, Batch end, Batch batch) {
            this.begin = begin;
            this.end = end;
            this.batch = batch;
        }

        private void process(Window w) {
            if (begin.compareTo(end) > 0)
                begin = end;
            w.processWindow(begin, end, -1, batch);
        }

        public String toString() {
            return batch.toString();
        }
    }
}
