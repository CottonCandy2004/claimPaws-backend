package cn.czu.claimpaws.resource;

import cn.czu.claimpaws.resource.application.ResourceService;
import cn.czu.claimpaws.resource.domain.ReservationSnapshot;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Rollback
@Sql("/data/test-data.sql")
class ReservationPolicyServiceIT {

    @Autowired
    private ResourceService resourceService;

    @Test
    void returnsActivePolicySnapshotForReservableResource() {
        long resourceId = 1L;
        ReservationSnapshot snapshot = resourceService.snapshot(resourceId);
        assertThat(snapshot.policy().slotMinutes()).isEqualTo(30);
    }

    @Test
    void snapshotForNonExistentResourceThrowsException() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.snapshot(999L)
        );
    }

    @Test
    void snapshotForResourceWithNoPolicyThrowsException() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> resourceService.snapshot(2L)
        );
    }
}
