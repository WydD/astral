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

package fr.lig.sigma.astral.source;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;

/**
 *
 */
public class BlockedRelationSource extends RelationContainer implements Source {
    protected SourceBlocker blocker;
    public BlockedRelationSource() {
    }

    @Override
    public void update(TupleSet content, Batch b) {
        super.update(content, b);
        blocker.release(b);
    }

    @Override
    public TupleSet getContent(Batch b) {
        return super.getContent(b);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void firstSchedule() throws Exception {
        blocker = new SourceBlocker(getScheduler());
        setUniqueChild(blocker);
        blocker.firstSchedule();
    }
}
