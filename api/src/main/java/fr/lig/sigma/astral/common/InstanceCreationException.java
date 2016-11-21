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
 * Fired when the framework cannot create a instance of an object via iPojo's framework. It happens when the user try to
 * create an operator/source with the wrong name or specification.
 * 
 * @author Loic Petit
 */
public class InstanceCreationException extends Exception {
    public InstanceCreationException(String s) {
        super(s);
    }
    public InstanceCreationException(String s, Throwable throwable) {
        super(s, throwable);
    }
    public InstanceCreationException(Throwable throwable) {
        super(throwable);
    }
}
