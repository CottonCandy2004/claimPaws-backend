package cn.czu.claimpaws.notification.application;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookSignatureServiceTest {

    @Test
    void signsTimestampAndPayloadWithHmacSha256() {
        var service = new WebhookSignatureService();

        assertThat(service.sign("secret", "2026-07-21T00:00:00Z", "{\"id\":1}"))
                .isEqualTo("b88a9959670d03d53241048aef56033eb9f6a52cd7f739e9138e93bcddf11c8d");
    }
}
