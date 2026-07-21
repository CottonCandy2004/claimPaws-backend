package cn.czu.claimpaws.notification.persistence;

import cn.czu.claimpaws.notification.domain.WebhookConfig;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WebhookConfigMapper {
    List<WebhookConfig> findEnabled();

    void insert(@Param("endpointUrl") String endpointUrl, @Param("encryptedSecret") String encryptedSecret);
}
