package cn.czu.claimpaws.notification.infrastructure;

import cn.czu.claimpaws.notification.application.PinnedWebhookEndpoint;

import java.io.IOException;

public interface WebhookHttpClient {
    WebhookHttpResponse post(PinnedWebhookEndpoint endpoint, String eventId, String eventType, String timestamp,
                             String signature, String payload) throws IOException, InterruptedException;
}
