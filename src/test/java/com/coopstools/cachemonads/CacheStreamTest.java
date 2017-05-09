/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
    public void  testForEachAbsent() {

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
}
