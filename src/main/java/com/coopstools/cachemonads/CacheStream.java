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
import java.util.stream.Stream;

/**
 * An extension of the java utils {@link Stream} that allows the caching of a reference
 * to the active value for use in later operations; and allows the loading of the
 * reference from cache back into the active value.
 *
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
 *
 * In this example, a cache stream is created from an ArrayList. Within the stream, a reference to each value
 * is stored in cache; the the accessable value is mapped from a string, to the length of that stream, without
 * altering the value stored in cache; the accessable value is then filtered to remove lengths less than 3;
 * and then the remaining values are sorted. After sorting, the reference in cache are used to overwrite their
 * corresponding accessable values. And finally, the first value is pulled out of the stream. As the value
 * pulled out is an Optional, the get method is called to pull the stored value out.
 *
 * When a value is placed in cache, it's remains associated with the accessable value. When the accessable
 * value is mapped, the association remains. This allows for filtering, sorting and other operations to be
 * performed on child or derived values without losing the original value.
 *
 * Outside of the cache and load methods (and a few other additions), the cache stream works exactly the same
 * as the java utils {@link Stream}. These javadocs attempt to describe the exact operation of each public
 * method. However, for further clarity, please consult the docs associated with the native {@link Stream}.
 *
 * @param <CACHE> The type of the value stored in cache (In later versions, if there does not exist a value in
 *               cache, this parameter will not be needed.
 * @param <VALUE> The type of the accessable value. This is the value upon which most of the methods operate.
 */
public class CacheStream<CACHE, VALUE> {

    private final Stream<CacheTuple<CACHE, VALUE>> innerStream;

    /**
     * Returns a sequential ordered CacheStream whose elements are the specified values with a null reference,
     * of the same type as the elements, stored in the cache.
     *
     * @param <V> The type of the stream available values, and the stream cache values
     * @param collection A collection of the elements that will make up the accessable values in the
     *                   CacheStream
     * @return the new CachStream
     */
    public static <V> CacheStream<V, V> of(final Collection<V> collection) {

        return new CacheStream<>(collection.stream().map(CacheStream::makeTuple));
    }

    /**
     * Returns a CacheStream whose elements are those from the stream with a null reference, of the same type
     * as the elements, stored in the cache. Whether the CacheStream will operate in sequence or parrallel is
     * dependant on the supplied stream.
     *
     * @param <V> The type of the stream available values, and the stream cache values
     * @param stream A stream of the elements that will make up the accessable values in the CacheStream
     * @return the new CachStream
     */
    public static <V> CacheStream<V, V> of(final Stream<V> stream) {

        return new CacheStream<>(stream.map(CacheStream::makeTuple));
    }

    /**
     * Returns a stream, which will run in parrallel, whose elements consist of those from the supplied
     * collection.
     *
     * @param <V> The type of the stream available values, and the stream cache values
     * @param collection A collection of the elements that will make up the accessable values in the
     *                   CacheStream
     * @return the new parrallel CacheStream
     */
    public static <V> CacheStream<V, V> parrallelOf(final Collection<V> collection) {

        return new CacheStream<>(collection.parallelStream().map(CacheStream::makeTuple));
    }

    /**
     * Returns a new tuple, whose right value will be the CacheStream's accessable value, upon which various
     * operations can be performed. The left value is initialized as null; and will be where a reference to
     * the accessable will be stored when the cache() method is called.
     *
     * @param value The value to be stored in the right side of teh tuple, and upon which most CacheStream
     *              methods operate
     * @param <C> The type of the cached value. Upon initialization, this will always be the same as the type
     *           of the accessable, right value.
     * @param <V> The type of the right, or accessable, value.
     * @return A new tuple consisting of a left, cached, value, and a right, accessable, value.
     */
    private static <C, V> CacheTuple<C, V> makeTuple(final V value) {
        return new CacheTuple<>((C) null, value);
    }

    /**
     * Returns an instance of the CacheStream, which acts as an adapter for the java utils {@code Stream},
     * explosing method which exclusively act upon the right, or accessable, value of the
     *
     * @param innerStream A {@code Stream} of tuples which is contained in the CacheStream
     */
    private CacheStream(Stream<CacheTuple<CACHE, VALUE>> innerStream) {
        this.innerStream = innerStream;
    }

    /**
     * Returns a CacheStream, where the cache values have been mapped to the associated accessable values.
     *
     * @return A new instance of CacheStream with a reference to the mapped inner stream
     */
    public CacheStream<VALUE, VALUE> cache() {

        Stream<CacheTuple<VALUE, VALUE>> cachedStream =
                innerStream.map(pair -> new CacheTuple<>(pair.getRight(), pair.getRight()));
        return new CacheStream<>(cachedStream);
    }

