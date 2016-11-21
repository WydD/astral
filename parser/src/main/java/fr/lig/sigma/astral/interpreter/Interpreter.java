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

package fr.lig.sigma.astral.interpreter;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.query.QueryRuntime;
import org.w3c.dom.Document;

/**
 * @author Loic Petit
 */
public interface Interpreter {
    QueryRuntime query(String query, AstralEngine engine) throws ParsingException, AxiomNotVerifiedException, InterpreterException;
    QueryRuntime query(String query, AstralEngine engine, boolean autostart) throws ParsingException, AxiomNotVerifiedException, InterpreterException;
    Document parse(String query) throws ParsingException;
    void inferSource(Document doc, AstralCore core) throws InterpreterException;
    void optimize(Document doc, AstralEngine engine) throws InterpreterException;
    QueryRuntime build(Document doc, AstralCore core, AstralEngine engine) throws AxiomNotVerifiedException, InterpreterException;

    /*Entity buildFromString(String query, SourceManager sm, SourceFactory sf, OperatorFactory of, EventScheduler es) throws ParsingException;
    Document onlyParse(String query, SourceManager sm, SourceFactory sf, OperatorFactory of) throws ParsingException;*/
}
