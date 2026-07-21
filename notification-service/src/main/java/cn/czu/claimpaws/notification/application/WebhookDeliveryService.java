package cn.czu.claimpaws.notification.application;

import cn.czu.claimpaws.common.event.DomainEvent;
import cn.czu.claimpaws.notification.domain.NotificationDelivery;
import cn.czu.claimpaws.notification.persistence.DeliveryMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class WebhookDeliveryService {

    private final DeliveryMapper deliveryMapper;
    private final String webhookSecret;

    public WebhookDeliveryService(DeliveryMapper deliveryMapper,
                                  @Value("${webhook.secret:changeit}") String webhookSecret) {
        this.deliveryMapper = deliveryMapper;
        this.webhookSecret = webhookSecret;
    }

    @Transactional
    public void createDeliveries(DomainEvent event) {
        String payload = event.payload().toString();
        String signature = sign(payload);
        NotificationDelivery delivery = new NotificationDelivery(
                null,
                event.eventId().toString(),
                event.eventType(),
                payload,
                signature,
                "PENDING",
                0,
                null,
                null,
                null
        );
        deliveryMapper.insert(delivery);
    }

    String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("HMAC signing failed", e);
        }
    }
}
