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

package fr.lig.sigma.astral.common.structure;

import fr.lig.sigma.astral.common.Batch;
import fr.lig.sigma.astral.common.Tuple;
import fr.lig.sigma.astral.common.AxiomNotVerifiedException;

/**  
 * Basic stream concept that will be shared among the framework.
 * It specifies the stream S defined in AStrAL
 * @author Loic Petit
 */
public interface Stream extends Entity {
    /**
     * Is there any tuple t such that B(t) <= b present in the stream
     * @param b The upper batch to compare
     * @return true iff there is a least one such tuple
     */
    boolean hasNext(Batch b);

    /**
     * Put one single tuple inside the stream
     * @param t The tuple
     * @param i the selected batch 
     * @throws fr.lig.sigma.astral.common.AxiomNotVerifiedException If an axiom is broken
     */
    void put(Tuple t, int i) throws AxiomNotVerifiedException;
    
    /**
     * Put a full TupleSet. If an axiom is broken an atomicity operation must ensure that no tuple were inserted
     * inside the stream.
     * @param ts The tuple set
     * @param i the selected batch 
     * @throws fr.lig.sigma.astral.common.AxiomNotVerifiedException If an axiom is broken
     */
    void putAll(TupleSet ts, int i) throws AxiomNotVerifiedException;

    /**
     * Pop and return the head tuple 
     * @return Head of the stream. Null if there is no tuple inside the stream
     */
    Tuple pop();

    /**
     * Return the head WITHOUT poping it
     * @return Head of the stream. Null if there is no tuple inside the stream
     */
    Tuple peek();
    

    /**
     * Tau : N -> TxN (def. 3.7) 
     * @param n Position
     * @return The corresponding batch id
     */
    Batch tau(long n);
    
    /**
     * Tau^{-1} : TxN -> N (based on axiom 3.1)
     * @param b the batch
     * @return Maximum position of all tuples that have this timestamp
     */
    long reversetau(Batch b);

    /**
     * B : S -> TxN
     * @param t The tuple
     * @return The corresponding batch
     */
    Batch B(Tuple t);

    void put(Tuple t) throws AxiomNotVerifiedException;

    void putAll(TupleSet ts) throws AxiomNotVerifiedException;
}
