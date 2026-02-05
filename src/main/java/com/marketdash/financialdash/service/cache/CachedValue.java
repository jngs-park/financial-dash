package com.marketdash.financialdash.service.cache;

public class CachedValue<T> {

    private final T value;
    private final long expireAt;

    public CachedValue(T value, long ttlMillis) {
        this.value = value;
        this.expireAt = System.currentTimeMillis() + ttlMillis;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expireAt;
    }

    public T getValue() {
        return value;
    }
}