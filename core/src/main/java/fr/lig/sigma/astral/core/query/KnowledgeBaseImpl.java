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

import fr.lig.sigma.astral.query.KnowledgeBase;
import fr.lig.sigma.astral.query.QueryNode;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import ws.prova.api2.ProvaCommunicator;
import ws.prova.api2.ProvaCommunicatorImpl;
import ws.prova.exchange.ProvaSolution;
import ws.prova.kernel2.ProvaConstant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
@Component
@Provides
@Instantiate
public class KnowledgeBaseImpl implements KnowledgeBase {
    private static String[] coreRules = new String[]{
            "genericcomparator",
            "list",
            "map",
            "applyrule",
            "parse",
            "syntacticsugar",
            "parseattributes",
            "macro",
            "typerules",
            "attribrules",
            "pushprojection",
            "optimize",
            "implrules",
            "unarywrapping",
            "buildquery",
            "infertree"
    };
    private int consultId = 0;
    private ProvaCommunicator prova;
    private static final Logger log = Logger.getLogger(KnowledgeBaseImpl.class);

    private static StringBuilder additionalRules = new StringBuilder();
    private boolean changed = true;
    private char[] buffer = new char[0x1000];
    private String messages;
    private Map<String, Object> constants = new HashMap<String, Object>();
    private BundleContext ctxt;

    public KnowledgeBaseImpl(BundleContext ctxt) {
        this.ctxt = ctxt;
    }

    public static BufferedReader fromString(String s) {
        return new BufferedReader(new StringReader(s));
    }

    @Validate
    private void init() throws Exception {
        init(astralIncluded);
    }

    private boolean astralIncluded = true;

    private void init(boolean astral) throws Exception {
        astralIncluded = astral;
        consultId = 0;
        prova = new ProvaCommunicatorImpl("prova", "", ProvaCommunicatorImpl.SYNC);
        for (Map.Entry<String, Object> entry : constants.entrySet()) {
            prova.setGlobalConstant("$" + entry.getKey(), entry.getValue());
        }

        log.trace("Loading user rules");
        prova.consultSync(fromString(additionalRules.toString()), String.valueOf(consultId++), new Object[]{});
        if (!astral) return;
        //Logger.getLogger("prova").setLevel(Level.TRACE);

        Enumeration knowledge = ctxt.getBundle().findEntries("prolog/operators", "*.prova", true);
        while (knowledge.hasMoreElements()) {
            String path = ((URL) knowledge.nextElement()).getPath();
            InputStream is = KnowledgeBaseImpl.class.getResourceAsStream(path);
            log.trace("Loading " + path);
            prova.consultSync(new BufferedReader(new InputStreamReader(is)), String.valueOf(consultId++), new Object[]{});
        }
        for (String file : coreRules) {
            InputStream is = KnowledgeBaseImpl.class.getResourceAsStream("/prolog/" + file + ".prova");
            log.trace("Loading " + "/prolog/" + file + ".prova");
            prova.consultSync(new BufferedReader(new InputStreamReader(is)), String.valueOf(consultId++), new Object[]{});
        }
    }

    @Override
    public void putKnowledge(String statement) throws Exception {
        additionalRules.append(statement).append("\n");
        changed = true;
    }

    @Override
    public void putKnowledge(InputStream file) throws Exception {
        StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(file);
        int read;
        do {
            read = in.read(buffer, 0, buffer.length);
            if (read > 0) {
                out.append(buffer, 0, read);
            }
        } while (read >= 0);
        putKnowledge(out.toString());
    }

    @Override
    public Map<String, Object> consult(String query, Object... parameters) throws Exception {
        if (changed) init();
        log.debug("Querying prova: " + query);
        ByteArrayOutputStream plOut = new ByteArrayOutputStream();
        prova.setPrintWriter(new PrintWriter(plOut, true));
        try {

            List<ProvaSolution[]> solutions = prova.consultSync(fromString(query), String.valueOf(consultId++), parameters);
            ProvaSolution[] psArray = solutions.get(0);
            if (psArray.length > 0) {
                Map<String, Object> result = new HashMap<String, Object>();

                for (Map.Entry<String, Object> entry : psArray[0].getNv().entrySet()) {
                    if (entry.getValue() instanceof ProvaConstant)
                        result.put(entry.getKey(), ((ProvaConstant) entry.getValue()).getObject());
                    else
                        result.put(entry.getKey(), entry.getValue());
                }
                return result;
            }
        } catch (Exception e) {
            throw new RuntimeException(query + "\n" + plOut.toString(), e);
        } finally {
            messages = query + "\n\nDebug: " + plOut.toString();
            for (String s : messages.split("\n"))
                log.debug("[Prova] " + s);
        }
        return null;
    }

    @Override
    public void setGlobal(String name, Object value) {
        constants.put(name, value);
        changed = true;
    }

    @Override
    public KnowledgeBase createOtherKB(boolean astralKnowledgeIncluded) throws Exception {
        KnowledgeBaseImpl kb = new KnowledgeBaseImpl(ctxt);
        kb.init(astralKnowledgeIncluded);
        return kb;
    }

    @Override
    public String getLastMessages() {
        return messages;
    }
}
