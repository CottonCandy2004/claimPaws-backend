package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import java.net.InetAddress;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookEndpointPinningTest {
    @Test
    void resolvesDeliveryEndpointOnceAndReturnsTheValidatedAddressForTheCallerToConnect() throws Exception {
        InetAddress publicAddress = InetAddress.getByName("93.184.216.34");
        WebhookEndpointService service = new WebhookEndpointService(new NoopWebhookConfigMapper(),
                new AesGcmSecretCipher(new byte[16]), host -> new InetAddress[]{publicAddress});

        PinnedWebhookEndpoint endpoint = service.resolveForDelivery("https://example.com/hooks");

        assertThat(endpoint.host()).isEqualTo("example.com");
        assertThat(endpoint.address()).isEqualTo(publicAddress);
    }

    private static final class NoopWebhookConfigMapper implements WebhookConfigMapper {
        @Override public java.util.List<cn.czu.claimpaws.notification.domain.WebhookConfig> findEnabled() { return java.util.List.of(); }
        @Override public void insert(String endpointUrl, String encryptedSecret) { }
    }
}
