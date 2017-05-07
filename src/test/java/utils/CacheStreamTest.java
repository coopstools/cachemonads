/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.coopsutils.cachemonads.CacheStream;

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
}
