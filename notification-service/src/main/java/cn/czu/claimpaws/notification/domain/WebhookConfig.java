package cn.czu.claimpaws.notification.domain;

public record WebhookConfig(Long id, String endpointUrl, String encryptedSecret, boolean enabled) {
}
