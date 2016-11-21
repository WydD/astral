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

package fr.lig.sigma.astral.operators;

import fr.lig.sigma.astral.common.InstanceCreationException;
import fr.lig.sigma.astral.query.AstralCore;

import java.util.Properties;

/**
 * Factory in order to create operators
 * @author Loic Petit
 */
public interface OperatorFactory {
    /**
     * Create an operator
     * @param name Operator Name (based on its class name)
     * @param prop The properties given to the creation module
     * @return The created operator
     * @throws InstanceCreationException If anything happens
     */
    Operator instanciateSpecificOperator(String name, Properties prop) throws InstanceCreationException;

    UnaryOperationFactory getUnaryFactory();

    void setCore(AstralCore core);
}
