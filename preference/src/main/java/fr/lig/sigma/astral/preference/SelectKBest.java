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

package fr.lig.sigma.astral.preference;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.operators.RelationOperator;
import fr.lig.sigma.astral.query.AstralCore;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class SelectKBest extends RelationOperator implements Operator {
    @Requires(id = "in")
    private Relation in;

    private PartialComparator<Tuple> comparator;
    @Property(mandatory = true)
    private List<Map<String, String>> preference;
    @Property(mandatory = true)
    private int k;
    @Requires(id = "core")
    private AstralCore core;
    private static final Logger log = Logger.getLogger(SelectKBest.class);

    @Property
    private String statsFile;

    public int getMaxInputs() {
        return 1;
    }

    private PrintStream out;

    public void prepare() throws Exception {
        if (statsFile != null)
            out = new PrintStream(new FileOutputStream(statsFile));
        // Build the comparator based on the preference set in the xml
        comparator = PreferenceBuilder.buildProfile(preference, core);
        // initialize the preference orders
        comparator.setContent(in.getAttributes());

        // Prepare the operator
        setOutput(createNewFrom(in, in.getAttributes(), "pref/" + in));
        addInput(in, true);
    }

    @Invalidate
    private void destroy() {
        if (out != null)
            out.close();
    }

    @Override
    public void processEvent(Batch b) throws AxiomNotVerifiedException {
        if (k == 0) return; // Do absolutely nothing as the first state of the relation is {}, it will stay {}

        TupleSet result = entityFactory.instanciateTupleSet(getAttributes());
        DominationGraph graph = new DominationGraph(comparator, k < 0);
        long time = System.currentTimeMillis();
        TupleSet content = in.getContent(b);
        graph.buildFrom(content);
        long tBuildGraph = System.currentTimeMillis() - time;

        time = System.currentTimeMillis();
        if (k < 0)
            graph.fillSources(result);
        else
            graph.fillTopK(result, k);
        update(result, b);
        time = System.currentTimeMillis() - time;
        if (out != null)
            out.println(b.getTimestamp() + "\t" + tBuildGraph + "\t" + time + "\t" + graph.getNodeCount() + "\t" +
                    graph.getSourceCount() + "\t" +
                    graph.getIsolatedNodeCount() + "\t" +
                    graph.getEdgeCount());
    }
}
