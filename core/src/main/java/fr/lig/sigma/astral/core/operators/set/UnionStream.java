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
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.StreamOperator;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

/**
 *
 */

@Component
@Provides
public class UnionStream extends StreamOperator implements Operator {

    @Requires(id = "in", policy = "dynamic-priority")
    private Stream[] in;

    private int internalId = 0;

    public void prepare() {
        Stream left = in[0];
        Stream right = in[1];
        if (!left.getAttributes().equals(right.getAttributes())) {
            throw new IllegalStateException("Attributes set are not the same between the left and the right side");
        }
        setOutput(createNewFrom(left, left.getAttributes(), left.getName() + " \\cup " + right.getName()));
        addInput(left, true);
        addInput(right, true);
    }

    private Batch preventSimultaneous = null;

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        if (preventSimultaneous == b) return;
        preventSimultaneous = b;
        for (Stream s : in) {
            while (s.hasNext(b)) {
                Tuple t = new Tuple(s.pop(), internalId++);
                put(t, b.getId());
            }
        }
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
