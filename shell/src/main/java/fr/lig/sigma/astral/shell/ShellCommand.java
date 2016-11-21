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

package fr.lig.sigma.astral.shell;

import fr.lig.sigma.astral.AstralEngine;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.common.WrongAttributeException;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.interpreter.Interpreter;
import fr.lig.sigma.astral.interpreter.InterpreterException;
import fr.lig.sigma.astral.interpreter.ParsingException;
import fr.lig.sigma.astral.interpreter.common.XMLUtils;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryChangedListener;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.query.QueryStatus;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.ServiceProperty;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.felix.service.command.Descriptor;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.channels.IllegalBlockingModeException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Loic Petit
 */
@Component(public_factory = false, immediate = true)
@Instantiate
@Provides(specifications = ShellCommand.class)
public class ShellCommand implements QueryChangedListener {
    private PrintStream out;

    @ServiceProperty(name = "osgi.command.scope", value = "astral")
    private String score;

    @ServiceProperty(name = "osgi.command.function", value = "{}")
    String[] functions = new String[]{
            "register",
            "ls",
            "set",
            "prepare",
            "parse",
            "join",
            "hold",
            "release",
            "remove",
            "scheduler",
            "queries",
            "reset",
            "stop",
            "kb",
            "launch"
    };

    @Requires
    private AstralEngine engine;
    @Requires(filter = "(instance.name=*XML*)")
    private Interpreter inter;
    // @Requires(optional = true)
    // private NetworkFactory nf;

    private PrintStream output;

    //private AstralCore core;
    private QueryRuntime runtime;
    private int port = 0;

    private static String getQuery(String file) throws IOException {
        InputStream is = new FileInputStream(file);
        Writer query = new StringWriter();

        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(
                    new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                query.write(buffer, 0, n);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            is.close();
        }
        return query.toString();
    }


    @Validate
    public void ready() throws IOException {
        //core = engine.createCore();
        engine.addQueryChangedListener(this);
        //noinspection ResultOfMethodCallIgnored
        new File("lock").createNewFile();
        out = System.out;
        output = out;
    }

    @Descriptor("Hold the execution of all queries")
    public void hold(@Descriptor("a query name") String name) throws InterruptedException {
        QueryRuntime runtime = engine.getDeclaredQuery(name);
        runtime.hold();
    }

    @Descriptor("Release the execution of all queries")
    public void release(@Descriptor("a query name") String name) {
        QueryRuntime runtime = engine.getDeclaredQuery(name);
        runtime.release();
    }

    @Descriptor("Wait for all running queries")
    public void join() {
        try {
            List<QueryRuntime> list = new LinkedList<QueryRuntime>(engine.getDeclaredQueries());
            for (QueryRuntime runtime : list) {
                runtime.join();
            }
        } catch (Exception e) {
            e.printStackTrace(out);
        }
    }

    @Descriptor("Launch the prepared query")
    public void launch(@Descriptor("an algebraic query") String[] s) throws IOException, WrongAttributeException, InterpreterException, InstanceCreationException, ParsingException, AxiomNotVerifiedException {
        if (s.length > 0)
            prepare(s);
        runtime.start();
        //current = new Thread(this);
        //current.start();
        //core = engine.createCore();
        runtime = null;
    }

    @Descriptor("Parse a query, optimize it and render the final XML")
    public void parse(@Descriptor("an algebraic query") String s) throws ParsingException, InterpreterException {
        /*Document doc = inter.parse(s);
        inter.inferSource(doc, core);
        inter.optimize(doc, engine);
        out.println(XMLUtils.xmlToString(doc));*/
    }

    @Descriptor("Reset the current core")
    public void reset() {
        //core.reset();
    }

