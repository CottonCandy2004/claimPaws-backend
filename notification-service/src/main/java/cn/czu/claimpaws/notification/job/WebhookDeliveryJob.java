package cn.czu.claimpaws.notification.job;

import cn.czu.claimpaws.notification.application.RetrySchedule;
import cn.czu.claimpaws.notification.application.WebhookEndpointService;
import cn.czu.claimpaws.notification.application.WebhookDeliveryService;
import cn.czu.claimpaws.notification.application.WebhookSignatureService;
import cn.czu.claimpaws.notification.config.WebhookProperties;
import cn.czu.claimpaws.notification.domain.WebhookDeliveryTask;
import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import cn.czu.claimpaws.notification.infrastructure.WebhookHttpClient;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WebhookDeliveryJob {
    private final WebhookHttpClient httpClient;
    private final AesGcmSecretCipher secretCipher;
    private final WebhookSignatureService signatureService;
    private final WebhookProperties properties;
    private final RetrySchedule retrySchedule;
    private final WebhookEndpointService webhookEndpointService;
    private final WebhookDeliveryService webhookDeliveryService;

    public WebhookDeliveryJob(WebhookHttpClient httpClient, AesGcmSecretCipher secretCipher,
                              WebhookSignatureService signatureService, WebhookProperties properties,
                              WebhookEndpointService webhookEndpointService,
                              WebhookDeliveryService webhookDeliveryService) {
        this.httpClient = httpClient;
        this.secretCipher = secretCipher;
        this.signatureService = signatureService;
        this.properties = properties;
        this.retrySchedule = new RetrySchedule(properties.retryBaseSeconds());
        this.webhookEndpointService = webhookEndpointService;
        this.webhookDeliveryService = webhookDeliveryService;
    }

    @Scheduled(fixedDelayString = "${notification.webhook.poll-interval-ms:1000}")
    public void deliverDue() {
        if (!properties.enabled()) return;
        webhookDeliveryService.claimDue(LocalDateTime.now(ZoneOffset.UTC), 100).forEach(this::deliver);
    }

    void deliver(WebhookDeliveryTask task) {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String timestamp = now.toInstant(ZoneOffset.UTC).toString();
        try {
            var endpoint = webhookEndpointService.resolveForDelivery(task.endpointUrl());
            String secret = task.encryptedSecret();
            String signature = secret != null && !secret.isEmpty()
                    ? signatureService.sign(secretCipher.decrypt(secret), timestamp, task.payload())
                    : "";
            var response = httpClient.post(endpoint, task.eventId(), task.eventType(), timestamp,
                    signature, task.payload());
            if (response.isSuccessful()) {
                webhookDeliveryService.markSucceeded(task.id(), now, response.statusCode());
            } else {
                recordFailure(task, now, response.statusCode(), "HTTP " + response.statusCode());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            recordFailure(task, now, null, "Webhook request interrupted");
        } catch (Exception exception) {
            recordFailure(task, now, null, exception.getClass().getSimpleName());
        }
    }

    private void recordFailure(WebhookDeliveryTask task, LocalDateTime now, Integer status, String reason) {
        int attempts = task.retryCount() + 1;
        if (attempts >= properties.maxRetries()) {
            webhookDeliveryService.markFailed(task.id(), attempts, now, status, reason);
            return;
        }
        webhookDeliveryService.markRetry(task.id(), attempts, now,
                now.plus(retrySchedule.delayAfterFailure(attempts)), status, reason);
    }
}
