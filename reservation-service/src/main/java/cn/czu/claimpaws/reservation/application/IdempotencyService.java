package cn.czu.claimpaws.reservation.application;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private final ConcurrentHashMap<String, Boolean> processed = new ConcurrentHashMap<>();

    public <T> T execute(long userId, String key, Supplier<T> action) {
        String compositeKey = userId + ":" + key;
        if (processed.containsKey(compositeKey)) {
            throw new RuntimeException("Idempotency key already used");
        }
        T result = action.get();
        processed.put(compositeKey, true);
        return result;
    }
}
