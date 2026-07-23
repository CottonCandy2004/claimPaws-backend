package cn.czu.claimpaws.reservation.web;

import cn.czu.claimpaws.common.api.ApiResponse;
import cn.czu.claimpaws.common.exception.BusinessException;
import cn.czu.claimpaws.common.exception.ErrorCode;
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
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        int offset = (page - 1) * size;
        List<Reservation> reservations = reservationMapper.findPendingApprovals(offset, size);
        long total = reservationMapper.countPendingApprovals();
        List<ReservationView> views = reservations.stream().map(r -> ReservationView.from(r, 0)).toList();
        return ApiResponse.success(new PageResponse<>(views, page, size, total), requestId);
    }

    @PutMapping("/{id}/approve")
    public ApiResponse<ReservationView> approve(
            @PathVariable long id,
            @RequestBody ApproveRequest body,
            @RequestHeader(value = "X-Request-Id", required = false) String requestId) {
        Reservation reservation = reservationMapper.findById(id);
        if (reservation == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
        }
        String newStatus = body.approved ? ReservationStatus.CONFIRMED.name() : ReservationStatus.REJECTED.name();
        reservationMapper.updateStatus(id, newStatus, null);
        Reservation updated = reservationMapper.findById(id);
        return ApiResponse.success(ReservationView.from(updated, 0), requestId);
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        return ApiResponse.failure(ex.getErrorCode().getCode(), ex.getMessage(), null);
    }

    record ApproveRequest(boolean approved, String comment) {}
}
