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

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.containers.StreamContainer;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.source.Source;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class StreamSourceHandler extends StreamContainer implements Handler<Stream>, Source {
    @Property(mandatory = true)
    private String id;
    @Requires(id = "core")
    private AstralCore core;

    @Property
    private String entityname;
    @Property
    private String[] schema;
    private Stream in;

    @Override
    public void setInput(Stream in) throws Exception {
        this.in = in;
        entityname = id;
        Set<String> attributes = in.getAttributes();
        schema = attributes.toArray(new String[attributes.size()]);
        core.getSf().prepareSource(this, core);
        setUniqueChild(in);
        in.registerNotifier(this);
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        while(in.hasNext(b)) {
            put(in.pop(), b.getId());
        }
    }

    @Override
    public void firstSchedule() throws Exception {
    }
}
