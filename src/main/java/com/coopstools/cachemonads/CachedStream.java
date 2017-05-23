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

/**
 * An extension of the java utils {@link Stream} that allows the caching of a reference
 * to the active value for use in later operations; and allows the loading of the
 * reference from cache back into the active value.
 * <p>
 * <pre>{@code
 *      String smallWord = CacheStream.of(
 *              Arrays.asList("code", "monkey", "get", "up", "get", "coffee", "code", "monkey", "go", "to", "job"))
 *              .cache() //creates a reference to the Strings in the cache buffer
 *              .map(String::length) //maps the Strings in the available value, but does not affect the cache
 *              .filter(length -> length >= 3)
 *              .sorted()
 *              .load() //Loads the reference from cache back into the available value
 *              .findFirst()
 *              .get();
 *
 *      assert smallestWord == "get";
 * }</pre>
 * <p>
 * In this example, a cache stream is created from an ArrayList. Within the stream, a reference to each value
 * is stored in cache; the the accessable value is mapped from a string, to the length of that stream, without
 * altering the value stored in cache; the accessable value is then filtered to remove lengths less than 3;
 * and then the remaining values are sorted. After sorting, the reference in cache are used to overwrite their
 * corresponding accessable values. And finally, the first value is pulled out of the stream. As the value
 * pulled out is an Optional, the get method is called to pull the stored value out.
 * <p>
 * When a value is placed in cache, it's remains associated with the accessable value. When the accessable
 * value is mapped, the association remains. This allows for filtering, sorting and other operations to be
 * performed on child or derived values without losing the original value.
 * <p>
 * Outside of the cache and load methods (and a few other additions), the cache stream works exactly the same
 * as the java utils {@link Stream}. These javadocs attempt to describe the exact operation of each public
 * method. However, for further clarity, please consult the docs associated with the native {@link Stream}.
 *
 * @param <CACHE> The type of the value stored in cache (In later versions, if there does not exist a value in
 *                cache, this parameter will not be needed.
 * @param <VALUE> The type of the accessable value. This is the value upon which most of the methods operate.
 */
public class CachedStream<CACHE, VALUE> {

    private final Stream<CacheTuple<CACHE, VALUE>> innerStream;

    /**
     * Returns an instance of the CacheStream, which acts as an adapter for the java utils {@code Stream},
     * explosing method which exclusively act upon the right, or accessable, value of the
     *
     * @param innerStream A {@code Stream} of tuples which is contained in the CacheStream
     */
    CachedStream(Stream<CacheTuple<CACHE, VALUE>> innerStream) {
        this.innerStream = innerStream;
    }

    /**
     * Returns a CacheStream, where the accessable values have been mapped to the associated cached values.
     *
     * @return A new instance of CacheStream with a reference to the mapped inner stream
     */
    public CachingStream<CACHE> load() {

        Stream<CACHE> cachedStream =
                innerStream.map(CacheTuple::getLeft);
        return CachingStream.of(cachedStream);
    }

    /**
     * Performs an action on each of the accessable values in the inner stream.
     * <p>
     * This is a terminal operation as defined in the {@code Stream} docs. As defined in the docs, this
     * operation is explicitly nondeterministic.
     *
     * @param action A non-interfering action to perform on the accessable values of the inner stream.
     */
    public void forEach(final Consumer<VALUE> action) {

        innerStream.forEach(pair -> action.accept(pair.getRight()));
    }

    /**
     * Performs an action on each of the accessable values in the inner stream, in the encounter order of the
     * stream if the stream has a defined order.
     * <p>
     * This is a terminal operation as defined in the {@code Stream} docs. This operation processes the
     * elements one at a time, in encounter order if one exists. For further reference consult the
     * {@code Stream} docs.
     *
     * @param action A non-interfering action to perform on the accessable values of the inner stream.
     */
    public void forEachOrdered(final Consumer<VALUE> action) {

        innerStream.forEachOrdered(pair -> action.accept(pair.getRight()));
    }

    /**
     * Returns a CacheStream consisting of the accessable values that meet the given predicate. The accessable
     * values will persist their association with cached values. i.e Any accessable values that remains, will
     * remain assocaited with the same cached calue; and any accessable value that is filter out, will also
     * remove its assocaited cached value.
     * <p>
     * This is an intermediate operation and will not terminate the stream.
     *
     * @param predicate A non-interfering, stateless predicate to apply to each accessable value to determine
     *                  if that value should remain in the stream
     * @return A post filter CacheStream
     */
    public CachedStream<CACHE, VALUE> filter(final Predicate<VALUE> predicate) {

        Stream<CacheTuple<CACHE, VALUE>> filteredStream =
                innerStream.filter(pair -> predicate.test(pair.getRight()));
        return new CachedStream<>(filteredStream);
    }

    /**
     * Returns a CacheStream consisting of the results of applying the given mapping function to the
     * accessable values in the stream. The newly mapped values will retain their association with the cached
     * values; and the mapping function will have no effect on the cached values.
     * <p>
     * This is an intermediate operation and will not terminate the stream.
     *
     * @param mapper A non-interfering, stateless function to apply to each accessable value
     * @param <R>    The element type of the new accessable values
     * @return the new, mapped CacheStream
     */
    public <R> CachedStream<CACHE, R> map(final Function<VALUE, R> mapper) {

        Stream<CacheTuple<CACHE, R>> mappedStream =
                innerStream.map(pair ->
                        new CacheTuple<>(pair.getLeft(), mapper.apply(pair.getRight())));
        return new CachedStream<>(mappedStream);
    }

