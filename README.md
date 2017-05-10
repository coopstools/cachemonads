# cachemonads
Exposes Stream and Optional monads with cache values.

These new monads give the user the option to 'cache' the current value into a buffer; or to 'load' the value from the buffer into the current working value. This gives the ability to perform operations on child entities without losing the original. In particular, this is usuful when a user would want to filter using a child entity.

The CacheOptional can be created in much the same way as the Java utils Optional:
```
CacheOptional<String, String> maybeValue = CacheOptional.of("Hello, World!!!")
```

The CacheStream, however, is created from an existing stream or a collection:
```
Collection<String> collection = Arrays.asList("value1", "value2", "value3");
CacheStream<String, String> cStreamFromCollection = CacheStream.of(collection);
CacheStream<String, String> cStreamFromStream = CacheStream.of(collection.stream());
```

The CacheOptional and CacheStream expose two new methods:
```
public CacheOptional<VALUE, VALUE> cache();
public CacheOptional<CACHE, CACHE> load();

public CacheStream<VALUE, VALUE> cache();
public CacheStream<CACHE, CACHE> load();
```

The cache() method takes the available value, and saves a reference to it in the cache. Any further mapping/filtering/etc operations performed on the value will no affect the CACHE. The load() method overwrites the available value with the one saved in cache.

```
        CacheStream<String, String> consumableStream =
                CacheStream.of(Arrays.asList("dumb", "bells", "ring", "are", "ya", "listen'n"));

        String max = consumableStream
                .cache()
                .map(String::length)
                .filter(lenght -> lenght > 4)
                .sorted()
                .load()
                .toArray(String[]::new)[0];
                
        System.out.print(max);
        
Output:

        bells
```

The signiture of the classes contain two generics. The first generic denotes the class of the value held in cache. The second generic denotes the class of the available value upon which can be acted.
