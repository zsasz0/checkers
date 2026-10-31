package main.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ApplicationContext {

    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> factories = new HashMap<>();

    // Register a singleton instance
    public <T> void registerSingleton(Class<T> type, T instance) {
        singletons.put(type, instance);
    }

    // Register a factory for prototypes
    public <T> void registerFactory(Class<T> type, Supplier<T> factory) {
        factories.put(type, factory);
    }

    // Resolve a bean by type
    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> type) {
        if (singletons.containsKey(type)) {
            return (T) singletons.get(type);
        }
        if (factories.containsKey(type)) {
            return (T) factories.get(type).get();
        }
        throw new IllegalArgumentException("No bean registered for type: " + type.getSimpleName());
    }
}