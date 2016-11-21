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

package fr.lig.sigma.astral.interpreter.common;

import org.w3c.dom.Element;

import java.util.List;

/**
 *
 */
public abstract class AbstractGuide<E,F,G> implements Guide<E,G> {
    protected QueryVisitor<E, F> visitor;

    public AbstractGuide(QueryVisitor<E, F> visitor) {
        this.visitor = visitor;
    }

    protected E performVisit(Element e, List<F> inputs) throws Exception {
        if ("sigma".equals(e.getTagName()))
            return visitor.sigma(e, inputs.get(0));
        if ("rho".equals(e.getTagName()))
            return visitor.rho(e, inputs.get(0));
        if ("pi".equals(e.getTagName()))
            return visitor.pi(e, inputs.get(0));
        if ("window".equals(e.getTagName()))
            return visitor.window(e, inputs.get(0));
        if ("join".equals(e.getTagName()))
            return visitor.join(e, inputs);
        if ("streamjoin".equals(e.getTagName()))
            return visitor.streamjoin(e, inputs);
        if ("source".equals(e.getTagName()))
            return visitor.source(e);
        if ("streamer".equals(e.getTagName()))
            return visitor.streamer(e, inputs.get(0));
        if ("fix".equals(e.getTagName()))
            return visitor.fix(e, inputs.get(0));
        if ("domain".equals(e.getTagName()))
            return visitor.domain(e, inputs.get(0));
        if ("aggregation".equals(e.getTagName()))
            return visitor.aggregation(e, inputs.get(0));
        if ("union".equals(e.getTagName()))
            return visitor.union(e, inputs);
        if ("spread".equals(e.getTagName()))
            return visitor.spread(e, inputs.get(0));
        if ("evaluate".equals(e.getTagName()))
            return visitor.evaluate(e, inputs.get(0));
        if ("query".equals(e.getTagName()))
            return visitor.query(e, inputs.get(0));

        throw new UnsupportedOperationException("Not yet implemented operator creation " + e.getTagName());
    }
}
