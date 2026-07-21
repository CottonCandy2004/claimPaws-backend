package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
class WebhookEndpointServiceTest {
    private final WebhookEndpointService service = new WebhookEndpointService(
            new NoopWebhookConfigMapper(), new AesGcmSecretCipher(new byte[16]), java.net.InetAddress::getAllByName);

    @Test
    void rejectsLoopbackAndPrivateWebhookEndpoints() {
        assertThatThrownBy(() -> service.add("https://127.0.0.1/hooks", "secret"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.add("https://10.0.0.5/hooks", "secret"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.add("https://169.254.169.254/latest/meta-data", "secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsIpv6DocumentationAndReservedEndpoints() {
        assertThatThrownBy(() -> service.add("https://[2001:db8::1]/hooks", "secret"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.add("https://[::1]/hooks", "secret"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static final class NoopWebhookConfigMapper implements WebhookConfigMapper {
        @Override
        public java.util.List<cn.czu.claimpaws.notification.domain.WebhookConfig> findEnabled() {
            return java.util.List.of();
        }

        @Override
        public void insert(String endpointUrl, String encryptedSecret) {
        }
    }
}
