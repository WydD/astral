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

import fr.lig.sigma.astral.query.KnowledgeBase;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.apache.log4j.Logger;

/**
 * @author Loic Petit
 */
@Component
@Instantiate
public class Activator implements Runnable {
    @Requires
    private KnowledgeBase kb;
    private final static Logger log = Logger.getLogger(Activator.class);

    @Validate
    public void run() {
        try {
            kb.putKnowledge(Activator.class.getResourceAsStream("/knowledge/SelectBest.prova"));
        } catch (Exception e) {
            log.error("Impossible to put the knowledge inside prova", e);
        }
    }
}
