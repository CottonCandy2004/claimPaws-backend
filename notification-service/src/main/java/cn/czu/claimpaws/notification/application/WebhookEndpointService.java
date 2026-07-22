package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.URI;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookEndpointService {
    private final WebhookConfigMapper webhookConfigMapper;
    private final AesGcmSecretCipher secretCipher;
    private final HostResolver hostResolver;

    public WebhookEndpointService(WebhookConfigMapper webhookConfigMapper, AesGcmSecretCipher secretCipher,
                                  HostResolver hostResolver) {
        this.webhookConfigMapper = webhookConfigMapper;
        this.secretCipher = secretCipher;
        this.hostResolver = hostResolver;
    }

    @Transactional
    public void add(String endpointUrl, String secret) {
        URI endpoint = validateUri(endpointUrl);
        validateAddresses(endpoint);
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Webhook endpoint must be HTTPS and secret is required");
        }
        webhookConfigMapper.insert(endpoint.toString(), secretCipher.encrypt(secret), true);
    }

    /**
     * Resolves and validates the hostname once. The returned address is passed to the TLS client,
     * preventing a second DNS answer from redirecting a delivery to a private network.
     */
    public PinnedWebhookEndpoint resolveForDelivery(String endpointUrl) {
        URI endpoint = validateUri(endpointUrl);
        InetAddress[] addresses = validateAddresses(endpoint);
        return new PinnedWebhookEndpoint(endpoint, addresses[0]);
    }

    private URI validateUri(String endpointUrl) {
        URI endpoint;
        try {
            endpoint = URI.create(endpointUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Webhook endpoint must be a valid HTTPS URL", exception);
        }
        if (!"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getHost() == null
                || endpoint.getUserInfo() != null || endpoint.getPort() > 65535) {
            throw new IllegalArgumentException("Webhook endpoint must be a valid HTTPS URL");
        }
        return endpoint;
    }

    private InetAddress[] validateAddresses(URI endpoint) {
        try {
            InetAddress[] addresses = hostResolver.resolve(endpoint.getHost());
            if (addresses.length == 0 || Arrays.stream(addresses).anyMatch(this::isBlockedAddress)) {
                throw new IllegalArgumentException("Webhook endpoint resolves to a blocked address");
            }
            return addresses;
        } catch (UnknownHostException exception) {
            throw new IllegalArgumentException("Webhook endpoint host cannot be resolved", exception);
        }
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return true;
        }
        byte[] bytes = address.getAddress();
        if (address instanceof Inet4Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            return first == 0 || first >= 224
                    || first == 10
                    || (first == 100 && second >= 64 && second <= 127)
                    || (first == 127)
                    || (first == 169 && second == 254)
                    || (first == 172 && second >= 16 && second <= 31)
                    || (first == 192 && (second == 0 || second == 168))
                    || (first == 198 && (second == 18 || second == 19))
                    || (first == 192 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 2)
                    || (first == 198 && second == 51 && Byte.toUnsignedInt(bytes[2]) == 100)
                    || (first == 203 && second == 0 && Byte.toUnsignedInt(bytes[2]) == 113);
        }
        if (address instanceof Inet6Address) {
            int first = Byte.toUnsignedInt(bytes[0]);
            int second = Byte.toUnsignedInt(bytes[1]);
            // ::/128, ::1/128, IPv4-mapped, IPv4-compatible, link-local, ULA,
            // documentation, 6to4, Teredo and multicast/reserved ranges.
            return (first == 0 && second == 0)
                    || (first == 0 && second == 1)
                    || (first == 0 && second == 0 && bytes[10] == (byte) 0xff && bytes[11] == (byte) 0xff)
                    || (first & 0xfe) == 0xfc
                    || (first == 0xfe && (second & 0xc0) == 0x80)
                    || (first == 0x20 && second == 0x01 && Byte.toUnsignedInt(bytes[2]) == 0x0d && Byte.toUnsignedInt(bytes[3]) == 0xb8)
                    || (first == 0x20 && second == 0x02)
                    || (first == 0x20 && second == 0x01 && bytes[2] == 0x00 && bytes[3] == 0x00)
                    || first == 0xff;
        }
        return true;
    }
}
