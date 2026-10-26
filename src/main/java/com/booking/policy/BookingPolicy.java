package com.booking.policy;

import com.booking.domain.SlotType;

/**
 * Strategy Pattern: cada tipo de horário tem a sua própria regra.
 * Para criar um novo tipo, basta adicionar uma nova classe @Component; o BookingService não muda.
 */
public interface BookingPolicy {
    SlotType supports();
    void validate(BookingContext context);
}
