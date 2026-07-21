package cn.czu.claimpaws.resource.domain;

public record ReservationSnapshot(Resource resource, ReservationPolicy policy, long version) {
    public static ReservationSnapshot from(Resource resource, ReservationPolicy policy) {
        return new ReservationSnapshot(resource, policy, System.currentTimeMillis());
    }
}
