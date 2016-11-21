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

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.UnknownSymbolException;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.structure.Entity;
import org.w3c.dom.Element;

import java.util.List;

/**
 *
 */
public interface QueryVisitor<E,F> {
    E sigma(Element e, F in) throws Exception;

    E pi(Element e, F in) throws Exception;

    E rho(Element e, F in) throws Exception;

    E join(Element e, List<F> inputs) throws Exception;

    E window(Element e, F in) throws Exception;

    E streamer(Element e, F in) throws Exception;

    E source(Element e) throws Exception;

    E streamjoin(Element e, List<F> inputs) throws Exception;

    E fix(Element e, F in) throws Exception;

    E domain(Element e, F in) throws Exception;

    E aggregation(Element e, F in) throws Exception;

    E union(Element e, List<F> inputs) throws Exception;

    E spread(Element e, F in) throws Exception;

    E evaluate(Element e, F in) throws Exception;

    E query(Element e, F in) throws Exception;
}
