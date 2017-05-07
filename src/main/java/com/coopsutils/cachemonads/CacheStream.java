/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopsutils.cachemonads;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class CacheStream<CACHE, VALUE> {

    private final Stream<CacheTuple<CACHE, VALUE>> innerStream;

    public static <V> CacheStream<V, V> of(final Collection<V> collection) {

        return new CacheStream<>(collection.stream());
    }

    public static <V> CacheStream<V, V> of(final Stream<V> stream) {

        return new CacheStream<>(stream);
    }

    public static <V> CacheStream<V, V> parrallelOf(final Collection<V> collection) {

        return new CacheStream<>(collection.parallelStream());
    }

    private CacheStream(
            final Stream<VALUE> valueCollection) {

        Function<VALUE, CacheTuple<CACHE, VALUE>> tupleMaker =
                v -> new CacheTuple<>(null, v);
        this.innerStream =  valueCollection
                .map(tupleMaker);
    }

    public void forEach(final Consumer<VALUE> consumer) {

        innerStream.forEach(pair -> consumer.accept(pair.getRight()));
    }

    //TODO: Filter
    //TODO: map
    //TODO: flatmap
    //TODO: distinct
    //TODO: sorted
    //TODO: sorted( with comparator )
    //TODO: peek
    //TODO: limit
    //TODO: skip
    //TODO: forEach
    //TODO: forEachOrdered
    //TODO: toArray (object[] and V[] returns)
    //TODO: reduce (x3)
    //TODO: min
    //TODO: max
    //TODO: count
    //TODO: anyMatch
    //TODO: allMatch
    //TODO: noneMatch
    //TODO: findFirst
    //TODO: findAny
    //TODO: iterate
    //TODO: generate
    //TODO: concat

    //TODO: Builder (with interface)

    //TODO: collect (x2)
    //TODO: mapToInt, Long, Double
    //TODO: flatMapToInt, Long, Double
}
