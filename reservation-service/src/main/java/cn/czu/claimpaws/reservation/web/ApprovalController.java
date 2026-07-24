package cn.czu.claimpaws.reservation.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.reservation.domain.Reservation;
import cn.czu.claimpaws.reservation.domain.ReservationStatus;
import cn.czu.claimpaws.reservation.domain.ReservationView;
import cn.czu.claimpaws.reservation.persistence.ReservationMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/approvals")
public class ApprovalController {

    private final ReservationMapper reservationMapper;

    public ApprovalController(ReservationMapper reservationMapper) {
        this.reservationMapper = reservationMapper;
    }

    @GetMapping
    public ApiResponse<PageResponse<ReservationView>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-User-Roles", required = false) String userRoles,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        int offset = (page - 1) * size;
        List<Reservation> all = reservationMapper.findPendingApprovals(0, Integer.MAX_VALUE);
        // Filter: user's roles must contain the next approver role
        String[] roles = userRoles != null ? userRoles.split(",") : new String[0];
        List<Reservation> filtered = all.stream()
                .filter(r -> {
                    String[] chain = r.approverRoles().split(",");
                    int level = r.approvedLevels();
                    if (level >= chain.length || chain[0].isEmpty()) return true; // no roles → anyone can approve
                    String nextRole = chain[level].trim();
                    for (String ur : roles) {
                        if (ur.trim().equals(nextRole)) return true;
                    }
                    if ("admin".equals(userRoles)) return true; // admin can always approve
                    return false;
                })
                .toList();
        int total = filtered.size();
        int end = Math.min(offset + size, total);
        List<Reservation> pageList = filtered.subList(Math.min(offset, total), end);
        List<ReservationView> views = pageList.stream().map(r -> ReservationView.from(r, 0)).toList();
        return ApiResponse.success(new PageResponse<>(views, page, size, total), requestId);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<ReservationView> approve(
            @PathVariable long id,
            @RequestBody ApproveRequest body,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            return ApiResponse.failure("NOT_FOUND", "Reservation not found", requestId);
        }
        if (!body.approved) {
            reservationMapper.updateStatus(id, ReservationStatus.REJECTED.name());
            return ApiResponse.success(ReservationView.from(reservationMapper.findById(id), 0), requestId);
        }

        String[] roles = reservation.approverRoles().split(",");
        int nextLevel = reservation.approvedLevels() + 1;
        if (nextLevel >= roles.length) {
            reservationMapper.updateStatus(id, ReservationStatus.CONFIRMED.name());
            reservationMapper.updateApprovedLevels(id, nextLevel);
        } else {
            reservationMapper.updateApprovedLevels(id, nextLevel);
        }
        Reservation updated = reservationMapper.findById(id);
        return ApiResponse.success(ReservationView.from(updated, 0), requestId);
    }

    record ApproveRequest(boolean approved, String comment) {}
}
