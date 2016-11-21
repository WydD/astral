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

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.VolatileStructure;

import java.util.ArrayDeque;
import java.util.Collection;

/**
 * @author Loic Petit
 */
public class TupleBuffer {

    private ArrayDeque<Tuple> buffer = new ArrayDeque<Tuple>();

    private Stream in;
    private final boolean includeLeftBoundary;
    private final boolean includeRightBoundary;

    public TupleBuffer(Stream in, boolean includeLeftBoundary, boolean includeRightBoundary) {
        this.in = in;
        this.includeLeftBoundary = includeLeftBoundary;
        this.includeRightBoundary = includeRightBoundary;
        System.out.println(includeLeftBoundary + " " + includeRightBoundary);
    }


    public void fillBufferUntil(Batch end, TupleSet added) {
        Tuple t = in.peek();
        if (t == null)
            return;
        int i = in.B(t).compareTo(end);
        while (i < 0 || includeRightBoundary && i == 0) {
            added.add(in.pop());
            buffer.offer(t);
            t = in.peek();
            if (t == null) return;
            i = in.B(t).compareTo(end);
        }
    }

    public void clearBufferUntil(Batch begin, TupleSet deleted) {
        Tuple t = buffer.peek();
        if (t != null) {
            int i = in.B(t).compareTo(begin);
            while (i < 0 || !includeLeftBoundary && i == 0) {
                deleted.add(buffer.pop());
                t = buffer.peek();
                if (t == null) break;
                i = in.B(t).compareTo(begin);
            }
        }
        if (in instanceof VolatileStructure)
            ((VolatileStructure) in).forgetDataBefore(begin);
    }

    public void clearBuffer(int size, TupleSet is, TupleSet ds) {
        if (size >= 0) {
            while (buffer.size() > size) {
                Tuple t = buffer.pop();
                boolean hasBeenRemoved = is.remove(t);
                if (!hasBeenRemoved)
                    ds.add(t);
            }
        }
    }

    public Collection<Tuple> getContent() {
        return buffer;
    }
}
