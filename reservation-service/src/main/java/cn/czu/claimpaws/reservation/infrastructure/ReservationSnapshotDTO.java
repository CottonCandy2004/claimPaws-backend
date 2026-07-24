package cn.czu.claimpaws.reservation.infrastructure;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservationSnapshotDTO(
        ResourceInfo resource,
        PolicyInfo policy,
        long version
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ResourceInfo(long id, String name, String type, int capacity, boolean active) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PolicyInfo(
            int slotMinutes, int advanceDays,
            int minDurationMinutes, int maxDurationMinutes,
            boolean requiresApproval, int approvalLevel,
            String description
    ) {}
}
