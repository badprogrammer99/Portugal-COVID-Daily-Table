package com.gmail.etpr99.jose.coviddailytable.models;

import java.util.concurrent.TimeUnit;

public class ExpiringObject<T> {
    private T obj;
    private final long expirationTime;

    public ExpiringObject(T obj, long expirationTime, TimeUnit unit) {
        this.obj = obj;
        this.expirationTime = System.nanoTime() + unit.toNanos(expirationTime);
    }

    public T get() {
        if (expirationTime == 0 || System.nanoTime() - expirationTime >= 0) {
            obj = null;
        }

        return obj;
    }
}
