/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class CachingStreamTest {

    @Test
    public void testStreamCollectionCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachingStream<String> stream = CachingStream.of(collection);

        assertNotNull(stream);
    }

    @Test
    public void testStreamStreamCreation() {

        Stream<Integer> stream = Stream.iterate(0, i -> i + 1).limit(10);
        CachingStream<Integer> cachingStream = CachingStream.of(stream);

        assertNotNull(cachingStream);
    }

    @Test
    public void testStreamArrayCreation() {

        Double[] doubles = {1.0, 2.0, 3.0};
        CachingStream<Double> cachingStream = CachingStream.of(doubles);

        assertNotNull(cachingStream);
    }

    @Test
    public void testToStream() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachingStream<String> cachingStream = CachingStream.of(collection);
        Stream<String> extractedStream = cachingStream.toStream();

        assertEquals(3, extractedStream.count());
    }

    @Test
    public void testParrallelStreamCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachingStream<String> cachingStream = CachingStream.parallelOf(collection);
        Stream<String> extractedStream = cachingStream.toStream();

        assertTrue(extractedStream.isParallel());
    }

    @Test
    public void testParrallelization() throws Exception {

        Stream<Integer> stream = Stream.iterate(0, i -> i + 1).limit(10);
        CachingStream<Integer> cachingStream = CachingStream.of(stream).parallel();
        Stream<Integer> extractedStream = cachingStream.toStream();

        assertTrue(extractedStream.isParallel());
    }

    @Test
    public void testCacheMethod() throws Exception {

        Stream<Integer> stream = Stream.iterate(1, i -> i + 1).limit(10);
        CachingStream<Integer> cachingStream = CachingStream.of(stream);
        CachedStream<Integer, Integer> cachedStream = cachingStream.cache();
        Integer total = cachedStream
                .map(v -> v + 1000)
                .load()
                .toStream()
                .reduce(0, (v1, v2) -> v1 + v2);

        assertEquals(55, total.intValue());
    }

    @Test
    public void testForEachMethod() {

        List<Integer> testList = new ArrayList<>();
        CachingStream.of(Stream.iterate(0, n -> n+1).limit(10))
                .forEach(testList::add);

        assertEquals(10, testList.size());
    }

    @Test
    public void testForEacOrderedhMethod() {

        List<Integer> testList = new ArrayList<>();
        CachingStream.of(Stream.iterate(0, n -> n+1).limit(10))
                .forEachOrdered(testList::add);

        assertEquals(10, testList.size());
    }

    @Test
    public void testFilter() throws Exception {

        Long postFilterCount = CachingStream.of(Stream.iterate(0, n -> n+1).limit(10))
                .filter(value -> (value % 2) == 0)
                .toStream()
                .count();

        assertEquals(5L, postFilterCount.longValue());
    }

    @Test
    public void testMappingMethod() {

        Integer sum = CachingStream.of(Stream.iterate(0, n -> n+1).limit(10))
                .map(value -> 10)
                .toStream()
                .reduce(0, (v1, v2) -> v1 + v2);

        assertEquals(100, sum.intValue());
    }

    @Test
    public void testDistinct() throws Exception {

        Long numberOfDistinctValues = CachingStream.of(Stream.iterate(0, n -> n+1).limit(10))
                .map(value -> value % 3)
                .distinct()
                .toStream()
                .count();

        assertEquals(3L, numberOfDistinctValues.longValue());
    }

    @Test
    public void testSortMethod() throws Exception {

        Stream<UnsortedTestClass> unsortableStream = Stream.of(
                new UnsortedTestClass(3, "green"),
                new UnsortedTestClass(2, "house"));

        CachingStream<UnsortedTestClass> unsortable =
                CachingStream.of(unsortableStream);

        try {
            unsortable.sorted().map(UnsortedTestClass::getStringValue).forEach(System.out::println);
            fail("Class cast should have been thrown as class is not comparable");
        } catch (ClassCastException ccEx) {
            System.out.println("ClassCastException successfully thrown");
        }
    }

    @Test
    public void testSortedOnSortable() {

        CachingStream<String> duplicateStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"));

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

        CachingStream.of(unsortableStream)
                .sorted((v1, v2) -> v1.getStringValue().compareTo(v2.getStringValue()))
                .map(UnsortedTestClass::getIntValue)
                .forEach(resultList::add);

        assertEquals(21, resultList.get(0).intValue());
    }

    @Test
    public void testPeekMethod() throws Exception {

        CachingStream<String> peekableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"));

        List<String> resultList = new ArrayList<>(5);
        assertEquals(0, resultList.size());

        //count is used to terminate the stream as peek() won't be called until consumption
        peekableStream.peek(resultList::add).toStream().count();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testLimitMethod() throws Exception {

        Long limit = CachingStream.of(Stream.iterate(0, n -> n+1))
                .limit(3L)
                .toStream()
                .count();

        assertEquals(3, limit.longValue());
    }

    @Test
    public void testSkipMethod() throws Exception {

        Long limit = CachingStream.of(Stream.iterate(0, n -> n+1))
                .limit(5L)
                .skip(3L)
                .toStream()
                .count();

        assertEquals(2L, limit.longValue());
    }

    @Test
    public void testCount() throws Exception {

        Long limit = CachingStream.of(Stream.iterate(0, n -> n+1))
                .limit(5L)
                .count();

        assertEquals(5L, limit.longValue());
    }

    @Test
    public void testToObjectArray() {

        Object[] objs =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                .sorted()
                .toArray();

        assertEquals("a", objs[0]);
    }

    @Test
    public void testToArrayWithConstructor() throws Exception {

        String[] strings =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                        .sorted()
                        .toArray(String[]::new);

        assertEquals("a", strings[0]);
    }

    @Test
    public void testSingleArgumentReduceMethod() throws Exception {

        String concatted = CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                .sorted()
                .reduce(String::concat)
                .get();

        assertEquals("abcdf", concatted);
    }

    @Test
    public void testTwoArgumentReduceMethod() throws Exception {

        String concatted = CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                .sorted()
                .reduce("x", String::concat);

        assertEquals("xabcdf", concatted);
    }

    @Test
    public void testThreeArgumentReduceMethod() throws Exception {

        Integer concatted = CachingStream.of(Arrays.asList("dddd", "a", "bbb", "ffffff", "cccc"))
                .sorted()
                .reduce(0, (sum, v2) -> sum + v2.length(), Integer::sum);

        assertEquals(18, concatted.intValue());
    }

    @Test
    public void testFindFirst() throws Exception {

        String first = CachingStream.of(Arrays.asList("d", "a", "b", "f", "c"))
                .sorted()
                .findFirst()
                .get();

        assertEquals("a", first);
    }

    @Test
    public void testCollect() {

        List<String> playList = CachingStream.of(
                Arrays.asList("code", "monkey", "get", "up", "get", "coffee", "code", "monkey", "go", "to", "job"))
                .collect(Collectors.toList());

        assertEquals("code", playList.get(0));
    }
}
