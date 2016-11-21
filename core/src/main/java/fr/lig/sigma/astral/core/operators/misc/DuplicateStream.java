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

package fr.lig.sigma.astral.core.operators.misc;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.query.AstralCore;

/**
 * @author Loic Petit
 */
public class DuplicateStream extends AbstractDuplicate<Stream> {

    public DuplicateStream(Stream in, AstralCore core) {
        super(in, "StreamQueueImpl", core);
    }

    private Tuple buffer = null;
    private Batch bufferB;

    public synchronized void processEvent(Batch b) throws AxiomNotVerifiedException {
        bufferB = null;
        TupleSet ts = null;
        while (in.hasNext(b)) {
            Tuple t = in.pop();
            Batch Bt = in.B(t);
            t = (Tuple) t.clone();
            if (!Bt.equals(bufferB)) {
                commitTS(ts);
                ts = core.getEf().instanciateTupleSet(in.getAttributes());
            }
            assert ts != null;
            ts.add(t);
            bufferB = Bt;
        }
        commitTS(ts);
    }

    private void commitTS(TupleSet ts) throws AxiomNotVerifiedException {
        if (ts == null) return;
        for (Stream s : duplicates) {
            s.putAll(ts, bufferB.getId());
        }
    }
}
