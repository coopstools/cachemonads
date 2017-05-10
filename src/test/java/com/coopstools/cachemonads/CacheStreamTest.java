/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Test;

public class CacheStreamTest {

    private final CacheStream<String, String> stream =
            CacheStream.of(Arrays.asList("a", "b", "c"));
    private final CacheStream<String, String> emptyStream =
            CacheStream.of(Collections.emptyList());

    @Test
    public void testStreamCollectionCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CacheStream<String, String> stream = CacheStream.of(collection);

        assertNotNull(stream);
    }

    @Test
    public void testParrallelStreamCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CacheStream<String, String> stream = CacheStream.parrallelOf(collection);

        assertNotNull(stream);
    }

    @Test
    public void testStreamStreamCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CacheStream<String, String> stream = CacheStream.of(collection.stream());

        assertNotNull(stream);
    }

    @Test
    public void testForEachPresent() {

        List<String> outputs = new ArrayList<>();
        stream.forEach(outputs::add);

        assertEquals(3, outputs.size());
    }

    @Test
    public void testForEachAbsent() {

        List<String> outputs = new ArrayList<>();
        emptyStream.forEach(outputs::add);

        assertEquals(0, outputs.size());
    }

    @Test
    public void testFilterForPresent() {

        List<String> outputs = new ArrayList<>();
        stream.filter(s -> !"b".equals(s)).forEach(outputs::add);

        assertEquals(2, outputs.size());
    }

    @Test
    public void testMapOnPresent() {

        List<Integer> outputs = new ArrayList<>();
        stream.map(String::length).forEach(outputs::add);

        assertEquals(Arrays.asList(1, 1, 1), outputs);
    }

    @Test
    public void testFlatMap() {

        CacheStream<String, String> subStream =
                CacheStream.of(Collections.singletonList("a"));
        long numberOfElements = subStream.flatMap(v -> stream).count();

        assertEquals(3L, numberOfElements);
    }

    @Test
    public void testCountOnPresent() {

        assertEquals(3L, stream.count());
    }

    @Test
    public void testDistinct() {

        CacheStream<String, String> duplicateStream =
                CacheStream.of(Arrays.asList("a", "b", "a", "c", "c", "c"));

        assertEquals(6, duplicateStream.count());

        CacheStream<String, String> distinctStream =
                CacheStream.of(Arrays.asList("a", "b", "a", "c", "c", "c"))
                        .distinct();

        assertEquals(3, distinctStream.count());
    }

    @Test
    public void testSortedOnUnsortable() {

        Stream<UnsortedTestClass> unsortableStream = Stream.of(
                new UnsortedTestClass(3, "green"),
                new UnsortedTestClass(2, "house"));

        CacheStream<UnsortedTestClass, UnsortedTestClass> unsortable =
                CacheStream.of(unsortableStream);

        try {
            unsortable.sorted().map(UnsortedTestClass::getStringValue).forEach(System.out::println);
            fail("Class cast should have been thrown as class is not comparable");
        } catch (ClassCastException ccEx) {
            System.out.println("ClassCastException successfully thrown");
        }
    }

    @Test
    public void testSortedOnSortable() {

        CacheStream<String, String> duplicateStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        List<String> resultList = new ArrayList<>(5);

        duplicateStream.sorted().forEach(resultList::add);
        assertEquals("a", resultList.get(0));
    }

    @Test
    public void testSortedWithComparator() {

        Stream<UnsortedTestClass> unsortableStream = Stream.of(
                new UnsortedTestClass(3, "green"),
                new UnsortedTestClass(2, "house"),
                new UnsortedTestClass(15, "on"),
                new UnsortedTestClass(26, "the"),
                new UnsortedTestClass(7, "hill"),
                new UnsortedTestClass(0, "beside"),
                new UnsortedTestClass(21, "a"),
                new UnsortedTestClass(22, "lake"));

        List<Integer> resultList = new ArrayList<>(2);

        CacheStream.of(unsortableStream)
                .sorted((v1, v2) -> v1.getStringValue().compareTo(v2.getStringValue()))
                .map(UnsortedTestClass::getIntValue)
                .forEach(resultList::add);

        assertEquals(21, resultList.get(0).intValue());
    }

    @Test
    public void testPeek() {

        CacheStream<String, String> peekableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        List<String> resultList = new ArrayList<>(5);
        assertEquals(0, resultList.size());

        //count is used to terminate the stream as peek() won't be called until consumption
        peekableStream.peek(resultList::add).count();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testLimit() {

        long value = 3L;
        CacheStream<String, String> countableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                        .limit(value);

        assertEquals(value, countableStream.count());
    }

    @Test
    public void testSkip() {

        long value = 3L;
        CacheStream<String, String> countableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                        .skip(value);

        assertEquals(5L - value, countableStream.count());
    }

    @Test
    public void testForEachOrdered() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        List<String> resultList = new ArrayList<>(5);

        consumableStream.forEachOrdered(resultList::add);
        assertEquals(5, resultList.size());
    }

    @Test
    public void testToObjectArray() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        Object[] objs = consumableStream.toArray();

        assertEquals("d", objs[0]);
    }

    @Test
    public void testToVALUEArray() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        String[] objs = consumableStream.toArray(String[]::new);

        assertEquals("d", objs[0]);
    }

    @Test
    public void testSingleReduceMethod() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        Optional<String> result = consumableStream.reduce(String::concat);

        assertEquals("dabfc", result.get());
    }

    @Test
    public void testDoubleReduceMethod() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        String result = consumableStream.reduce("r=", String::concat);

        assertEquals("r=dabfc", result);
    }

    @Test
    public void testTripleReduceMethod() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        Integer result = consumableStream
                .reduce(
                        0,
                        (r, v) -> r + v.length(),
                        Integer::sum);

        assertEquals(5, result.intValue());
    }

    @Test
    public void testCacheAndLoad() {

        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("dumb", "bells", "ring", "are", "ya", "listen'n"));

        String max = consumableStream
                .cache()
                .map(String::length)
                .filter(lenght -> lenght > 4)
                .sorted()
                .load()
                .toArray(String[]::new)[0];

        assertEquals("bells", max);
    }
}
