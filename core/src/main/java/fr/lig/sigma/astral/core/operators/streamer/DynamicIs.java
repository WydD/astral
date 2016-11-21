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

package fr.lig.sigma.astral.core.operators.streamer;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.StreamOperator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.List;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class DynamicIs extends StreamOperator implements Operator {
    @Requires(id = "in")
    private DynamicRelation in;
    private int id = 0;

    @Property
    private List<String> attributes;

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public void prepare() throws Exception {
        setOutput((Stream) entityFactory.instanciateEntity("StreamQueueImpl", "IS(" + in.getName() + ")", new AttributeSet(attributes)));
        addInput(in, true);
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        TupleSet insertedTuples = in.getInsertedTuples(b);
        TupleSet toCommit = entityFactory.instanciateTupleSet(getAttributes());
        for (Tuple t : insertedTuples) {
            Tuple t1 = new Tuple(t, id++);
            t1.put(Tuple.TIMESTAMP_ATTRIBUTE, b.getTimestamp());
            toCommit.add(t1);
        }

        putAll(toCommit, b.getId());
    }
}
