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
import fr.lig.sigma.astral.common.event.SchedulerContainer;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.misc.Duplicate;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.source.SourceBlocker;
import org.apache.log4j.Logger;

/**
 * @author Loic Petit
 */
public class DuplicateRelation extends AbstractDuplicate<Relation> {
    public DuplicateRelation(Relation in, AstralCore core) {
        super(in, "RelationBufferedVolatileImpl", core);
    }

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        TupleSet ts = in.getContent(b);
        for (Relation r : duplicates) {
            r.update(ts, b);
        }
        //release(b);
    }
}