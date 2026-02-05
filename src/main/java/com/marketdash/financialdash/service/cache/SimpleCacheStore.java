package com.marketdash.financialdash.service.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCacheStore {

    private final Map<String, CachedValue<?>> store = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        CachedValue<?> cached = store.get(key);
        if (cached == null || cached.isExpired()) {
            store.remove(key);
            return null;
        }
        return (T) cached.getValue();
    }

    public void put(String key, Object value, long ttlMillis) {
        store.put(key, new CachedValue<>(value, ttlMillis));
    }
}