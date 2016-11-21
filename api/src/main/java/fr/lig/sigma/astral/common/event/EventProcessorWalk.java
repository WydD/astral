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

package fr.lig.sigma.astral.common.event;

import fr.lig.sigma.astral.operators.Operator;
import fr.lig.sigma.astral.source.Source;
import org.apache.log4j.Logger;

/**
 *
 */
public class EventProcessorWalk {
    private static Logger log = Logger.getLogger(EventProcessorWalk.class);

    public static void walk(EventProcessorVisitor visitor, EventProcessor e, Object give) {
        Object res = visitor.visit(e, give);
        if (e instanceof Source) return;
        try {
            for (EventProcessor child : (e instanceof Operator ? ((Operator) e).getInputs() : e.waitFor()))
                walk(visitor, child, res);
        } catch (Throwable t) {
            log.warn("Can't walk on my query", t);
        }
    }
}
