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

package fr.lig.sigma.astral.core.operators.relational;

import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.structure.DynamicRelation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.operators.DynamicRelationOperator;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.relational.UnaryRelationalOperation;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class UnaryDynamicRelationOperator extends DynamicRelationOperator implements Operator {
    private LinkedList<UnaryRelationalOperation> unaryOperations = new LinkedList<UnaryRelationalOperation>();


    @Property(mandatory = true)
    private List<String> attributes;
    @Property(mandatory = true)
    private List<Map<String,Object>> operations;
    @Property
    private Map<String,String> structure;

    @Requires(id = "in")
    private DynamicRelation in;
    @Requires(id = "core")
    private AstralCore core;

    private static final Logger log = Logger.getLogger(UnaryDynamicRelationOperator.class);

    @Override
    public int getMaxInputs() {
        return 1;
    }

    public void addOperation(UnaryRelationalOperation op) {
        unaryOperations.add(op);
    }

    @Invalidate
    private void destroy() {
        for(UnaryRelationalOperation op : unaryOperations)
            AbstractPojoFactory.dispose(op);
    }

    public void prepare() throws Exception {
        for(Map<String,Object> op : operations)
            unaryOperations.add(core.getOf().getUnaryFactory().instanciateUnaryOperator(op));
        String name = in.getName();
        Set<String> attributes = new AttributeSet(this.attributes);
        for (UnaryRelationalOperation op : unaryOperations) {
            name = op.getOperationName()+"/"+name;
        }
        setOutput(createNewFrom(in, attributes, name, structure));
        addInput(in, true);
    }

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        TupleSet inIS = in.getInsertedTuples(b);
        TupleSet inDS = in.getDeletedTuples(b);
        TupleSet is = entityFactory.instanciateTupleSet(getAttributes());
        TupleSet ds = entityFactory.instanciateTupleSet(getAttributes());
        UnaryRelationOperator.computeOperations(inIS, is, unaryOperations);
        UnaryRelationOperator.computeOperations(inDS, ds, unaryOperations);
        update(b, is, ds);
    }

    @Override
    public String toString() {
        String res = "";
        for(UnaryRelationalOperation op : unaryOperations)
            res += op.getOperationName()+"\n";
        return res.substring(0,res.length()-1);
    }
}
