package com.jojos.challenge.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * utility class
 *
 * Created by karanikasg@gmail.com.
 */
public class Util {

    /**
     * For a map containing a set for the specified {@code mapKey}, adds the provided value to the set.
     * If there is no set for the {@code mapKey}, one will be created first. As we are using a {@link ConcurrentMap},
     * we will assume that we want a concurrent set as well, so one will be created by way of {@link ConcurrentHashMap}
     * and {@link java.util.Collections#newSetFromMap(java.util.Map)}
     *
     * @param map    the map of sets
     * @param mapKey the key that indicates a set in the map
     * @param value  the value to be added to the set
     * @return {@code true} if the value could be added to the set or {@code false} otherwise.
     * @see java.util.Set#add(Object)
     */
    public static <K, V> boolean addToContainedSet(ConcurrentMap<K, Set<V>> map, K mapKey, V value) {
        Set<V> existingSet = map.get(mapKey);
        if (existingSet == null) {
            // It might look like checking for null and then creating something means that we need a lock.
            // this isn't the case, as the ACTUAL point of synchronization is the map.putIfAbsent() below.
            // it's perfectly possible to have multiple threads enter this block at the same time.
            // this is fine, as the only "true" value added is added by the putIfAbsent() call.
            // this race will only be an issue in the beginning. Once putIfAbsent() has succeeded,
            // the outer if-statement will always be false, which means we can avoid creating the
            // inner container and calling putIfAbsent() again.
            // This replaces this more legible but slower pattern:
            // map.putIfAbsent(mapKey, Collections.newSetFromMap(new ConcurrentHashMap<V, Boolean>())); // ensure that we have something
            // map.get(mapKey).add(value);
            // See slides 54 and 55 of this presentation regarding the speed of this: http://www.slideshare.net/marakana/effective-java-still-effective-after-all-these-years
            Set<V> newSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            existingSet = map.putIfAbsent(mapKey, newSet);
            if (existingSet == null) {
                // we've added a new set
                existingSet = newSet;
            }
        }
        return existingSet.add(value);
    }

    public static <K, V> boolean removeFromContainedSet(ConcurrentMap<K, Set<V>> map, K mapKey, V value) {
        Set<V> existingSet = map.get(mapKey);
        if (existingSet == null) {
            // It might look like checking for null and then creating something means that we need a lock.
            // this isn't the case, as the ACTUAL point of synchronization is the map.putIfAbsent() below.
            // it's perfectly possible to have multiple threads enter this block at the same time.
            // this is fine, as the only "true" value added is added by the putIfAbsent() call.
            // this race will only be an issue in the beginning. Once putIfAbsent() has succeeded,
            // the outer if-statement will always be false, which means we can avoid creating the
            // inner container and calling putIfAbsent() again.
            // This replaces this more legible but slower pattern:
            // map.putIfAbsent(mapKey, Collections.newSetFromMap(new ConcurrentHashMap<V, Boolean>())); // ensure that we have something
            // map.get(mapKey).add(value);
            // See slides 54 and 55 of this presentation regarding the speed of this: http://www.slideshare.net/marakana/effective-java-still-effective-after-all-these-years
            Set<V> newSet = Collections.newSetFromMap(new ConcurrentHashMap<>());
            existingSet = map.putIfAbsent(mapKey, newSet);
            if (existingSet == null) {
                // we've added a new set
                existingSet = newSet;
            }
        }
        return existingSet.remove(value);
    }
}
