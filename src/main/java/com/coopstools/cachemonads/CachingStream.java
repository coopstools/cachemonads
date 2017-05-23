/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class CachingStream<VALUE> {

    private final Stream<VALUE> innerStream;

    public static <V> CachingStream<V> of(final Collection<V> collection) {
        return new CachingStream<>(collection.stream());
    }

    public static <V> CachingStream<V> parallelOf(final Collection<V> collection) {
        return new CachingStream<>(collection.parallelStream());
    }

    public static <V> CachingStream<V> of(final Stream<V> stream) {
        return new CachingStream<>(stream);
    }

    @SafeVarargs
    @SuppressWarnings("varargs") // Creating a stream from an array is safe
    public static <V> CachingStream<V> of (final V... values) {

        Stream<V> innerStream = Stream.of(values);
        return new CachingStream<>(innerStream);
    }

    private CachingStream(final Stream<VALUE> innerStream) {

        this.innerStream = innerStream;
    }

    public CachedStream<VALUE, VALUE> cache() {

        Stream<CacheTuple<VALUE, VALUE>> tupleStream = innerStream
                .map(v -> new CacheTuple<>(v, v));
        return new CachedStream<>(tupleStream);
    }

    public void forEach(final Consumer<VALUE> consumer) {
        innerStream.forEach(consumer);
    }

    public void forEachOrdered(final Consumer<VALUE> consumer) {
        innerStream.forEachOrdered(consumer);
    }

    public CachingStream<VALUE> filter(final Predicate<VALUE> predicate) {

        Stream<VALUE> filteredStream = innerStream.filter(predicate);
        return new CachingStream<>(filteredStream);
    }

    public <R> CachingStream<R> map(final Function<VALUE, R> mapper) {

        Stream<R> mappedStream = innerStream.map(mapper);
        return new CachingStream<>(mappedStream);
    }

    //TODO: Add in flatmap methods

    public CachingStream<VALUE> distinct() {

        Stream<VALUE> distinctStream = innerStream.distinct();
        return new CachingStream<>(distinctStream);
    }

    public CachingStream<VALUE> sorted() {

        Stream<VALUE> sortedStream = innerStream.sorted();
        return new CachingStream<>(sortedStream);
    }

    public CachingStream<VALUE> sorted(final Comparator<VALUE> comparator) {

        Stream<VALUE> sortedStream = innerStream.sorted(comparator);
        return new CachingStream<>(sortedStream);
    }

    public CachingStream<VALUE> peek(final Consumer<VALUE> consumer) {

        Stream<VALUE> postPeekStream = innerStream.peek(consumer);
        return new CachingStream<>(postPeekStream);
    }

    public CachingStream<VALUE> limit(final long limit) {

        Stream<VALUE> limitedStream = innerStream.limit(limit);
        return new CachingStream<>(limitedStream);
    }

    public CachingStream<VALUE> skip(final long skip) {

        Stream<VALUE> tailStream = innerStream.skip(skip);
        return new CachingStream<>(tailStream);
    }

    public long count() {
        return innerStream.count();
    }

    public Object[] toArray() {
        return innerStream.toArray();
    }

    public VALUE[] toArray(final IntFunction<VALUE[]> generator) {
        return innerStream.toArray(generator);
    }

    public Optional<VALUE> reduce(final BinaryOperator<VALUE> accumulator) {
        return innerStream.reduce(accumulator);
    }

    public VALUE reduce(final VALUE identity, final BinaryOperator<VALUE> accumulator) {
        return innerStream.reduce(identity, accumulator);
    }

    public <R> R reduce(
            final R identity,
            final BiFunction<R, VALUE, R> accumulator,
            final BinaryOperator<R> combiner) {

        return innerStream.reduce(identity, accumulator, combiner);
    }

    public Optional<VALUE> findFirst() {
        return innerStream.findFirst();
    }

    public CachingStream<VALUE> parallel() {
        return new CachingStream<>(innerStream.parallel());
    }

    public Stream<VALUE> toStream() {
        return innerStream;
    }

    public <A, CV> CV collect(final Collector<VALUE, A, CV> collector) {
        return innerStream.collect(collector);
    }

    //TODO: When it exists, return CachingOptional in all places where Optional is currently used
}
