/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package utils;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.coopsutils.cachemonads.CacheOptional;

public class CachedOptionalTest {

    @Test
    public void testCreationAndGet() throws Exception {

        Optional.empty();

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);

        Assert.assertEquals(value, maybeValue.get());
    }

    @Test
    public void testMap() throws Exception {

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional
                .of(value)
                .map(v -> v + v);

        Assert.assertEquals(value + value, maybeValue.get());
    }

    @Test
    public void testDecache() throws Exception {

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional
                .of(value)
                .map(v -> v + v)
                .load();

        Assert.assertEquals(value, maybeValue.get());
    }

    @Test
    public void testCache() throws Exception {

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional
                .of(value)
                .map(v -> v + v)
                .cache()
                .map(v -> v + v)
                .load();

        Assert.assertEquals(value + value, maybeValue.get());
    }

    @Test
    public void testToEmpty() throws Exception {

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional
                .of(value)
                .map(v -> v + v)
                .cache()
                .map(v -> v + v)
                .load();

        Assert.assertEquals(value + value, maybeValue.get());
    }

    @Test
    public void testGetOneEmprty() {

       try {
           CacheOptional.empty().get();
           Assert.fail("NoSuchElementException not thrown");
       } catch (NoSuchElementException nseEx) {
           System.out.println("success");
       }
    }

    @Test
    public void testNullMap() {

        CacheOptional<String, String> maybeNulled = CacheOptional
                .of("test value")
                .map(v -> null);
        Assert.assertEquals(maybeNulled.hashCode(), CacheOptional.empty().hashCode());
    }

    @Test
    public void testMapNotReferencingNull() {

        CacheOptional<String, Integer> maybeNulled = CacheOptional
                .of("test value")
                .map(v -> (String) null)
                .map(String::length);
    }

    @Test
    public void testOfNull() {

        Assert.assertEquals(
                CacheOptional.empty().hashCode(),
                CacheOptional.ofNullable(null).hashCode());
    }

    @Test
    public void testNullCache() {

        Assert.assertEquals(
                CacheOptional.empty().hashCode(),
                CacheOptional.ofNullable(null).cache().hashCode());
    }

    @Test
    public void testOfNullableOnValue() {

        String value = "value";
        CacheOptional<String, String> maybeValue = CacheOptional.ofNullable(value);

        Assert.assertEquals(value, maybeValue.get());
    }

    @Test
    public void testOfNullableOnNull() {

        String value = null;
        CacheOptional<String, String> maybeValue = CacheOptional.ofNullable(value);

        try {
            Assert.assertEquals(value, maybeValue.get());
            Assert.fail("NoSuchElementException not thrown: filter failed");
        } catch (NoSuchElementException nseEx) {
            System.out.println("success");
        }
    }

    @Test
    public void testNullLoad() {

        Assert.assertEquals(
                CacheOptional.empty().hashCode(),
                CacheOptional.ofNullable(null).load().hashCode());
    }

    @Test
    public void testFilter() {

        String value = "test value";
        CacheOptional.of(value).filter(v -> true).get();

        try {
            CacheOptional.of(value).filter(v -> false).get();
            Assert.fail("NoSuchElementException not thrown: filter failed");
        } catch (NoSuchElementException nseEx) {
            System.out.println("success");
        }
    }

    @Test
    public void testAbsentFilter() {

        try {
            CacheOptional<String, String> definatelyNull = CacheOptional.empty();
            definatelyNull.filter(value -> value.equals("value"));
        } catch (NullPointerException npEx) {
            Assert.fail("Null value check failed in filter");
        }
    }

    @Test
    public void testIsPresentTrue() {

        Assert.assertTrue(CacheOptional.of("test value").isPresent());
    }

    @Test
    public void testIsPresentFalse() {

        Assert.assertFalse(CacheOptional.empty().isPresent());
    }

    @Test
    public void testIfPresentTrue() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);

        Container<Boolean> callCheckContainer = new Container<>(false);
        maybeValue.ifPresent(v -> callCheckContainer.setValue(true));

        Assert.assertTrue(callCheckContainer.getValue());
    }

    @Test
    public void testIfPresentFalse() {

        CacheOptional<String, String> maybeValue = CacheOptional.empty();

        Container<Boolean> callCheckContainer = new Container<>(false);
        maybeValue.ifPresent(v -> callCheckContainer.setValue(true));

        Assert.assertFalse(callCheckContainer.getValue());
    }

    @Test
    public void testOrDoPresent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);

        Container<Boolean> callCheckContainer = new Container<>(false);
        maybeValue.orDo(() -> callCheckContainer.setValue(true));

        Assert.assertFalse(callCheckContainer.getValue());
    }

    @Test
    public void testOrDoAbsent() {

        CacheOptional<String, String> maybeValue = CacheOptional.empty();

        Container<Boolean> callCheckContainer = new Container<>(false);
        maybeValue.orDo(() -> callCheckContainer.setValue(true));

        Assert.assertTrue(callCheckContainer.getValue());
    }

    @Test
    public void testOrElseAbsent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.empty();
        String retrievedValue = maybeValue.orElse(value);

        Assert.assertEquals(value, retrievedValue);
    }

    @Test
    public void testOrElsePresent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);
        String retrievedValue = maybeValue.orElse("new value");

        Assert.assertEquals(value, retrievedValue);
    }

    @Test
    public void testOrElseGetAbsent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.empty();
        String retrievedValue = maybeValue.orElseGet(() -> value);

        Assert.assertEquals(value, retrievedValue);
    }

    @Test
    public void testOrElseGetPresent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);
        String retrievedValue = maybeValue.orElseGet(() -> "new value");

        Assert.assertEquals(value, retrievedValue);
    }

    @Test
    public void testOrElseThrowAbsent() {

        CacheOptional<String, String> maybeValue = CacheOptional.empty();

        try {
            maybeValue.orElseThrow(() -> new RuntimeException(""));
            Assert.fail("Error not thrown like required");
        } catch (RuntimeException rtEx) {
            System.out.println("success");
        }
    }

    @Test
    public void testOrElseThrowPresent() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);

        try {
            maybeValue.orElseThrow(() -> new RuntimeException(""));
            System.out.println("success");
        } catch (RuntimeException rtEx) {
            Assert.fail("Error should not have been thrown");;
        }
    }

    @Test
    public void testOrElseThrowPresentReturnValue() {

        String value = "test value";
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);

        try {
            String returnedValue = maybeValue.orElseThrow(() -> new RuntimeException(""));
            Assert.assertEquals(value, returnedValue);
        } catch (RuntimeException rtEx) {
            Assert.fail("Error should not have been thrown");;
        }
    }

    @Test
    public void testOfMethodForNullValues() {

        String value = null;

        try {
            CacheOptional.of(value);
            Assert.fail("NullPointerException should be thrown when using off on null");
        } catch (NullPointerException npEx) {
            System.out.println("success");
        }
    }

    @Test
    public void testNullMapperInMap() {

        String value = "value";

        try {
            CacheOptional.of(value).map(null);
        } catch (NullPointerException npEx) {
            Assert.assertEquals("Mapping function must not be null", npEx.getMessage());
        }
    }

    @Test
    public void testEqualsOnSameInstance() {

        Assert.assertTrue(Objects.equals(CacheOptional.empty(), CacheOptional.empty()));
        CacheOptional<String, String> maybeValue = CacheOptional.of("value");
        Assert.assertTrue(Objects.equals(maybeValue, maybeValue));
    }

    @Test
    public void testEqualsOnDifferentTypes() {

        String value = "value";
        Assert.assertFalse(Objects.equals(CacheOptional.empty(), value));
        CacheOptional<String, String> maybeValue = CacheOptional.of(value);
        Assert.assertFalse(Objects.equals(maybeValue, value));
    }

    @Test
    public void testEqualsOnDifferentInstancesWithSameValue() {

        CacheOptional<String, String> maybeValue = CacheOptional.of("value");
        CacheOptional<String, String> maybeValue2 = CacheOptional
                .of("value2")
                .cache()
                .map(s -> "value");

        Assert.assertTrue(Objects.equals(maybeValue, maybeValue2));
    }

    @Test
    public void testEmptyToString() {

        Assert.assertEquals("CacheOptional.empty", CacheOptional.empty().toString());
    }

    @Test
    public void testCachedValueToString() {

        String value1 = "value1";
        String value2 = "value2";

        CacheOptional<String, String> maybeValue = CacheOptional
                .of(value1)
                .cache()
                .map(s -> value2);

        String expected = String.format("CacheOptional[%s, %s]", value1, value2);

        Assert.assertEquals(expected, maybeValue.toString());
    }

    @Test
    public void testIfPresentOrElseWhenPresent() {

        String value1 = "original value";
        String presentValue = "new value if present";
        String absentValue = "new value if absent";
        Container<String> testContainer = new Container<>(value1);

        CacheOptional.of(presentValue).ifPresentOrElse(
                testContainer::setValue,
                () -> testContainer.setValue(absentValue));

        Assert.assertEquals(presentValue, testContainer.getValue());
    }

    @Test
    public void testIfPresentOrElseWhenAbsent() {

        String value1 = "original value";
        String presentValue = "new value if present";
        String absentValue = "new value if absent";
        Container<String> testContainer = new Container<>(value1);

        CacheOptional.of(presentValue)
                .map(s -> (String) null)
                .ifPresentOrElse(
                testContainer::setValue,
                () -> testContainer.setValue(absentValue));

        Assert.assertEquals(absentValue, testContainer.getValue());
    }

    @Test
    public void testOrWhenAbsent() {

        String originalValue = "oringial value";
        String defaultValue = "default value";

        CacheOptional<String, String> maybeValue = CacheOptional.empty();

        CacheOptional<String, String> postMaybeValueValue =
                maybeValue.or(() -> CacheOptional.of(defaultValue));

        Assert.assertEquals(defaultValue, postMaybeValueValue.get());
    }

    @Test
    public void testOrWhenPresent() {

        String originalValue = "oringial value";
        String defaultValue = "default value";

        CacheOptional<String, String> maybeValue = CacheOptional.of(originalValue);

        CacheOptional<String, String> postMaybeValueValue =
                maybeValue.or(() -> CacheOptional.of(defaultValue));

        Assert.assertEquals(originalValue, postMaybeValueValue.get());
    }

    private class Container<V> {
        private V value;

        public Container(V value) {
            this.value = value;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }
    }
}
