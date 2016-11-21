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
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import org.apache.felix.ipojo.annotations.Component;

/**
 * The relation container is an abstract class that is a proxy. This class is often used for operators. A typical
 * example would be:
 * <pre>
 *      public class A implements SomeOperator {
 *          public setInput(Relation r) {
 *              setOutput(createNewFrom(r, attribs, name));
 *          }
 *      }
 * </pre>
 *
 * Starting from the <code>setOutput</code>, the object has a valid relation behaviour.
 * @author Loic Petit
 */
@Component
public abstract class DynamicRelationContainer extends AbstractEntityContainer<DynamicRelation> implements DynamicRelation {
    public void update(TupleSet content, Batch b) {
        output.update(content, b);
    }

    public TupleSet getContent(Batch b) {
        return output.getContent(b);
    }

    @Override
    public TupleSet getInsertedTuples(Batch b) {
        return output.getInsertedTuples(b);
    }

    @Override
    public TupleSet getDeletedTuples(Batch b) {
        return output.getDeletedTuples(b);
    }

    @Override
    public void update(Batch b, TupleSet insertedTuples, TupleSet deletedTuples) {
        output.update(b,insertedTuples,deletedTuples);
    }
}
