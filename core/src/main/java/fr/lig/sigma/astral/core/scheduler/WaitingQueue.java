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

package fr.lig.sigma.astral.core.scheduler;

import fr.lig.sigma.astral.common.event.WaitingEntry;

/**
 * @author Loic Petit
 */
public interface WaitingQueue<K extends Comparable, V> {
    boolean isEmpty();

    K getHeadKey();

    void addToList(K to, V add);

    boolean containsKey(K key);

    WaitingEntry<K, V> popHead();

    WaitingEntry<K, V> peekHead();

    K maxKey();

    void removeEntry(WaitingEntry<K, V> entry);

    int getCount();
}