    @Descriptor("List of every queries that are declared inside the core")
    public void queries() {
        for (QueryRuntime qr : engine.getDeclaredQueries()) {
            out.println(qr.getDisplayName() +
                    "\n\tStatus: " + qr.getStatus() + " / " + (qr.isComputing() ? "COMPUTING" : "IDLE") +
                    "\n\tTime: " + qr.getCore().getEs().getGlobalTime() +
                    "\n\tOut: " + qr.getOut().getName() +
                    "\n\tHandlers: " + qr.getHandlers() + "\n");
        }
    }

    @Descriptor("List of every queries that are declared inside the core")
    public void scheduler(@Descriptor("the query identifier") String s) {
        try {
            QueryRuntime qr = engine.getDeclaredQuery(s);
            if (qr == null) {
                out.println("Unknown query " + s);
                return;
            }
            out.println(qr.getCore().getEs());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Descriptor("Build a new query but no registration/launch is done")
    public void prepare(@Descriptor("an algebraic query") String[] s) throws ParsingException, InterpreterException, AxiomNotVerifiedException, IOException, InstanceCreationException, WrongAttributeException {
        long timestamp = System.currentTimeMillis();
        String query = s[0];
        for (int i = 1; i < s.length; i++)
            query += ' ' + s[i];

        String queryContent = getQuery(query);
        try {
            runtime = inter.query(queryContent, engine);
            if (runtime == null) return;
            if (runtime.getHandlers().size() > 0) return;
            //if (port > 0) {
            //nf.createExportServer(runtime.getOut(), port);
            //} else {
            String handlerName = "Print" + (runtime.getOut() instanceof Stream ? "Stream" : "Relation") + "Handler";
            Properties p = new Properties();
            p.put("out", output);
            runtime.getCore().getHf().createAndAttachHandler(handlerName, p, runtime);
            //}
            timestamp = System.currentTimeMillis() - timestamp;
            out.println("Query prepared in " + timestamp + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Descriptor("Set a configuration parameter")
    public void set(String config, String value, String[] args) throws FileNotFoundException {
        //if ("simulatedTime".equals(config))
        //    core.getEs().setSimulatedTime(Boolean.parseBoolean(value));
        //else
        if ("t0".equals(config)) {
            //core.getEs().setT0(Long.parseLong(value));
        } else if ("output".equals(config)) {
            port = -1;
            if ("default".equals(value))
                output = out;
            else if (value.startsWith("network")) {
                port = Integer.parseInt(args[4]);
                output.close();
            } else
                output = new PrintStream(new FileOutputStream(value));
        }
    }

    @Descriptor("List all the registered sources")
    public void ls() {
        out.println(engine.getGlobalSm().getRegisteredSources());
    }

    @Descriptor("Register a new source")
    public void register(@Descriptor("name of the source") String name, @Descriptor("type of the source") String type) throws ParsingException, InterpreterException, AxiomNotVerifiedException {
        out.print(type);
        inter.query("declare {\n" + name + ": " + type + ";\n}\n", engine);
    }

    @Override
    public void queryChanged(QueryRuntime runtime, QueryStatus status) {
        //if (status != QueryStatus.INITIALIZED && status != QueryStatus.RUNNING)
        out.println(System.currentTimeMillis() + " Query status changed with code " + runtime.getStatus().name() + ": " + runtime.getOut().getName());
    }

    @Descriptor("Stop a query")
    public void stop(@Descriptor("query name") String query) {
        out.println("Not yet implemented");
    }

    @Descriptor("Queries the knowledge base of the engine")
    public void kb(@Descriptor("the predicate to solve") String query) {
        Map<String, Object> consult = null;
        try {
            consult = engine.getKnowledgeBase().consult(":- solve(" + query + ").");
            if (consult == null) {
                out.println("No solutions found");
            } else {
                for (Map.Entry<String, Object> entry : consult.entrySet()) {
                    out.println(entry.getKey() + ":\t" + entry.getValue());
                }
            }
        } catch (Exception e) {
            out.println("Messages: \n" + engine.getKnowledgeBase().getLastMessages());
            e.printStackTrace(out);
        }
    }
}

