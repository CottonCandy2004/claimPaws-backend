package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Collections;
import java.util.function.Supplier;

@Component
public class ReservationLock {
    private static final DefaultRedisScript<Long> RELEASE_IF_OWNER = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);
    private final StringRedisTemplate redis;

    public ReservationLock(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public <T> T withLock(CreateReservationCommand command, int slotMinutes, Supplier<T> action) {
        if (slotMinutes <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED);
        }
        String owner = UUID.randomUUID().toString();
        List<String> keys = slotKeys(command, slotMinutes);
        boolean releaseAfterTransaction = false;
        try {
            for (String key : keys) {
                if (!Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, owner, Duration.ofSeconds(30)))) {
                    throw new BusinessException(ErrorCode.RESERVATION_TIME_CONFLICT);
                }
            }
            T result = action.get();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                releaseAfterTransaction = true;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        release(keys, owner);
                    }
                });
            }
            return result;
        } finally {
            if (!releaseAfterTransaction) {
                release(keys, owner);
            }
        }
    }

    private void release(List<String> keys, String owner) {
        for (String key : keys) {
            redis.execute(RELEASE_IF_OWNER, Collections.singletonList(key), owner);
        }
    }

    private List<String> slotKeys(CreateReservationCommand command, int slotMinutes) {
        long seconds = Duration.ofMinutes(slotMinutes).toSeconds();
        long cursor = Math.floorDiv(command.startAt().getEpochSecond(), seconds) * seconds;
        long end = command.endAt().getEpochSecond();
        List<String> keys = new ArrayList<>();
        while (cursor < end) {
            keys.add("reservation:lock:" + command.resourceId() + ':' + cursor);
            cursor += seconds;
        }
        return keys;
    }
}
