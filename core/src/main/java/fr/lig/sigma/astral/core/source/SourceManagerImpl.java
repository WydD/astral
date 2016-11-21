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

import fr.lig.sigma.astral.common.structure.Entity;
import fr.lig.sigma.astral.common.structure.Relation;
import fr.lig.sigma.astral.common.structure.Stream;
import fr.lig.sigma.astral.core.operators.misc.DuplicateRelation;
import fr.lig.sigma.astral.core.operators.misc.DuplicateStream;
import fr.lig.sigma.astral.operators.misc.Duplicate;
import fr.lig.sigma.astral.query.AstralCore;
import fr.lig.sigma.astral.query.QueryRuntime;
import fr.lig.sigma.astral.source.Source;
import fr.lig.sigma.astral.source.SourceAlreadyExistsException;
import fr.lig.sigma.astral.source.SourceManager;
import fr.lig.sigma.astral.source.UnknownSourceException;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.log4j.Logger;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * @author Loic Petit
 */
@Instantiate
@Component
@Provides
public class SourceManagerImpl implements SourceManager {
    private HashMap<String, Source> sources = new HashMap<String, Source>();
    private HashMap<String, QueryRuntime> runtimes = new HashMap<String, QueryRuntime>();
    private HashMap<String, Duplicate> duplicates = new HashMap<String, Duplicate>();
    private Logger log = Logger.getLogger(SourceManagerImpl.class);

    private SourceManagerImpl() {
    }

    public void registerSource(Source s, QueryRuntime runtime) throws SourceAlreadyExistsException {
        String name = s.getName();
        if (sources.get(name) != null) throw new SourceAlreadyExistsException(name);
        log.debug("Registering new source " + name);
        sources.put(name, s);
        runtimes.put(name, runtime);
        Duplicate dup;
        if (s instanceof Stream)
            dup = new DuplicateStream((Stream) s, runtime.getCore());
        else
            dup = new DuplicateRelation((Relation) s, runtime.getCore());
        duplicates.put(name, dup);
    }

    public Source getSource(String name) throws UnknownSourceException {
        Source s = sources.get(name);
        if (s == null)
            throw new UnknownSourceException(name);
        return s;
    }

    @Override
    public void startSource(String name) {
        log.debug("Starting the source " + name);
        runtimes.get(name).start();
    }

    @Override
    public boolean isRegistered(String name) {
        return runtimes.containsKey(name);
    }

    public Entity newDuplicatedEntity(String name, AstralCore core, Dictionary<String, Object> properties) throws UnknownSourceException {
        Duplicate dup = duplicates.get(name);
        if (dup == null)
            return getSource(name);
        if (properties == null)
            properties = new Hashtable<String, Object>();
        return dup.addDuplicate(core, properties);
    }

    @Override
    public Set<String> getRegisteredSources() {
        return sources.keySet();
    }

    public String toString() {
        String s = "";
        for (Map.Entry<String, Source> entry : sources.entrySet()) {
            s += entry.getKey() + ": " + entry.getValue() + "\n";
        }
        return s;
    }

    public void clear() {
        for (QueryRuntime run : runtimes.values())
            run.stop();
    }

    @Override
    public void sourceDown(String name) {
        log.debug("Source is down " + name);
        Duplicate dup = duplicates.remove(name);
        runtimes.remove(name);
        sources.remove(name);
        if (dup != null) dup.sourceDown();
    }
}
