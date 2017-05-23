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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.coopstools.Parent;
import com.coopstools.Child;

public class CachedStreamTest {

    private final CachedStream<String, String> stream =
            CachingStream.of(Arrays.asList("a", "b", "c")).cache();
    private final List<String> emptyStrings = Collections.emptyList();
    private final CachedStream<String, String> emptyStream =
            CachingStream.of(emptyStrings).cache();

    @Test
    public void testStreamCollectionCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachedStream<String, String> stream = CachingStream.of(collection).cache();

        assertNotNull(stream);
    }

    @Test
    public void testParrallelStreamCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachedStream<String, String> stream = CachingStream.parallelOf(collection).cache();

        assertNotNull(stream);
    }

    @Test
    public void testStreamStreamCreation() {

        Collection<String> collection = Arrays.asList("a", "b", "c");
        CachedStream<String, String> stream = CachingStream.of(collection.stream()).cache();

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

    /*@Test
    public void testFlatMap() {

        CacheStream<String, String> subStream =
                CacheStream.of(Collections.singletonList("a"));
        long numberOfElements = subStream.flatMap(v -> stream).count();

        assertEquals(3L, numberOfElements);
    }*/

    @Test
    public void testFlatMapFromStream() {

        Parent parent1 = new Parent("parent1");
        parent1.setChildren(Arrays.asList(new Child(4), new Child(11)));
        Parent parent2 = new Parent("parent2");
        parent2.setChildren(Arrays.asList(new Child(3), new Child(6)));
        Parent parent3 = new Parent("parent3");
        parent3.setChildren(Arrays.asList(new Child(12), new Child(16)));

        Parent[] parents = CachingStream.of(Arrays.asList(parent1, parent2, parent3))
                .cache()
                .map(Parent::getChildren)
                .flatMap(Collection::stream)
                .map(Child::getAttribute1)
                .filter(att -> att > 10)
                .load()
                .toStream()
                .distinct()
                .toArray(Parent[]::new);

        assertEquals(2, Arrays.asList(parents).size());
    }

    @Test
    public void testCountOnPresent() {

        assertEquals(3L, stream.count());
    }

    @Test
    public void testDistinct() {

        CachedStream<String, String> duplicateStream =
                CachingStream.of(Arrays.asList("a", "b", "a", "c", "c", "c")).cache();

        assertEquals(6, duplicateStream.count());

        CachedStream<String, String> distinctStream =
                CachingStream.of(Arrays.asList("a", "b", "a", "c", "c", "c")).cache()
                        .distinct();

        assertEquals(3, distinctStream.count());
    }

    @Test
    public void testSortedOnUnsortable() {

        Stream<UnsortedTestClass> unsortableStream = Stream.of(
                new UnsortedTestClass(3, "green"),
                new UnsortedTestClass(2, "house"));

        CachedStream<UnsortedTestClass, UnsortedTestClass> unsortable =
                CachingStream.of(unsortableStream).cache();

        try {
            unsortable.sorted().map(UnsortedTestClass::getStringValue).forEach(System.out::println);
            fail("Class cast should have been thrown as class is not comparable");
        } catch (ClassCastException ccEx) {
            System.out.println("ClassCastException successfully thrown");
        }
    }

    @Test
    public void testSortedOnSortable() {

        CachedStream<String, String> duplicateStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache().sorted();

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
                .cache()
                .sorted((v1, v2) -> v1.getStringValue().compareTo(v2.getStringValue()))
                .map(UnsortedTestClass::getIntValue)
                .forEach(resultList::add);

        assertEquals(21, resultList.get(0).intValue());
    }

    @Test
    public void testPeek() {

        CachedStream<String, String> peekableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        List<String> resultList = new ArrayList<>(5);
        assertEquals(0, resultList.size());

        //count is used to terminate the stream as peek() won't be called until consumption
        peekableStream.peek(resultList::add).count();
        assertEquals(5, resultList.size());
    }

    @Test
    public void testLimit() {

        long value = 3L;
        CachedStream<String, String> countableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache()
                        .limit(value);

        assertEquals(value, countableStream.count());
    }

    @Test
    public void testSkip() {

        long value = 3L;
        CachedStream<String, String> countableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache()
                        .skip(value);

        assertEquals(5L - value, countableStream.count());
    }

    @Test
    public void testForEachOrdered() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        List<String> resultList = new ArrayList<>(5);

        consumableStream.forEachOrdered(resultList::add);
        assertEquals(5, resultList.size());
    }

    @Test
    public void testToObjectArray() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        Object[] objs = consumableStream.toArray();

        assertEquals("d", objs[0]);
    }

    @Test
    public void testToVALUEArray() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        String[] objs = consumableStream.toArray(String[]::new);

        assertEquals("d", objs[0]);
    }

    @Test
    public void testSingleReduceMethod() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        Optional<String> result = consumableStream.reduce(String::concat);

        assertEquals("dabfc", result.get());
    }

    @Test
    public void testDoubleReduceMethod() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        String result = consumableStream.reduce("r=", String::concat);

        assertEquals("r=dabfc", result);
    }

    @Test
    public void testTripleReduceMethod() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        Integer result = consumableStream
                .reduce(
                        0,
                        (r, v) -> r + v.length(),
                        Integer::sum);

        assertEquals(5, result.intValue());
    }

    @Test
    public void testFindFirst() {

        CachedStream<String, String> consumableStream =
                CachingStream.of(Arrays.asList("d", "a", "b", "f", "c")).cache();

        Optional<String> maybeFirstValue = consumableStream
                .sorted()
                .findFirst();

        assertEquals("a", maybeFirstValue.get());
    }

    @Test
    public void testCacheAndLoad() {

        CachingStream<String> consumableStream =
                CachingStream.of("dumb", "bells", "ring", "are", "ya", "listen'n");

        String max = consumableStream
                .cache()
                .map(String::length)
                .filter(lenght -> lenght > 4)
                .sorted()
                .load()
                .toStream()
                .toArray(String[]::new)[0];

        assertEquals("bells", max);
    }

    @Test
    public void messingAroundWithCacheLoad() {

        String smallWord = CachingStream
                .of("code", "monkey", "get", "up", "get", "coffee", "code", "monkey", "go", "to", "job")
                .cache() //creates a reference to the Strings in the cache buffer
                .map(String::length) //maps the Strings in the available value, but does not affect the cache
                .filter(length -> length >= 3)
                .sorted()
                .load() //Loads the reference from cache back into the available value
                .toStream() //At the time of the writing of this test, Caching stream don't have find first
                .findFirst()
                .get();
        assertEquals("get", smallWord);
    }

    @Test
    public void testToStream() {

        Stream<String> streamingMusic = CachingStream.of(
                Arrays.asList("code", "monkey", "get", "up", "get", "coffee", "code", "monkey", "go", "to", "job"))
                .cache()
                .toStream();

        assertEquals(11, streamingMusic.count());
    }

    @Test
    public void testCollect() {

        List<String> playList = CachingStream.of(
                Arrays.asList("code", "monkey", "get", "up", "get", "coffee", "code", "monkey", "go", "to", "job"))
                .cache()
                .collect(Collectors.toList());

        assertEquals("code", playList.get(0));
    }
}
