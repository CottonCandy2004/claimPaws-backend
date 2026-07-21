package cn.czu.claimpaws.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "notification.webhook")
public record WebhookProperties(boolean enabled, int connectTimeout, int readTimeout,
                                int maxRetries, int retryBaseSeconds, String encryptionKey) {
    public WebhookProperties {
        if (connectTimeout < 1 || readTimeout < 1 || maxRetries < 1 || retryBaseSeconds < 1) {
            throw new IllegalArgumentException("Webhook timeout and retry settings must be positive");
        }
    }
}
