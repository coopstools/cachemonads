/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopsutils.cachemonads;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class CacheOptional<C, V> {

    private static final CacheOptional<?, ?> EMPTY = new CacheOptional<>();

    private final C cached;
    private final V value;

    private CacheOptional() {

        this.cached = null;
        this.value = null;
    }

    private CacheOptional(final C cached, final V value) {

        this.value = Objects.requireNonNull(value);
        this.cached = cached;
    }

    public static <V> CacheOptional<V, V> of(final V value) {

        return new CacheOptional<>(value, value);
    }

    public static <V> CacheOptional<V, V> ofNullable(final V value) {

        return (value == null) ? empty() : new CacheOptional<>(value, value);
    }

    public static <T, R> CacheOptional<T, R> empty() {

        @SuppressWarnings("unchecked")
        CacheOptional<T, R> t = (CacheOptional<T, R>) EMPTY;
        return t;
    }

    public V get() {

        if (value == null)
            throw new NoSuchElementException("Absent value");
        return value;
    }

    public Boolean isPresent() {
        return (value != null);
    }

    public void ifPresent(final Consumer<? super V> consumer) {

        if (isPresent())
            consumer.accept(value);
    }

    public void ifPresentOrElse(
            final Consumer<? super V> presentAction, final Runnable absentAction) {

        if (isPresent()) {
            Objects.requireNonNull(presentAction);
            presentAction.accept(value);
        } else {
            Objects.requireNonNull(absentAction);
            absentAction.run();
        }
    }

    //Java 9 Optional methods
    //TODO: Add Stream method
    //TODO: check all lambdas if null, and return nullpointer with description

    public CacheOptional<C, V> or(
            final Supplier<? extends CacheOptional<C,V>> supplier) {

        return isPresent() ? this : supplier.get();
    }

    public void orDo(final Runnable runnable) {

        if (!isPresent())
            runnable.run();
    }

    public V orElse(final V defaultValue) {

        return isPresent() ? value : defaultValue;
    }

    public V orElseGet(final Supplier<V> supplier) {

        return isPresent() ? value : supplier.get();
    }

    public <T extends Throwable> V orElseThrow(final Supplier<? extends T> exceptionSupplier) throws T {

        if (isPresent())
            return value;

        throw exceptionSupplier.get();
    }

    public <R> CacheOptional<C, R> map(final Function<? super V, R> mapper) {

        if (mapper == null)
            throw new NullPointerException("Mapping function must not be null");

        if (!isPresent())
            return empty();

        R newValue = mapper.apply(value);
        if (newValue == null)
            return empty();

        return new CacheOptional<>(cached, newValue);
    }

    //TODO: flatmap

    public CacheOptional<V, V> cache() {

        if (!isPresent())
            return empty();
        return of(value);
    }

    public CacheOptional<C, C> load() {

        if (!isPresent() || cached == null)
            return empty();
        return of(cached);
    }

    public CacheOptional<C, V> filter(final Predicate<? super V> predicate) {

        if (!isPresent())
            return empty();
        return predicate.test(value) ? this : empty();
    }

    public boolean equals(Object obj) {

        //TODO: Find out why this is not being tested
        if (this == obj)
            return true;

        if (!(obj instanceof CacheOptional))
            return false;

        CacheOptional<?, ?> other = (CacheOptional<?, ?>) obj;
        return Objects.equals(value, other.value);
    }

    public String toString() {
        return value != null
                ? String.format("CacheOptional[%s, %s]", cached, value)
                : "CacheOptional.empty";
    }
}

