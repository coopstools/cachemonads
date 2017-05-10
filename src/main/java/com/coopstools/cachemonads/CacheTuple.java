/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopstools.cachemonads;

import java.util.Objects;

class CacheTuple<LEFT_CACHE, RIGHT_VALUE> implements Comparable<CacheTuple<LEFT_CACHE, RIGHT_VALUE>> {

    private final LEFT_CACHE left;
    private final RIGHT_VALUE right;

    public CacheTuple(LEFT_CACHE left, RIGHT_VALUE right) {

        this.left = left;
        this.right = right;
    }

    public LEFT_CACHE getLeft() {
        return left;
    }

    public RIGHT_VALUE getRight() {
        return right;
    }

    @Override
    public int compareTo(CacheTuple<LEFT_CACHE, RIGHT_VALUE> obj) {

        if (!(right instanceof Comparable))
            throw new ClassCastException("Class does not implement comparable: " + right.getClass());

        return ((Comparable<RIGHT_VALUE>) right).compareTo(obj.right);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null)
            return false;

        if (!(obj instanceof CacheTuple))
            return false;

        Object rightObjValue = ((CacheTuple) obj).getRight();
        return right.getClass().isInstance(rightObjValue) &&
                Objects.equals(right, rightObjValue);

    }

    @Override
    public int hashCode() {
        return right.hashCode();
    }
}
