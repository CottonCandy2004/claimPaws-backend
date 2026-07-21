package cn.czu.claimpaws.notification.application;

import java.time.Duration;

public final class RetrySchedule {
    private final int baseDelaySeconds;

    public RetrySchedule(int baseDelaySeconds) {
        if (baseDelaySeconds < 1) {
            throw new IllegalArgumentException("baseDelaySeconds must be positive");
        }
        this.baseDelaySeconds = baseDelaySeconds;
    }

    public Duration delayAfterFailure(int attempt) {
        long multiplier = 1L << Math.min(Math.max(0, attempt - 1), 20);
        return Duration.ofSeconds(Math.multiplyExact(baseDelaySeconds, multiplier));
    }
}
