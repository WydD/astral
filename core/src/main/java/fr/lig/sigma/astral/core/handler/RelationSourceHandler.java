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
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;
import fr.lig.sigma.astral.handler.Handler;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.source.Source;
import fr.lig.sigma.astral.source.SourceAlreadyExistsException;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

import java.util.Set;

/**
 * @author Loic Petit
 */
@Provides
@Component
public class RelationSourceHandler extends RelationContainer implements Source, Handler<Relation> {
    @Property(mandatory = true)
    private String id;
    @Requires(id = "core")
    private AstralCore core;

    @Property
    private String entityname;
    @Property
    private String[] schema;
    private Relation in;

    @Override
    public void setInput(Relation in) throws WrongAttributeException, SourceAlreadyExistsException, InstanceCreationException {
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
        update(in.getContent(b), b);
    }

    @Override
    public void firstSchedule() throws Exception {
    }
}