    //TODO: create map that takes two arguments: Value, and Cache

    /**
     * Returns a stream constructed from the concatanation of the CacheStreams generated by the mapping
     * function, mapper, on the accessable value. Each element in the new stream will be associated with the
     * cache value from with which the original value was associated.
     * <p>
     * This is an intermediate operation and will not terminate the stream. For more clarity on its operation,
     * consult the docs for java.util.Stream.
     *
     * @param mapper A non-interfering, statelss function to apply to each accessable value
     * @param <RV>   The type of the right value returned in the mapper
     * @return the new cache stream
     */
    public <RV> CachedStream<CACHE, RV> flatMap(final Function<VALUE, Stream<RV>> mapper) {

        Stream<CacheTuple<CACHE, RV>> mappedStream =
                innerStream.flatMap(pair -> this.subFlatMap(pair, mapper));
        return new CachedStream<>(mappedStream);
    }

    private <RV> Stream<CacheTuple<CACHE, RV>> subFlatMap(
            final CacheTuple<CACHE, VALUE> pair, final Function<VALUE, Stream<RV>> mapper) {

        CACHE leftValue = pair.getLeft();
        Stream<RV> rightStream = mapper.apply(pair.getRight());
        return rightStream.map(rightValue -> new CacheTuple<>(leftValue, rightValue));
    }

    //TODO: Find way to create flatMap for taking CacheStreams

    /**
     * Returns a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of
     * the accessable values of the stream. During the distinct process, the cache values are ignored. The
     * values that are kept will retain their association with the corresponding cached value. If there are
     * two or more, non-distinct, accessable values with different assocaited cached values, there is no
     * assurance as to which cache/accessable pair will be kept.
     *
     * @return return stream of distinct values
     */
    public CachedStream<CACHE, VALUE> distinct() {

        Stream<CacheTuple<CACHE, VALUE>> distinctStream =
                innerStream.distinct();
        return new CachedStream<>(distinctStream);
    }

    //TODO: Create flatmap that returns CacheStream, but takes a bi-function as an argument
    //TODO: Create flatmap that takes java.utils.Stream as a return value, instead of CacheStream

    public CachedStream<CACHE, VALUE> sorted() {

        Stream<CacheTuple<CACHE, VALUE>> sortedStream =
                innerStream.sorted();
        return new CachedStream<>(sortedStream);
    }

    public CachedStream<CACHE, VALUE> sorted(final Comparator<VALUE> comparator) {

        Stream<CacheTuple<CACHE, VALUE>> sortedStream =
                innerStream.sorted((t1, t2) -> comparator.compare(t1.getRight(), t2.getRight()));
        return new CachedStream<>(sortedStream);
    }

    public CachedStream<CACHE, VALUE> peek(final Consumer<VALUE> consumer) {

        Stream<CacheTuple<CACHE, VALUE>> peekedStrem =
                innerStream.peek(pair -> consumer.accept(pair.getRight()));
        return new CachedStream<>(peekedStrem);
    }

    //TODO: Add peek method that accepts BiFunction

    public CachedStream<CACHE, VALUE> limit(final long limit) {

        Stream<CacheTuple<CACHE, VALUE>> limitedStream =
                innerStream.limit(limit);
        return new CachedStream<>(limitedStream);
    }

    public CachedStream<CACHE, VALUE> skip(final long skip) {

        Stream<CacheTuple<CACHE, VALUE>> limitedStream =
                innerStream.skip(skip);
        return new CachedStream<>(limitedStream);
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

    public Optional<VALUE> reduce(final BinaryOperator<VALUE> accumulater) {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.reduce(accumulater);
    }

    public VALUE reduce(final VALUE identity, final BinaryOperator<VALUE> accumulater) {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.reduce(identity, accumulater);
    }

    public <U> U reduce(
            final U identity,
            final BiFunction<U, VALUE, U> accumulater,
            final BinaryOperator<U> combiner) {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.reduce(identity, accumulater, combiner);
    }

    //TODO: Update method to return CacheOptional, or create new findFirstWithCache() method
    public Optional<VALUE> findFirst() {

        Stream<VALUE> mappedStream =
                innerStream.map(CacheTuple::getRight);
        return mappedStream.findFirst();
    }

    public Stream<VALUE> toStream() {
        return innerStream.map(CacheTuple::getRight);
    }

    public <A, CV> CV collect(Collector<VALUE, A, CV> collector) {
        return innerStream.map(CacheTuple::getRight).collect(collector);
    }

    //TODO: min
    //TODO: max
    //TODO: anyMatch
    //TODO: allMatch
    //TODO: noneMatch
    //TODO: findAny
    //TODO: concat

    //TODO: drop() - drops the cached value and returns a CachingStream

    //TODO: collect (the other kind)
    //TODO: mapToInt, Long, Double
    //TODO: flatMapToInt, Long, Double

    //TODO: FilterNull()
    //TODO: flatten() - if value if of type Collection, this will flatten it out
    //TODO: empty()
    //TODO: FlatMap of CacheStream

    //static methods
    //TODO: generate (could take two arguments; one for generating CACHE, and the other forbuilding teh value from the CACHE)
    //TODO: iterate
    //TODO: Builder (with interface)
}
