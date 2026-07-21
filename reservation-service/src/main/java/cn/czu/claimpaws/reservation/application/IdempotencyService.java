package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import cn.czu.claimpaws.reservation.domain.ReservationView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private static final String PROCESSING = "PROCESSING";
    private static final Duration PROCESSING_TTL = Duration.ofMinutes(2);
    private static final Duration RESULT_TTL = Duration.ofDays(1);
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public IdempotencyService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    public ReservationView execute(long userId, String key, Supplier<ReservationView> action) {
        String redisKey = "idempotency:reservation:" + userId + ':' + key;
        if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(redisKey, PROCESSING, PROCESSING_TTL))) {
            return awaitCompletedResult(redisKey);
        }
        try {
            ReservationView result = action.get();
            publishResultAfterCommit(redisKey, result);
            return result;
        } catch (RuntimeException exception) {
            redis.delete(redisKey);
            throw exception;
        }
    }

    private ReservationView awaitCompletedResult(String redisKey) {
        for (int attempt = 0; attempt < 50; attempt++) {
            String stored = redis.opsForValue().get(redisKey);
            if (stored != null && !PROCESSING.equals(stored)) {
                try {
                    return objectMapper.readValue(stored, ReservationView.class);
                } catch (JsonProcessingException exception) {
                    throw new IllegalStateException("Stored idempotency response is invalid", exception);
                }
            }
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new BusinessException(ErrorCode.IDEMPOTENCY_KEY_REUSED);
    }

    private void publishResultAfterCommit(String redisKey, ReservationView result) {
        String serialized;
        try {
            serialized = objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException exception) {
            redis.delete(redisKey);
            throw new IllegalStateException("Unable to serialize idempotency response", exception);
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            redis.opsForValue().set(redisKey, serialized, RESULT_TTL);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redis.opsForValue().set(redisKey, serialized, RESULT_TTL);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    redis.delete(redisKey);
                }
            }
        });
    }
}
