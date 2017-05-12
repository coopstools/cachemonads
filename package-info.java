/**
 * Classes to support caching operations on the java utils Stream and Optional classes. For example:
 *
 * <pre>{@code
 *      Widget youngestLargestWidget = CacheStream.of(widget.strea())
 *              .cache()
 *              .map(Widget:getCharacteristics)
 *              .map(Characteristic::Color)
 *              .filter(RED::equals)
 *              .load()
 *              .map(Widget::getChildWidget())
 *              .filter(child -> child == null)
 *              .map(ChildWidget::getCreationDate)
 *              .sorted(CreationDate::Compare)
 *              .load()
 *              .findFirst()
 * }</pre>
 *
 * <p>Here we use a {@code stream} of {@code widgets} as source to for a CacheStream on which a
 * filter-by-child is performed for two of the child entities of the {@code stream}. {@code cache} and
 * {@code load} are introduced in {@code CacheStream}, while the map, {@code filter}, {@code sorted}, and
 * {@code findFirst} methods are derived from the java utils {@code Stream} class.</p>
 *
 *
 * <p>The key abstraction introduced in this package is a <em>caching</em> mechanism. This mechanism, employed
 * by the methods {@code cache} and {@code load}, allow references to the values in a stream or optional to be
 * stored in a cache, while mainting an association with the values in the stream/optional, and be recalled at
 * a later point. This allows mapping and filtering, as well as the rest of the methods available in the
 * {@code stream} or {@code optional} class, to be performed on the values of the stream/optional without
 * loosing reference to the initial values.</p>
 *
 *
 * CacheStream can be characterized by
 * <ul>
 *     <li>An extension of stream. CacheStreams should, for all methods from the Stream interface, behave
 *     in the exact same manner as the java util stream.</li>
 *     <li>Association retention. After the {@code cache} method is called, an assocation is created between
 *     each value in the stream and a reference to those values now stored in an internal state. When map,
 *     filter, sorted, or any other method is called that transforms, removes, or organizes the elements in
 *     the stream, these associations are retained.</li>
 *     <li>Isoaltion of cached value. For all methods derived from the Stream interface, only the non-cahced
 *     value, otherwise known as the accessable or available value, is acted upon. The CacheStream serves to
 *     hide cached values under a layer of abstraction until the {@code load} method is called.</li>
 * </ul>
 *
 * CacheOptional can be characterized by
 * <ul>
 *     <li>A derivation of optional. CacheOptional should, for all method found in the optional class, behave
 *     in the exact same manner as the java util optional. Custom, externally viewable behaviour should only
 *     be found in new methods, or method overloading.</li>
 *     <li>Association retention. This is the same as the CacheStream.</li>
 *     <li>Isolation of cached value. This is the same as the CachStream.</li>
 * </ul>
 *
 * CacheStreams can be generated from two sources.
 * <ul>
 *     <li>From a {@link java.util.Collection}, by calling the {@code CacheStream.of()} method on the
 *     collection. This will create a non-parrellel stream.</li>
 *     <li>From a {@link java.util.stream.Stream}, by calling the {@code CacheStream.of()} method on the
 *     stream. Whether the CacheStream is parrallel or not parrallel is determined by the type of stream.</li>
 * </ul>
 *
 * <p>The methods derived from Stream maintain their characteristics as <em>intermediate</em> and
 * <em>terminal</em> operations. Reference the stream docs for further explanation. The CacheStream is lazy
 * processed.</p>
 *
 * <p>The {@code cache} and {@code load} methods are both intermediate, deterministic, idempotent operations.
 * </p>
 *
 * @since 0.1.0
 */