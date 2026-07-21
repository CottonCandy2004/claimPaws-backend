package cn.czu.claimpaws.reservation.infrastructure;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {
    public void publish(OutboxMessage message) {
    }
}
