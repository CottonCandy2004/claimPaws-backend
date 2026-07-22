package cn.czu.claimpaws.notification.persistence;

import cn.czu.claimpaws.notification.domain.WebhookConfig;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface WebhookConfigMapper {
    List<WebhookConfig> findEnabled();

    void insert(@Param("endpointUrl") String endpointUrl, @Param("encryptedSecret") String encryptedSecret,
                @Param("enabled") boolean enabled);

    List<WebhookConfig> findPage(@Param("offset") int offset, @Param("limit") int limit);

    long count();

    WebhookConfig findById(@Param("id") long id);

    int update(@Param("endpointUrl") String endpointUrl, @Param("encryptedSecret") String encryptedSecret,
               @Param("enabled") boolean enabled, @Param("id") long id);

    int deleteById(@Param("id") long id);
}
