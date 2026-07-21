package cn.czu.claimpaws.notification.application;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RetryScheduleTest {

    @Test
    void doublesDelayForEachFailedAttempt() {
        var schedule = new RetrySchedule(5);

        assertThat(schedule.delayAfterFailure(1)).isEqualTo(Duration.ofSeconds(5));
        assertThat(schedule.delayAfterFailure(3)).isEqualTo(Duration.ofSeconds(20));
    }
}
