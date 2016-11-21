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

package fr.lig.sigma.astral.core.operators.set;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Arrays;
import java.util.Map;

/**
 *
 */

@Component
@Provides
public class UnionRelation extends RelationOperator implements Operator {
    @Requires(id = "in", policy = "dynamic-priority")
    private Relation[] in;
    @Property
    private Map<String, String> structure;

    public void prepare() {
        Relation left = in[0];
        Relation right = in[1];
        if (!left.getAttributes().equals(right.getAttributes()))
            throw new IllegalStateException("Attributes set are not the same between the left and the right side");
        setOutput(createNewFrom(left, left.getAttributes(), left.getName() + " \\cup " + right.getName(), structure));
        addInput(left, true);
        addInput(right, true);
    }

    private Batch preventSimultaneous = null;

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        if (preventSimultaneous == b) return;
        preventSimultaneous = b;

        union(b, Arrays.asList(in), this, entityFactory, false);
    }

    public static void union(Batch b, Iterable<Relation> relations, Relation out, EntityFactory entityFactory, boolean keepIds) {
        long id = 0;

        TupleSet ts = entityFactory.instanciateTupleSet(out.getAttributes());
        for (Relation r : relations) {
            TupleSet content = entityFactory.ensureOrderedTupleSet(r.getContent(b));
            for (Tuple t : content)
                ts.add(new Tuple(t, keepIds ? t.getId() : id++));
        }
        out.update(ts, b);
    }

    @Override
    public int getMaxInputs() {
        return 2;
    }

    @Override
    public String toString() {
        return "Union";
    }
}
