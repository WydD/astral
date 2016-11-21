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

package fr.lig.sigma.astral.core.source;

import fr.lig.sigma.astral.common.AxiomNotVerifiedException;
import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.containers.RelationContainer;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.source.Source;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;

import java.io.*;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class FileRelationImpl extends RelationContainer implements Source, Runnable {
    private BufferedReader reader;
    private String nextT;

    private int id = 0;
    @Property(mandatory = true)
    private String entityname;
    @Property(mandatory = true)
    private File file;
    @Property
    private String parseAttributes;
    @Property
    private String[] schema;
    @Property(value = "0")
    private int sleepFactor;

    @Validate
    protected void ready() throws IOException {
        InputStream in = new FileInputStream(file);
        reader = new BufferedReader(new InputStreamReader(in));
        String schemaString;
        if (parseAttributes != null)
            schemaString = parseAttributes;
        else
            schemaString = reader.readLine();
        schema = schemaString.split("\\s*,\\s*");

        nextT = reader.readLine();
        if (!nextT.startsWith("t="))
            throw new WrongFileException("Missing t= statement");
    }

    private FileRelationImpl() {
    }

    private long oldTimestamp = Long.MIN_VALUE;
    private int batch = 0;

    private void scheduleNextTuple() throws IOException {
        long timestamp = Long.parseLong(nextT.substring(2).trim());
        if (timestamp == oldTimestamp)
            batch++;
        else
            batch = 0;
        //scheduler.pushEvent(new Batch(timestamp, batch), this);
        oldTimestamp = timestamp;
    }

    public int getMaxInputs() {
        return 0;
    }

    private Thread th;

    public void processEvent(Batch batch) throws AxiomNotVerifiedException {
        th = new Thread(this, "FileRelation(" + file.getPath() + ")");
        th.start();
    }

    public boolean sendBatch(Batch batch) throws AxiomNotVerifiedException {
        Set<String> attributes = getAttributes();
        TupleSet ts = entityFactory.instanciateTupleSet(attributes);

        try {
            nextT = reader.readLine();
            while (nextT != null && !nextT.trim().isEmpty() && !nextT.startsWith("t=")) {
                Tuple t = new Tuple(id++);
                String[] newTuple = nextT.split("\\s*,\\s*");
                for (int i = 0; i < schema.length; i++) {
                    if (attributes.contains(schema[i])) {
                        Comparable value = null;

                        String s = i < newTuple.length ? newTuple[i] : "";
                        try {
                            value = Long.parseLong(s);
                        } catch (NumberFormatException ignored) {
                        }
                        if (value == null)
                            try {
                                value = Double.parseDouble(s);
                            } catch (NumberFormatException ignored) {
                            }
                        if (value == null)
                            value = s;
                        t.put(schema[i], value);
                    }
                }
                ts.add(t);
                nextT = reader.readLine();
            }
            update(ts, batch);

            return nextT != null && !nextT.trim().isEmpty();
        } catch (IOException e) {
            scheduler.togglePush(batch.getTimestamp());
            return false;
        }
    }

    @Override
    public void firstSchedule() throws Exception {
        scheduler.pushEvent(Batch.MIN_VALUE, this);
    }


    @Override
    public void run() {
        try {
            do {
                scheduleNextTuple();
                if (sleepFactor > 0)
                    Thread.sleep(sleepFactor);
            } while (sendBatch(new Batch(oldTimestamp, batch)));
            if (sleepFactor > 0)
                Thread.sleep(sleepFactor);
            scheduler.togglePush(oldTimestamp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