    /**
     * Returns a CacheStream, where the accessable values have been mapped to the associated cached values.
     *
     * @return A new instance of CacheStream with a reference to the mapped inner stream
     */
    public CacheStream<CACHE, CACHE> load() {

        Stream<CacheTuple<CACHE, CACHE>> cachedStream =
                innerStream.map(pair -> new CacheTuple<>(pair.getLeft(), pair.getLeft()));
        return new CacheStream<>(cachedStream);
    }

    /**
     * Performs an action on each of the accessable values in the inner stream.
     *
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
     *
     * This is a terminal operation as defined in the {@code Stream} docs. This operation processes the
     * elements one at a time, in encounter order is one exists. For further reference consult the
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
     *
     * This is an intermediate operation and will not terminate the stream.
     *
     * @param predicate A non-interfering, stateless predicate to apply to each accessable value to determine
     *                  if that value should remain in the stream
     * @return A post filter CacheStream
     */
    public CacheStream<CACHE, VALUE> filter(final Predicate<VALUE> predicate) {

        Stream<CacheTuple<CACHE, VALUE>> filteredStream =
                innerStream.filter(pair -> predicate.test(pair.getRight()));
        return new CacheStream<>(filteredStream);
    }

    /**
     * Returns a CacheStream consisting of the results of applying the given mapping function to the
     * accessable values in the stream. The newly mapped values will retain their association with the cached
     * values; and the mapping function will have no effect on the cached values.
     *
     * This is an intermediate operation and will not terminate the stream.
     *
     * @param mapper A non-interfering, stateless function to apply to each accessable value
     * @param <R> The element type of the new accessable values
     * @return the new, mapped CacheStream
     */
    public <R> CacheStream<CACHE, R> map(final Function<VALUE, R> mapper) {

        Stream<CacheTuple<CACHE, R>> mappedStream =
                innerStream.map(pair ->
                        new CacheTuple<>(pair.getLeft(), mapper.apply(pair.getRight())));
        return new CacheStream<>(mappedStream);
    }

    /**
     * Returns a stream constructed from the concatanation of the CacheStreams generated by the mapping
     * function, mapper, on the accessable value. The mapper is responsible for setting up the new association
     * between cached and accessable values.
     *
     * This is an intermediate operation and will not terminate the stream. For more clarity on its operation,
     * consult the docs for java.util.Stream.
     *
     * @param mapper A non-interfering, statelss function to apply to each accessable value
     * @param <RV> The type of the right value returned in the mapper
     * @param <RC> The type of the left, or cached, value returned in the mapper
     * @return
     */
    public <RC, RV> CacheStream<RC, RV> flatMap(final Function<VALUE, CacheStream<RC, RV>> mapper) {

        Stream<CacheTuple<RC, RV>> mappedStream =
                innerStream.flatMap(pair ->
                    mapper.apply(pair.getRight()).innerStream);
        return new CacheStream<>(mappedStream);
    }

    /**
     * Returns a stream consisting of the distinct elements (according to {@link Object#equals(Object)}) of
     * the accessable values of the stream. During the distinct process, the cache values are ignored. The
     * values that are kept will retain their association with the corresponding cached value. If there are
     * two or more, non-distinct, accessable values with different assocaited cached values, there is no
     * assurance as to which cache/accessable pair will be kept.
     *
     *
     * @return
     */
    public CacheStream<CACHE, VALUE> distinct() {

        Stream<CacheTuple<CACHE, VALUE>> distinctStream =
                innerStream.distinct();
        return new CacheStream<>(distinctStream);
    }

    //TODO: Create flatmap that returns CacheStream, but takes a bi-function as an argument
    //TODO: Create flatmap that takes java.utils.Stream as a return value, instead of CacheStream

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

    //TODO: min
    //TODO: max
    //TODO: anyMatch
    //TODO: allMatch
    //TODO: noneMatch
    //TODO: findAny
    //TODO: concat

    //TODO: collect (x2)
    //TODO: mapToInt, Long, Double
    //TODO: flatMapToInt, Long, Double

    //TODO: FilterNull()
    //TODO: empty()
    //TODO: FlatMap of java util stream

    //static methods
    //TODO: generate (could take two arguments; one for generating CACHE, and the other forbuilding teh value from the CACHE)
    //TODO: iterate
    //TODO: Builder (with interface)
}
