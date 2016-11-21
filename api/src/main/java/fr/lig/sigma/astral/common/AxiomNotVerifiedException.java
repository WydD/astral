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

package fr.lig.sigma.astral.common;

/**
 * Fired when an axiom has been broken during the execution. It can indicates mainly a malfunction of the sources that
 * do not deliver the tuples correctly. If the sources are good, then it is a major internal error, thanks to report the
 * bug to the developers.
 *  
 * @author Loic Petit
 */
public class AxiomNotVerifiedException extends Exception {
    public static final String CONSISTENCY_AXIOM = "Axiom 3.1: Consistency between positional and temporal order";
    public static final String WINDOW_DESCRIPTION_HYPOTHESIS = "Window Description Hypothesis"; 
    public AxiomNotVerifiedException(String axiom, String message) {
        super(axiom+"\n"+message);
    }
}
