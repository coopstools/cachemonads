/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

public class UnsortedTestClass {

    private final Integer intValue;
    private final String stringValue;

    public UnsortedTestClass(
            final Integer intValue,
            final String stringValue) {

        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}
