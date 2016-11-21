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

import fr.lig.sigma.astral.common.*;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.common.structure.TupleSet;
import fr.lig.sigma.astral.common.structure.containers.StreamContainer;
import fr.lig.sigma.astral.common.AttributeSet;
import fr.lig.sigma.astral.query.QueryStatus;
import fr.lig.sigma.astral.source.Source;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Property;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Component
@Provides
public class FileStreamImpl extends StreamContainer implements Source, Runnable {
    private static Logger log = Logger.getLogger(FileStreamImpl.class);
    private BufferedReader reader;
    private int id = 0;
    private int timestampPosition = -1;

    @Property(mandatory = true)
    private String entityname;
    @Property(mandatory = true)
    private File file;
    @Property
    private String parseAttributes;
    @Property
    private String stampAttribute;
    @Property(value = "0")
    private int sleepFactor;
    @Property
    private String schema[];

    @Validate
    protected void ready() throws IOException, InstanceCreationException {
        InputStream in = new FileInputStream(file);
        reader = new BufferedReader(new InputStreamReader(in));
        String schemaString;
        if (parseAttributes != null)
            schemaString = parseAttributes;
        else
            schemaString = reader.readLine();
        schema = schemaString.split("\\s*,\\s*");
        for (int i = 0; i < this.schema.length; i++) {
            if (Tuple.TIMESTAMP_ATTRIBUTE.equals(this.schema[i]))
                timestampPosition = i;
        }
        if (stampAttribute != null) {
            schema = Arrays.copyOf(schema, schema.length + 1);
            schema[schema.length - 1] = stampAttribute;
        }
        if (timestampPosition < 0)
            throw new WrongFileException("No timestamp attribute (" + Tuple.TIMESTAMP_ATTRIBUTE + ")");
    }

    private FileStreamImpl() {
    }

    private TupleSet ts;

    private int batchId = 0;

    public int getMaxInputs() {
        return 0;
    }


    private Thread th;

    public void processEvent(Batch batch) throws AxiomNotVerifiedException {
        th = new Thread(this, "FileStream(" + file.getPath() + ")");
        th.start();
    }

    @Override
    public void firstSchedule() throws Exception {
        ts = entityFactory.instanciateTupleSet(getAttributes(), true);
        scheduler.pushEvent(Batch.MIN_VALUE, this);
    }

    @Override
    public void run() {
        Batch oldBatch = Batch.MIN_VALUE;
        String s;
        try {
            while ((s = reader.readLine()) != null) {
                if (s.trim().isEmpty()) break;
                if (s.trim().equals("--")) {
                    batchId++;
                    continue;
                }
                String[] newTuple = s.split("\\s*,\\s*");
                if (newTuple == null || newTuple.length <= 0) {
                    throw new IllegalStateException("Invalid file format");
                }
                long timestamp = Long.parseLong(newTuple[timestampPosition]);
                if (oldBatch != null && timestamp != oldBatch.getTimestamp())
                    batchId = 0;
                Batch b = new Batch(timestamp, batchId);
                if (oldBatch == Batch.MIN_VALUE)
                    oldBatch = b;

                if (!b.equals(oldBatch))
                    sendBatch(oldBatch);

                oldBatch = b;
                putTuple(newTuple);
            }
            sendBatch(oldBatch);
            scheduler.togglePush(oldBatch.getTimestamp());
        } catch (AxiomNotVerifiedException e) {
            log.warn("Stream not correctly ordered around " + oldBatch, e);
        } catch (IOException e) {
            log.warn("Can't read the file correctly", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            scheduler.togglePush(oldBatch.getTimestamp());
        }
    }

    private void putTuple(String[] newTuple) {
        Tuple t = new Tuple(id++);
        for (int i = 0; i < newTuple.length; i++) {
            if (getAttributes().contains(schema[i])) {
                Comparable value = null;

                try {
                    value = Long.parseLong(newTuple[i]);
                } catch (NumberFormatException ignored) {
                }
                if (value == null)
                    try {
                        value = Double.parseDouble(newTuple[i]);
                    } catch (NumberFormatException ignored) {
                    }
                if (value == null)
                    value = newTuple[i];
                t.put(schema[i], value);
            }
        }
        if (stampAttribute != null)
            t.put(stampAttribute, 0L);
        ts.add(t);
    }

    private void sendBatch(Batch oldBatch) throws InterruptedException, AxiomNotVerifiedException {
        if (sleepFactor > 0)
            Thread.sleep(sleepFactor);
        if (stampAttribute != null) {
            for (Tuple t : ts)
                t.put(stampAttribute, System.currentTimeMillis());
        }
        output.putAll(ts, oldBatch.getId());
        ts = entityFactory.instanciateTupleSet(getAttributes(), true);
    }
}
