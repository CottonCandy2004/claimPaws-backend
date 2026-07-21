package cn.czu.claimpaws.reservation.job;

import cn.czu.claimpaws.reservation.domain.OutboxMessage;
import cn.czu.claimpaws.reservation.infrastructure.OutboxPublisher;
import cn.czu.claimpaws.reservation.persistence.OutboxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class OutboxPublishJob {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublishJob.class);

    private final OutboxMapper outboxMapper;
    private final OutboxPublisher outboxPublisher;

    public OutboxPublishJob(OutboxMapper outboxMapper, OutboxPublisher outboxPublisher) {
        this.outboxMapper = outboxMapper;
        this.outboxPublisher = outboxPublisher;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPending() {
        String owner = UUID.randomUUID().toString();
        for (OutboxMessage message : outboxMapper.findClaimCandidates()) {
            if (outboxMapper.claim(message.id(), owner, LocalDateTime.now().plusSeconds(30)) != 1) {
                continue;
            }
            try {
                outboxPublisher.publish(message);
                outboxMapper.updateStatus(message.id(), owner, "PUBLISHED");
                log.debug("Published outbox message {}", message.eventId());
            } catch (Exception e) {
                outboxMapper.releaseClaim(message.id(), owner);
                log.error("Failed to publish outbox message {}", message.eventId(), e);
            }
        }
    }
}
