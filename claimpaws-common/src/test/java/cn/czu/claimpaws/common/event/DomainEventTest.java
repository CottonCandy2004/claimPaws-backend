package cn.czu.claimpaws.common.event;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DomainEventTest {

    @Test
    void requiresEventIdentityAndType() {
        assertThatThrownBy(() -> new DomainEvent(
                null,
                "",
                Instant.now(),
                1L,
                1,
                JsonNodeFactory.instance.objectNode()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void defensivelyCopiesPayloadOnConstructionAndAccess() {
        ObjectNode sourcePayload = JsonNodeFactory.instance.objectNode().put("status", "created");
        DomainEvent event = new DomainEvent(
                UUID.randomUUID(),
                "reservation.created",
                Instant.now(),
                1L,
                1,
                sourcePayload);

        sourcePayload.put("status", "tampered");
        assertThat(event.payload().path("status").asText()).isEqualTo("created");

        ((ObjectNode) event.payload()).put("status", "mutated-through-accessor");
        assertThat(event.payload().path("status").asText()).isEqualTo("created");
    }
}
