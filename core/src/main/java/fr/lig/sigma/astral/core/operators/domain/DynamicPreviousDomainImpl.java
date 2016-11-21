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

package fr.lig.sigma.astral.core.operators.domain;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.DynamicRelationOperator;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.domain.TimeTransformer;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Map;

/**
 * @author Loic Petit
 */

@Component
@Provides
public class DynamicPreviousDomainImpl extends DynamicRelationOperator implements Operator, Relation {
    private int id;

    private Batch lastB = new Batch(Long.MIN_VALUE, 0);
    @Requires(id = "in")
    private DynamicRelation in;

    @Property(mandatory = true)
    private TimeTransformer description;

    private TupleSet is, ds;

    @Property
    private Map<String, String> structure;

    public void prepare() {
        assert description instanceof PreviousTime;

        setOutput(createNewFrom(in, in.getAttributes(), "D" + "-" + "(" + in.getName() + ")", structure));
        addInput(in, true);
    }

    private boolean lastWasVoid = true;

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        update(b, in.getInsertedTuples(lastB), in.getDeletedTuples(lastB));
        lastB = b;
    }


    public int getMaxInputs() {
        return 1;
    }

    @Override
    public String toString() {
        return "Domain(PreviousTime)";
    }
}
