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

package fr.lig.sigma.astral.interpreter.common;

import org.w3c.dom.Element;

/**
 *
 */
public class ExtendedElement<E> {
    private E e;
    private Element elt;

    public ExtendedElement(E e, Element elt) {
        this.e = e;
        this.elt = elt;
    }

    public Element getElt() {
        return elt;
    }

    public void setElt(Element elt) {
        this.elt = elt;
    }

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }
}
