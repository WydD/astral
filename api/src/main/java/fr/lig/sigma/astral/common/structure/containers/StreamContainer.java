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

package fr.lig.sigma.astral.common.structure.containers;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.structure.VolatileStructure;
import org.apache.felix.ipojo.annotations.Component;

/**
 * The stream container is an abstract class that is a proxy for another stream.
 * This class is often used for operators. A typical example would be:
 * <pre>
 *      public class A implements SomeOperator {
 *          public setInput(Stream r) {
 *              setOutput(createNewFrom(r, attribs, name));
 *          }
 *      }
 * </pre>
 *
 * Starting from the <code>setOutput</code>, the object has a valid stream behaviour.
 * @author Loic Petit
 */
@Component
public abstract class StreamContainer extends AbstractEntityContainer<Stream> implements Stream {
    @Override
    public Batch B(Tuple t) {
        return output.B(t);
    }

    public long reversetau(Batch b) {
        return output.reversetau(b);
    }

    public Batch tau(long n) {
        return output.tau(n);
    }

    public Tuple peek() {
        return output.peek();
    }

    public Tuple pop() {
        return output.pop();
    }

    public void putAll(TupleSet ts, int i) throws AxiomNotVerifiedException {
        output.putAll(ts, i);
    }

    public void put(Tuple t, int i) throws AxiomNotVerifiedException {
        output.put(t, i);
    }

    @Override
    public void put(Tuple t) throws AxiomNotVerifiedException {
        output.put(t);
    }

    @Override
    public void putAll(TupleSet ts) throws AxiomNotVerifiedException {
        output.putAll(ts);
    }

    @Override
    public boolean hasNext(Batch b) {
        return output.hasNext(b);
    }
}
