package cn.czu.claimpaws.notification.application;

import java.net.InetAddress;
import java.net.URI;

/** A delivery target whose address was validated during admission and must not be resolved again. */
public record PinnedWebhookEndpoint(URI uri, InetAddress address) {
    public String host() {
        return uri.getHost();
    }

    public int port() {
        return uri.getPort() == -1 ? 443 : uri.getPort();
    }
}
