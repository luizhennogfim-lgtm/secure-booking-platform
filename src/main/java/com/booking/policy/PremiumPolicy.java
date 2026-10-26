package com.booking.policy;

import com.booking.domain.SlotType;
import com.booking.service.BusinessRuleException;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Horário premium: 24 horas de antecedência e apenas 1 reserva premium ativa por pessoa. */
@Component
public class PremiumPolicy implements BookingPolicy {

    static final long MIN_HOURS_AHEAD = 24;

    @Override
    public SlotType supports() { return SlotType.PREMIUM; }

    @Override
    public void validate(BookingContext ctx) {
        if (Duration.between(ctx.now(), ctx.slot().getStartTime()).toHours() < MIN_HOURS_AHEAD) {
            throw new BusinessRuleException("Horários premium exigem pelo menos 24 horas de antecedência.");
        }
        if (ctx.activeBookingsOfSameType() >= 1) {
            throw new BusinessRuleException("Você já possui uma reserva premium ativa.");
        }
    }
}
