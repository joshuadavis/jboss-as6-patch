/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logmanager;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

final class CopyOnWriteWeakMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

    private final Queue<K, V> queue = new Queue<K, V>();

    private static final FastCopyHashMap EMPTY = new FastCopyHashMap(32, 0.25f);

    @SuppressWarnings({ "unchecked" })
    private FastCopyHashMap<K, Node<K, V>> empty() { return (FastCopyHashMap<K, Node<K, V>>) EMPTY; }

    private volatile FastCopyHashMap<K, Node<K, V>> map = empty();

    private FastCopyHashMap<K, Node<K, V>> cleanCopyForRemove() {
        assert Thread.holdsLock(this);
        final Map<K, Node<K, V>> oldMap = map;
        final Queue<K, V> queue = this.queue;
        if (oldMap.isEmpty()) {
            queue.clear();
            return empty();
        }
        final FastCopyHashMap<K, Node<K, V>> newMap = new FastCopyHashMap<K, Node<K, V>>(oldMap);
        Node<K, V> ref;
        while ((ref = queue.poll()) != null) {
            final Object key = ref.getKey();
            if (newMap.get(key) == ref) {
                newMap.remove(key);
                if (newMap.isEmpty()) {
                    queue.clear();
                    return empty();
                }
            }
        }
        return newMap;
    }

    private FastCopyHashMap<K, Node<K, V>> cleanCopyForMod() {
        assert Thread.holdsLock(this);
        final Map<K, Node<K, V>> oldMap = map;
        final Queue<K, V> queue = this.queue;
        if (oldMap.isEmpty()) {
            queue.clear();
            return empty().clone();
        }
        final FastCopyHashMap<K, Node<K, V>> newMap = new FastCopyHashMap<K, Node<K, V>>(oldMap);
        Node<K, V> ref;
        while ((ref = queue.poll()) != null) {
            final Object key = ref.getKey();
            if (newMap.get(key) == ref) {
                newMap.remove(key);
                if (newMap.isEmpty()) {
                    queue.clear();
                    return empty().clone();
                }
            }
        }
        return newMap;
    }

    public V putIfAbsent(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        synchronized (this) {
            final Node<K, V> oldNode = map.get(key);
            if (oldNode != null) {
                final V val = oldNode.get();
                if (val != null) return val;
            }
            final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForMod();
            newMap.put(key, new Node<K,V>(key, value, queue));
            map = newMap;
            return null;
        }
    }

    public boolean remove(final Object key, final Object value) {
        synchronized (this) {
            final Node<K, V> oldNode = map.get(key);
            final V existing = oldNode.get();
            if (existing != null && existing.equals(value)) {
                final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForRemove();
                newMap.remove(key);
                map = newMap;
                return true;
            }
        }
        return false;
    }

    public boolean replace(final K key, final V oldValue, final V newValue) {
        if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }
        if (oldValue == null) {
            return false;
        }
        synchronized (this) {
            final Node<K, V> oldNode = map.get(key);
            final V existing = oldNode.get();
            if (existing != null && existing.equals(oldValue)) {
                final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForMod();
                map.put(key, new Node<K, V>(key, newValue, queue));
                map = newMap;
                return true;
            }
        }
        return false;
    }

    public V replace(final K key, final V value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        synchronized (this) {
            final Node<K, V> oldNode = map.get(key);
            final V existing = oldNode.get();
            if (existing != null) {
                final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForMod();
                map.put(key, new Node<K, V>(key, value, queue));
                map = newMap;
            }
            return existing;
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(final Object key) {
        return map.containsKey(key);
    }

    public boolean containsValue(final Object value) {
        if (value == null) return false;
        for (Node<K, V> node : map.values()) {
            if (value.equals(node.get())) {
                return true;
            }
        }
        return false;
    }

    public V get(final Object key) {
        final Node<K, V> node = map.get(key);
        return node == null ? null : node.get();
    }

    public V put(final K key, final V value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value is null");
        }
        synchronized (this) {
            final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForMod();
            final Node<K, V> old = newMap.put(key, new Node<K, V>(key, value, queue));
            map = newMap;
            return old == null ? null : old.get();
        }
    }

    public V remove(final Object key) {
        if (key == null) return null;
        synchronized (this) {
            final FastCopyHashMap<K, Node<K, V>> newMap = cleanCopyForRemove();
            final Node<K, V> old = newMap.remove(key);
            map = newMap;
            return old == null ? null : old.get();
        }
    }

    public void clear() {
        synchronized (this) {
            map = empty();
        }
    }

    public Set<Entry<K, V>> entrySet() {
        final FastCopyHashMap<K, Node<K, V>> snapshot = map;
        final Map<K, V> copyMap = new HashMap<K, V>();
        for (Node<K, V> node : snapshot.values()) {
            final V value = node.get();
            if (value == null) continue;
            final K key = node.getKey();
            copyMap.put(key, value);
        }
        return Collections.unmodifiableMap(copyMap).entrySet();
    }

    private static final class Node<K, V> extends WeakReference<V> {
        private final K key;

        Node(final K key, final V value, final ReferenceQueue<? super V> queue) {
            super(value, queue);
            this.key = key;
        }

        K getKey() {
            return key;
        }
    }

    private static final class Queue<K, V> extends ReferenceQueue<V> {

        public Node<K, V> poll() {
            return (Node<K, V>) super.poll();
        }

        void clear() {
            while (poll() != null);
        }
    }
}
