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

The cache() method takes the available value, and saves a reference to it in the cache. Any further mapping/filtering/etc operations performed on the value will have no affect on the saved/cached value. The load() method overwrites the available value with the one saved in cache.

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

The signiture of the classes contain two generics. The first generic denotes the class of the value held in cache. The second generic denotes the class of the available value upon which mapping/sorting/filtering/etc methods can be acted.

# Full Example
(This example is borrowed from https://stackoverflow.com/questions/32132387/getting-only-required-objects-from-a-list-using-java-8-streams)

There exists a class, Parent, that has a name and a List of children:

```
public class Parent {

    private final String name;
    private List<Child> children;

    public Parent(String name) {
        this.name = name;
    }
    
    ...
    //Getters, setters, equals(), and hashCode()
    ...
}
```

The class child is defined by a single integer attribute, attribute1:
```
public class Child {

    private final Integer attribute1;

    public Child(Integer attribute1) {
        this.attribute1 = attribute1;
    }

    public Integer getAttribute1() {
        return attribute1;
    }
}
```

Given a group of Parents, each with some number of children, we want to find which parents have a child with an attribute greater than 10. This can be found using the following method:

```
        List<Parent> goodParents = CacheStream.of(Arrays.asList(parent1, parent2, parent3))
                .cache() //A reference to parent is saved in cache
                .map(Parent::getChildren) //The visible value is mapped Parent -> Child; but the cached reference to parents is preserved
                .flatMap(Collection::stream)
                .map(Child::getAttribute1)
                .filter(att -> att > 10)
                .load() //The parents are pulled out of cache, and overide the visible value
                .distinct()
                .collect(Collectors.toList());
```

The list, goodParents, now only contains parents with at least one child who has an attribute above 10.
