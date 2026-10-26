package com.booking.policy;

import com.booking.domain.SlotType;
import com.booking.service.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Horário padrão: 2 horas de antecedência e no máximo 3 reservas futuras ao mesmo tempo. */
@Component
public class StandardPolicy implements BookingPolicy {

    static final long MIN_MINUTES_AHEAD = 120;
    static final long MAX_ACTIVE = 3;

    @Override
    public SlotType supports() { return SlotType.STANDARD; }

    @Override
    public void validate(BookingContext ctx) {
        long minutesAhead = Duration.between(ctx.now(), ctx.slot().getStartTime()).toMinutes();
        if (minutesAhead < MIN_MINUTES_AHEAD) {
            throw new BusinessRuleException("Horários padrão exigem pelo menos 2 horas de antecedência.");
        }
        if (ctx.activeBookings() >= MAX_ACTIVE) {
            throw new BusinessRuleException("Você já atingiu o limite de 3 reservas ativas.");
        }
    }
}
