/*
 * Copyright (C) 2016 by Amobee Inc.
 * All Rights Reserved.
 */
package com.coopsutils.cachemonads;

class CacheTuple<LEFT_CACHE, RIGHT_VALUE> {

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
}
