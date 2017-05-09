/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class CacheStream<CACHE, VALUE> {

    private final Stream<CacheTuple<CACHE, VALUE>> innerStream;

    public static <V> CacheStream<V, V> of(final Collection<V> collection) {

        return new CacheStream<>(collection.stream().map(CacheStream::makeTuple));
    }

    public static <V> CacheStream<V, V> of(final Stream<V> stream) {

        return new CacheStream<>(stream.map(CacheStream::makeTuple));
    }

    public static <V> CacheStream<V, V> parrallelOf(final Collection<V> collection) {

        return new CacheStream<>(collection.parallelStream().map(CacheStream::makeTuple));
    }

    private static <C, V> CacheTuple<C, V> makeTuple(final V value) {
        return new CacheTuple<>((C) null, value);
    }

    private CacheStream(Stream<CacheTuple<CACHE, VALUE>> innerStream) {
        this.innerStream = innerStream;
    }

    public void forEach(final Consumer<VALUE> consumer) {

        innerStream.forEach(pair -> consumer.accept(pair.getRight()));
    }

    public void forEachOrdered(final Consumer<VALUE> consumer) {

        innerStream.forEachOrdered(pair -> consumer.accept(pair.getRight()));
    }

    public CacheStream<CACHE, VALUE> filter(final Predicate<VALUE> predicate) {

        Stream<CacheTuple<CACHE, VALUE>> filteredStream =
                innerStream.filter(pair -> predicate.test(pair.getRight()));
        return new CacheStream<>(filteredStream);
    }

    public <R> CacheStream<CACHE, R> map(final Function<VALUE, R> function) {

        Stream<CacheTuple<CACHE, R>> mappedStream =
                innerStream.map(pair ->
                        new CacheTuple<>(pair.getLeft(), function.apply(pair.getRight())));
        return new CacheStream<>(mappedStream);
    }

    public <R> CacheStream<CACHE, R> flatMap(final Function<VALUE, CacheStream<CACHE, R>> function) {

        Stream<CacheTuple<CACHE, R>> mappedStream =
                innerStream.flatMap(pair ->
                    function.apply(pair.getRight()).innerStream);
        return new CacheStream<>(mappedStream);
    }

    public CacheStream<CACHE, VALUE> distinct() {

        Stream<CacheTuple<CACHE, VALUE>> distinctStream =
                innerStream.distinct();
        return new CacheStream<>(distinctStream);
    }

    public CacheStream<CACHE, VALUE> sorted() {

        Stream<CacheTuple<CACHE, VALUE>> sortedStream =
                innerStream.sorted();
        return new CacheStream<>(sortedStream);
    }

    public CacheStream<CACHE, VALUE> sorted(final Comparator<VALUE> comparator) {

        Stream<CacheTuple<CACHE, VALUE>> sortedStream =
                innerStream.sorted((t1, t2) -> comparator.compare(t1.getRight(), t2.getRight()));
        return new CacheStream<>(sortedStream);
    }

    public CacheStream<CACHE, VALUE> peek(final Consumer<VALUE> consumer) {

        Stream<CacheTuple<CACHE, VALUE>> peekedStrem =
                innerStream.peek(pair -> consumer.accept(pair.getRight()));
        return new CacheStream<>(peekedStrem);
    }

    public CacheStream<CACHE, VALUE> limit(final long limit) {

        Stream<CacheTuple<CACHE, VALUE>> limitedStream =
                innerStream.limit(limit);
        return new CacheStream<>(limitedStream);
    }

    public CacheStream<CACHE, VALUE> skip(final long skip) {

        Stream<CacheTuple<CACHE, VALUE>> limitedStream =
                innerStream.skip(skip);
        return new CacheStream<>(limitedStream);
    }

    public long count() {

        return innerStream.count();
    }

    public Object[] toArray() {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.toArray();
    }

    public VALUE[] toArray(final IntFunction<VALUE[]> generator) {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.toArray(generator);
    }

    //TODO: reduce (x3)
    //TODO: min
    //TODO: max
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

    //TODO: FilterNull()
    //TODO: empty()
    //TODO: FlatMap of java util stream
}
