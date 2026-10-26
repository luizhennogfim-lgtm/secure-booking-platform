package com.booking.policy;

import com.booking.domain.SlotType;
import com.booking.service.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Horário expresso: só vale para as próximas 24 horas e precisa de pelo menos 15 minutos de antecedência. */
@Component
public class ExpressPolicy implements BookingPolicy {

    @Override
    public SlotType supports() { return SlotType.EXPRESS; }

    @Override
    public void validate(BookingContext ctx) {
        long minutesAhead = Duration.between(ctx.now(), ctx.slot().getStartTime()).toMinutes();
        if (minutesAhead < 15) {
            throw new BusinessRuleException("Horários expressos exigem pelo menos 15 minutos de antecedência.");
        }
        if (minutesAhead > 24 * 60) {
            throw new BusinessRuleException("Horários expressos só podem ser reservados nas próximas 24 horas.");
        }
    }
}
