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

package fr.lig.sigma.astral.core.handler;

import fr.lig.sigma.astral.common.*;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.handler.AbstractHandler;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class PrintRelationHandler extends AbstractHandler<Relation> {

    @Property
    private PrintStream out;
    private LinkedList<String> attribList;

    @Property
    private String file;

    @Validate
    private void ready() throws FileNotFoundException {
        if (file != null) {
            out = new PrintStream(new FileOutputStream(file));
        }
    }

    @Override
    public void setInput(Relation in) {
        super.setInput(in);
        if (out == null) out = System.out;
        attribList = new LinkedList<String>(in.getAttributes());
        out.println(AttributeSet.string(attribList));
    }

    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        out.println("t=" + b.getTimestamp());
        TupleSet content = in.getContent(b);

        if (content.size() == 0) return;
        Iterable<Tuple> it = content;
        if (!content.isOrdered()) {
            TreeSet<Tuple> ts = new TreeSet<Tuple>();
            for (Tuple t : content)
                ts.add(t);
            it = ts;
        }
        for (Tuple t : it)
            out.println(t.toString(attribList));
    }
}
