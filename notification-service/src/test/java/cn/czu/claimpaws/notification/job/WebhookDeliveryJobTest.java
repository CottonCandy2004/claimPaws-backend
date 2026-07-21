package cn.czu.claimpaws.notification.job;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookDeliveryJobTest {
    @Test
    void doesNotHoldADatabaseTransactionWhileMakingWebhookRequests() throws Exception {
        assertThat(WebhookDeliveryJob.class.getMethod("deliverDue").isAnnotationPresent(Transactional.class)).isFalse();
    }
}
