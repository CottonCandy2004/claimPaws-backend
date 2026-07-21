package cn.czu.claimpaws.reservation.application;

import cn.czu.claimpaws.reservation.domain.CreateReservationCommand;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ReservationLock {
    public <T> T withLock(CreateReservationCommand command, Supplier<T> action) {
        return action.get();
    }
}
