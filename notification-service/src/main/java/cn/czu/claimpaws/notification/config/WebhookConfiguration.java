package cn.czu.claimpaws.notification.config;

import cn.czu.claimpaws.notification.application.WebhookSignatureService;
import cn.czu.claimpaws.notification.infrastructure.AesGcmSecretCipher;
import java.util.Base64;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(WebhookProperties.class)
public class WebhookConfiguration {
    @Bean
    AesGcmSecretCipher webhookSecretCipher(WebhookProperties properties) {
        return new AesGcmSecretCipher(Base64.getDecoder().decode(properties.encryptionKey()));
    }

    @Bean
    WebhookSignatureService webhookSignatureService() {
        return new WebhookSignatureService();
    }
}
