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
import fr.lig.sigma.astral.handler.AbstractHandler;
import fr.lig.sigma.astral.handler.TupleSender;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;

/**
 *
 */
@Component
@Provides
public class TupleSenderHandler extends AbstractHandler<Stream> {
    @Requires
    private TupleSender sender;
    @Property(value = "")
    private String description;
    @Requires(id = "core")
    private AstralCore core;

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        while(in.hasNext(b)) {
            sender.sendTuple(core.getQR().getDisplayName(), description, in.pop());
        }
    }
}
