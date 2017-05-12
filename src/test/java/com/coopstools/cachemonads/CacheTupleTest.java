package com.coopstools.cachemonads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import javafx.util.Pair;

import org.junit.Test;

public class CacheTupleTest {

    @Test
    public void testNullComparison() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);

        assertFalse(firstValue.equals(null));
    }

    @Test
    public void testWrongContainer() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        Pair<Double, Double> secondValue = new Pair<>(5.0, 10.9);

        assertFalse(firstValue.equals(secondValue));
    }

    @Test
    public void testWrongValueType() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        CacheTuple<Double, Integer> secondValue = new CacheTuple<>(5.0, 2);

        assertFalse(firstValue.equals(secondValue));
    }

    @Test
    public void testSameTypeDifferentValue() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        CacheTuple<Double, Double> secondValue = new CacheTuple<>(5.0, 10.9);

        assertFalse(firstValue.equals(secondValue));
    }

    @Test
    public void testSameValueDifferentContainer() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        CacheTuple<Integer, Double> secondValue = new CacheTuple<>(5, 8.9);

        assertTrue(firstValue.equals(secondValue));
    }

    @Test
    public void testHasOnSameValue() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        CacheTuple<Integer, Double> secondValue = new CacheTuple<>(5, 8.9);

        assertEquals(firstValue.hashCode(), secondValue.hashCode());
    }

    @Test
    public void testHasOnSifferentValue() {

        CacheTuple<Double, Double> firstValue = new CacheTuple<>(3.0, 8.9);
        CacheTuple<Integer, Double> secondValue = new CacheTuple<>(5, 9.8);

        assertNotEquals(firstValue.hashCode(), secondValue.hashCode());
    }
}
