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

package fr.lig.sigma.astral.core.query;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.structure.EntityFactory;
import fr.lig.sigma.astral.core.common.AbstractPojoFactory;
import fr.lig.sigma.astral.operators.aggregation.AggregateFactory;
import fr.lig.sigma.astral.operators.relational.evaluate.ExpressionAnalyzer;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.common.event.EventScheduler;
import fr.lig.sigma.astral.operators.OperatorFactory;
import fr.lig.sigma.astral.handler.HandlerFactory;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.SourceFactory;
import fr.lig.sigma.astral.source.SourceManager;
import org.apache.felix.ipojo.annotations.*;

@Component(immediate = true)
@Provides
public class AstralCoreImpl implements AstralCore {
    @Property(mandatory = true)
    private SourceFactory sf;
    @Requires
    private OperatorFactory of;
    @Requires
    private EventScheduler es;
    @Property(mandatory = true)
    private SourceManager sm;
    @Property(mandatory = true)
    private EntityFactory ef;
    @Property(mandatory = true)
    private HandlerFactory hf;
    @Property(mandatory = true)
    private ExpressionAnalyzer ea;
    @Property(mandatory = true)
    private AggregateFactory af;
    @Property(mandatory = true)
    private AstralEngine engine;
    private QueryRuntime qr;

    @Validate
    private void ready() {
        of.setCore(this);
    }

    public EntityFactory getEf() {
        return ef;
    }

    public SourceFactory getSf() {
        return sf;
    }

    public OperatorFactory getOf() {
        return of;
    }

    public EventScheduler getEs() {
        return es;
    }

    public SourceManager getSm() {
        return sm;
    }

    public HandlerFactory getHf() {
        return hf;
    }

    public ExpressionAnalyzer getEa() {
        return ea;
    }

    @Override
    public AggregateFactory getAf() {
        return af;
    }

    @Override
    public QueryRuntime getQR() {
        return qr;
    }

    @Override
    public void setQR(QueryRuntime qr) {
        this.qr = qr;
    }

    @Override
    public AstralEngine getEngine() {
        return engine;
    }

    @Invalidate
    private void invalidate() {
        of.setCore(null);
        es.setRuntime(null);
        setQR(null);
    }
}
