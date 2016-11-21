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
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.handler.AbstractHandler;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class PrintStreamHandler extends AbstractHandler<Stream> {
    @Property
    private PrintStream out;

    private List<String> attribList;

    private long lastTimestamp;

    @Property
    private String file;
    @Property
    private String stampAttribute;

    @Validate
    private void ready() throws FileNotFoundException {
        if (file != null) {
            out = new PrintStream(new FileOutputStream(file));
        }
    }

    @Override
    public void setInput(Stream in) {
        super.setInput(in);
        if (out == null) out = System.out;
        attribList = new LinkedList<String>(in.getAttributes());
        out.print(AttributeSet.string(attribList));
        if (stampAttribute != null)
            out.print("," + stampAttribute);
        out.println();
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        boolean first = true;
        while (in.hasNext(b)) {
            if (first && b.getTimestamp() == lastTimestamp)
                out.println("--");
            Tuple t = in.pop();
            out.print(t.toString(attribList));
            if (stampAttribute != null)
                out.print("," + System.currentTimeMillis());
            out.println();
            out.flush();
            first = false;
        }
        lastTimestamp = b.getTimestamp();

    }
}
