package cn.czu.claimpaws.notification.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import cn.czu.claimpaws.notification.domain.WebhookConfig;
import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import cn.czu.claimpaws.notification.persistence.DeliveryMapper;
import cn.czu.claimpaws.notification.persistence.WebhookConfigMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks")
public class WebhookController {

    private final WebhookConfigMapper webhookConfigMapper;
    private final DeliveryMapper deliveryMapper;
    private final AesGcmSecretCipher secretCipher;

    public WebhookController(WebhookConfigMapper webhookConfigMapper, DeliveryMapper deliveryMapper,
                             AesGcmSecretCipher secretCipher) {
        this.webhookConfigMapper = webhookConfigMapper;
        this.deliveryMapper = deliveryMapper;
        this.secretCipher = secretCipher;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<WebhookConfig> configs = webhookConfigMapper.findPage(offset, size);
        long total = webhookConfigMapper.count();
        List<Map<String, Object>> items = configs.stream().map(this::toFrontendMap).toList();
        return ApiResponse.success(Map.of("records", items, "total", total, "page", page, "size", size), requestId);
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> get(@PathVariable long id,
                                                 @RequestHeader("X-Request-Id") String requestId) {
        WebhookConfig config = webhookConfigMapper.findById(id);
        if (config == null) {
            return ApiResponse.failure("WEBHOOK_NOT_FOUND", "Webhook config not found", requestId);
        }
        return ApiResponse.success(toFrontendMap(config), requestId);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body,
                                                    @RequestHeader("X-Request-Id") String requestId) {
        String url = (String) body.get("url");
        String secret = (String) body.get("secret");
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : true;

        if (url == null || url.isBlank()) {
            return ApiResponse.failure("INVALID_REQUEST", "url is required", requestId);
        }
        String encrypted = secret != null && !secret.isBlank() ? secretCipher.encrypt(secret) : "";
        webhookConfigMapper.insert(url, encrypted, enabled);
        return ApiResponse.success(Map.of("message", "Webhook created"), requestId);
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable long id,
                                                    @RequestBody Map<String, Object> body,
                                                    @RequestHeader("X-Request-Id") String requestId) {
        WebhookConfig existing = webhookConfigMapper.findById(id);
        if (existing == null) {
            return ApiResponse.failure("WEBHOOK_NOT_FOUND", "Webhook config not found", requestId);
        }

        String url = body.containsKey("url") ? (String) body.get("url") : existing.endpointUrl();
        String secret = body.containsKey("secret") ? (String) body.get("secret") : null;
        Boolean enabled = body.containsKey("enabled") ? (Boolean) body.get("enabled") : existing.enabled();

        if (url == null || url.isBlank()) {
            return ApiResponse.failure("INVALID_REQUEST", "url is required", requestId);
        }

        String encryptedSecret;
        if (secret != null && !secret.isBlank()) {
            encryptedSecret = secretCipher.encrypt(secret);
        } else {
            encryptedSecret = existing.encryptedSecret();
        }

        webhookConfigMapper.update(url, encryptedSecret, enabled != null ? enabled : true, id);
        return ApiResponse.success(Map.of("message", "Webhook updated"), requestId);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, Object>> delete(@PathVariable long id,
                                                    @RequestHeader("X-Request-Id") String requestId) {
        int rows = webhookConfigMapper.deleteById(id);
        if (rows == 0) {
            return ApiResponse.failure("WEBHOOK_NOT_FOUND", "Webhook config not found", requestId);
        }
        return ApiResponse.success(Map.of("message", "Webhook deleted"), requestId);
    }

    @PostMapping("/{id}/test")
    public ApiResponse<Map<String, Object>> testDelivery(@PathVariable long id,
                                                          @RequestHeader("X-Request-Id") String requestId) {
        WebhookConfig config = webhookConfigMapper.findById(id);
        if (config == null) {
            return ApiResponse.failure("WEBHOOK_NOT_FOUND", "Webhook config not found", requestId);
        }

        String testPayload = "{\"type\":\"test.delivery\",\"message\":\"This is a test delivery from ClaimPaws\"}";
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        NotificationDelivery delivery = new NotificationDelivery(
                null, config.id(), UUID.randomUUID().toString(), "test.delivery",
                testPayload, config.endpointUrl(), "PENDING", 0, now, now, null, null, null, null);
        deliveryMapper.insert(delivery);
        return ApiResponse.success(Map.of("message", "Test delivery queued"), requestId);
    }

    @GetMapping("/delivery-audits")
    public ApiResponse<Map<String, Object>> listAudits(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long webhookId,
            @RequestParam(required = false) String status,
            @RequestHeader("X-Request-Id") String requestId) {
        int offset = (page - 1) * size;
        List<Map<String, Object>> items = deliveryMapper.findAuditPage(offset, size, webhookId, status);
        long total = deliveryMapper.countAudits(webhookId, status);
        return ApiResponse.success(Map.of("records", items, "total", total, "page", page, "size", size), requestId);
    }

    @PostMapping("/delivery-audits/{id}/retry")
    public ApiResponse<Map<String, Object>> retryDelivery(@PathVariable long id,
                                                           @RequestHeader("X-Request-Id") String requestId) {
        int rows = deliveryMapper.resetForRetry(id);
        if (rows == 0) {
            return ApiResponse.failure("DELIVERY_NOT_FOUND", "Delivery audit not found", requestId);
        }
        return ApiResponse.success(Map.of("message", "Delivery retry queued"), requestId);
    }

    private Map<String, Object> toFrontendMap(WebhookConfig config) {
        return Map.of(
                "id", config.id(),
                "name", "Webhook-" + config.id(),
                "url", config.endpointUrl(),
                "secret", config.encryptedSecret(),
                "events", List.of(),
                "enabled", config.enabled(),
                "maxRetries", 3,
                "connectTimeout", 5,
                "readTimeout", 30
        );
    }
}
