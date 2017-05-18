package com.coopstools.examples;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.coopstools.Child;
import com.coopstools.Parent;
import com.coopstools.cachemonads.CacheStream;

public class ParentChildExample {

    @Test
    public void demoStandardOperation() {

        Parent parent1 = new Parent("parent1");
        parent1.setChildren(Arrays.asList(new Child(4), new Child(11)));
        Parent parent2 = new Parent("parent2");
        parent2.setChildren(Arrays.asList(new Child(3), new Child(6)));
        Parent parent3 = new Parent("parent3");
        parent3.setChildren(Arrays.asList(new Child(12), new Child(16)));

        List<Parent> parents = CacheStream.of(Arrays.asList(parent1, parent2, parent3))
                .cache()
                .map(Parent::getChildren)
                .flatMap(Collection::stream)
                .map(Child::getAttribute1)
                .filter(att -> att > 10)
                .load()
                .distinct()
                .collect(Collectors.toList());

        assertEquals(2, parents.size());
    }
}
