package at.tobiazsh.myworld.traffic_addition.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

/**
 * A simple LRU Cache implementation.
 * @param <C> The type of the cache.
 */
public class LRUCache <C> { // LRU = Least Recently Used

    private final int capacity;
    private final CopyOnWriteArrayList<CacheItem<C>> cache;
    private final String id;

    private static final Map<String, LRUCache> CACHE_REGISTRAR = new HashMap<>();

    public LRUCache(String id, int capacity) {
        this.capacity = capacity;
        this.cache = new CopyOnWriteArrayList<>();

        if (CACHE_REGISTRAR.containsKey(id)) {
            throw new IllegalArgumentException("Cache with id " + id + " already exists and thus cannot be registered!");
        }

        this.id = id;

        registerCache(id, this);
    }

    /**
     * Add or access an item in the cache.
     * @param item The item to add or access.
     */
    public void access(C item) {
        CacheItem<C> exisisting = findItem(item);

        if (exisisting != null) { // Move up if it is already existing
            cache.remove(exisisting);
            cache.addFirst(exisisting);
        } else { // Add new item on first place if not already existing
            cache.addFirst(new CacheItem<>(item));
            if (cache.size() > capacity) {
                cache.removeLast(); // Remove last item (least accessed) if capacity is reached
            }
        }
    }

    private CacheItem<C> findItem(C item) {
        if (!exists(item)) return null;

        return cache.stream()
                .filter(cacheItem -> cacheItem.val.equals(item))
                .toList()
                .getFirst();
    }

    /**
     * Filters the cache based on a predicate.
     * @param predicate The condition to filter items.
     * @return A list of filtered cache items.
     */
    public List<CacheItem<C>> filter(Predicate<? super C> predicate) {
        return cache.stream()
                .filter(item -> predicate.test(item.get()))
                .toList();
    }

    public boolean anyMatch(Predicate<? super C> predicate) {
        return cache.stream().anyMatch(item -> predicate.test(item.get()));
    }

    public boolean exists(C item) {
        return cache.stream().anyMatch(cacheItem -> cacheItem.val.equals(item));
    }

    public C get(C item) {
        if (cache.stream().anyMatch(cacheItem -> cacheItem.val.equals(item)))
            return cache.stream()
                    .filter(cacheItem -> cacheItem.val.equals(item))
                    .toList()
                    .getFirst()
                    .get();

        return null; // Item not found
    }

    public String getId() {
        return this.id;
    }

    public static class CacheItem <O> {
        O val;

        CacheItem(O val) {
            this.val = val;
        }

        public O get() {
            return val;
        }
    }

    public static void registerCache(String id, LRUCache cache) {
        CACHE_REGISTRAR.put(id, cache);
    }

    public static void unregisterCache(String id) {
        CACHE_REGISTRAR.remove(id);
    }

    public static void unregisterCache(LRUCache cache) {
        CACHE_REGISTRAR.entrySet().removeIf(entry -> entry.getValue().equals(cache));
    }

    public static Map<String, LRUCache> getRegisteredCaches() {
        return CACHE_REGISTRAR;
    }

    public static void clearAllCaches() {
        CACHE_REGISTRAR.forEach((s, cache) -> cache.cache.clear());
    }

    public static void clearCache(String id) {
        if (CACHE_REGISTRAR.containsKey(id))
            CACHE_REGISTRAR.get(id).cache.clear();
    }

}
