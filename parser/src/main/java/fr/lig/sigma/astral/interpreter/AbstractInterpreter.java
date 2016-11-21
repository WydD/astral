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
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.interpreter.builder.PrologBuilder;
import fr.lig.sigma.astral.interpreter.builder.QueryBuilder;
import fr.lig.sigma.astral.interpreter.common.XMLUtils;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryNode;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.SourceAlreadyExistsException;
import fr.lig.sigma.astral.source.UnknownSourceException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.UUID;

/**
 *
 */
public abstract class AbstractInterpreter implements Interpreter {
    protected static Logger log = Logger.getLogger(AlgebraInterpreter.class);

    public QueryRuntime query(String query, AstralEngine engine, boolean autostart) throws ParsingException, InterpreterException, AxiomNotVerifiedException {
        Document doc = parse(query);
        AstralCore core = engine.createCore();
        inferSource(doc, core);
        optimize(doc, engine);
        validateDocument(doc);
        if (log.isTraceEnabled())
            log.trace("Building query:\n" + XMLUtils.xmlToString(doc));
        QueryRuntime qr = build(doc, core, engine);
        if (autostart)
            qr.start();
        return qr;
    }

    public QueryRuntime query(String query, AstralEngine engine) throws ParsingException, InterpreterException, AxiomNotVerifiedException {
        return query(query, engine, false);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void validateDocument(Document doc) {
        // No validation for now
    }

    public void inferSource(Document doc, AstralCore core) throws InterpreterException {
        long t = System.currentTimeMillis();
        QueryBuilder qb = new QueryBuilder(core);
        try {
            qb.induceSource(doc);
        } catch (InstanceCreationException e) {
            throw new RuntimeException(e);
        } catch (SourceAlreadyExistsException e) {
            throw new InterpreterException(e);
        } catch (UnknownSourceException e) {
            throw new InterpreterException(e);
        }
        log.trace((System.currentTimeMillis() - t) + "ms to infer sources");
    }

    public void optimize(Document doc, AstralEngine engine) throws InterpreterException {
    }

    public QueryRuntime build(Document doc, AstralCore core, AstralEngine engine) throws AxiomNotVerifiedException, InterpreterException {
        long t = System.currentTimeMillis();
        QueryBuilder qb = new QueryBuilder(core);
        Element queryNode = (Element) XMLUtils.seek("//query[1]", doc);
        String name = null;
        if (queryNode != null) {
            name = queryNode.getAttribute("name");

            String t0S = queryNode.getAttribute("t0");
            try {
                long t0 = Long.parseLong(t0S);
                core.getEs().setT0(t0);
            } catch (NumberFormatException e) {
            }
            String depends = queryNode.getAttribute("depends");
            if (depends != null && !depends.isEmpty()) {
                for (String dep : depends.split(",")) {
                    QueryRuntime qr = engine.getDeclaredQuery(dep);
                    if (qr == null) {
                        log.warn("The query " + depends + " has not been found, therefore I can't establish the dependency");
                    } else {
                        core.getEs().addDependentEventScheduler(qr.getCore().getEs(), true);
                    }
                }
            }
            String mode = queryNode.getAttribute("sync");
            if ("none".equals(mode))
                core.getEs().setWaitMode(false);
        }
        if (name == null || name.isEmpty())
            name = UUID.randomUUID().toString();
        PrologBuilder prolog = new PrologBuilder(engine.getKnowledgeBase());

        try {
            //Entity res = qb.buildQuery(doc);
            QueryNode node = prolog.buildQuery(doc, core);
            if (node == null) return null;
            Entity res = (Entity) engine.getServiceFromId(node.getId());
            log.debug("Created query entity: " + res);
            if (res == null) return null;
            QueryRuntime qr = engine.declareQuery(core, res, name);
            qr.setQueryNode(node);
            qb.buildHandlers(doc, qr, core, node);

            log.trace((System.currentTimeMillis() - t) + "ms to build");
            return qr;
        } catch (Exception e) {
            e.printStackTrace();
            throw new InterpreterException(e);
        }
    }
}
