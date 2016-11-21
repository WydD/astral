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

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.DynamicRelationOperator;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.operators.window.WindowDescription;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class LastBatchWindow extends DynamicRelationOperator implements Operator {
    @Requires(id = "in")
    private Stream in;
    @Property(mandatory = true)
    private WindowDescription description;
    @Property
    private Map<String,String> structure;

    private boolean oneTupleOnly;
    private static Logger log = Logger.getLogger(LastBatchPartitionedWindowImpl.class);
    private TupleSet oldTS;

    @Override
    public void processEvent(Batch timestamp) throws AxiomNotVerifiedException {
        TupleSet ts = entityFactory.instanciateTupleSet(getAttributes());
        Tuple t = null;
        while (in.hasNext(timestamp)) {
            t = in.pop();
            if (!oneTupleOnly)
                ts.add(t);
        }
        if (oneTupleOnly && t != null)
            ts.add(t);
        update(timestamp, ts, oldTS);
        oldTS = ts;
    }

    @Override
    public int getMaxInputs() {
        return 1;
    }

    @Override
    public void prepare() throws Exception {
        oneTupleOnly = description instanceof LinearPositionalDescription;

        setOutput((DynamicRelation) entityFactory.instanciateEntity(
                "NativeDynamicRelation",
                in.getName() + "[" + description + "]",
                in.getAttributes(),
                new Hashtable<String, Object>(structure))
        );
        addInput(in, true);
        oldTS = entityFactory.instanciateTupleSet(getAttributes());
    }


    @Override
    public String toString() {
        return "Window";
    }
}
