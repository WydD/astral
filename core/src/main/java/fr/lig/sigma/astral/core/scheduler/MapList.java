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

import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.TreeMap;

/**
 * @author Loic Petit
 */
public class MapList<K extends Comparable, V> implements WaitingQueue<K, V> {
    private Comparator<? super V> comparator;
    private TreeMap<K, PriorityQueue<V>> content = new TreeMap<K, PriorityQueue<V>>();

    public MapList(Comparator<? super V> comparator) {
        this.comparator = comparator;
    }

    @Override
    public synchronized boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public synchronized K getHeadKey() {
        return content.firstKey();
    }

    @Override
    public synchronized void addToList(K to, V add) {
        PriorityQueue<V> list = content.get(to);
        if (list == null) {
            list = new PriorityQueue<V>(2, comparator);
            content.put(to, list);
        } else if (list.contains(add)) return;
        list.add(add);
    }

    @Override
    public synchronized boolean containsKey(K key) {
        return content.containsKey(key);
    }

    @Override
    public synchronized WaitingEntry<K, V> popHead() {
        if (content.isEmpty()) return null;
        Map.Entry<K, PriorityQueue<V>> contentEntry = content.firstEntry();
        Queue<V> listOfEntry = contentEntry.getValue();
        V min = listOfEntry.poll();
        if (listOfEntry.isEmpty())
            content.remove(contentEntry.getKey());
        return new WaitingEntry<K, V>(contentEntry.getKey(), min);
    }

    @Override
    public synchronized WaitingEntry<K, V> peekHead() {
        if (content.isEmpty()) return null;
        Map.Entry<K, PriorityQueue<V>> contentEntry = content.firstEntry();
        Queue<V> listOfEntry = contentEntry.getValue();
        return new WaitingEntry<K, V>(contentEntry.getKey(), listOfEntry.peek());
    }

    @Override
    public synchronized String toString() {
        String s = "";
        for (Map.Entry<K, PriorityQueue<V>> entry : content.entrySet()) {
            s += "@" + entry.getKey() + ": {";
            for (V v : entry.getValue())
                s += v + ", ";
            s = s.substring(0, s.length() - 2) + "} ; ";
        }
        if (!s.isEmpty())
            s = s.substring(0, s.length() - 3);
        return s;
    }

    @Override
    public synchronized K maxKey() {
        if (content.isEmpty()) return null;
        return content.lastKey();
    }

    @Override
    public synchronized void removeEntry(WaitingEntry<K, V> entry) {
        PriorityQueue<V> listOfEntry = content.get(entry.getKey());
        if (listOfEntry != null) {
            listOfEntry.remove(entry.getValue());
            if (listOfEntry.isEmpty())
                content.remove(entry.getKey());
        }
    }

    @Override
    public synchronized int getCount() {
        int c = 0;
        for (PriorityQueue<V> queues : content.values()) {
            c += queues.size();
        }
        return c;
    }
}
