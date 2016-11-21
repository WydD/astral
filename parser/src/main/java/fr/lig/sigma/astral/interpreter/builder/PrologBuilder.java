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

package fr.lig.sigma.astral.interpreter.builder;

import fr.lig.sigma.astral.interpreter.common.XML2Prolog;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.KnowledgeBase;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import ws.prova.kernel2.ProvaConstant;

import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
public class PrologBuilder {
    private KnowledgeBase kb;
    private QueryNode node;

    public PrologBuilder(KnowledgeBase kb) {
        this.kb = kb;
        xml2Prolog = new XML2Prolog(kb);
    }

    private static final Logger log = Logger.getLogger(PrologBuilder.class);
    private XML2Prolog xml2Prolog;

    public synchronized QueryNode buildQuery(Document dom, AstralCore core) throws Exception {
        String pl = xml2Prolog.getProlog(dom);
        if (pl == null || pl.isEmpty()) return null;
        log.debug("Building query now... " + pl);
        try {
            kb.setGlobal("Core", core);
            Map<String, Object> result = kb.consult(":- solve(buildquery(" + pl + ",[_0,_1],T,Z,S)).", core, this);
            if (result == null)
                throw new RuntimeException("Not result has been provided by prova... crysis!!!!" + kb.getLastMessages());
            node = (QueryNode) result.get("S");
            return node;
        } finally {
            if (log.isTraceEnabled()) {
                log.trace(kb.getLastMessages());
            }
        }
    }

    public QueryNode createNode(String name, Map<String, Object> parameters, List<QueryNode> childs, String id) {
        return new QueryNode(name, parameters, childs, id);
    }
}
