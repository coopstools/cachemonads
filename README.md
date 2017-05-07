# cachemonads
Exposes Stream and Optional monads with cache value

These new monads give the user the option to 'cache' the current value into a buffer; or to 'load' the value from the buffer into the current working value. This gives the ability to perform operations on child entities without losing the original. In particular, thisis usuful when a user would want to filter using a child entity.

It can be created in the same way as the java util Optional.
```
CacheOptional<String, String> maybeValue = CacheOptional.of("Hello, World!!!")
```

On top of the standard operations that are found in the Java 8 Optional class, this new class gives two additional methods:
```
public CacheOptional<V, V> cache();
public CacheOptional<C, C> load();
```
