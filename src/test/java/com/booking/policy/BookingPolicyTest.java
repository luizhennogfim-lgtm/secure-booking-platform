package com.booking.policy;

import com.booking.domain.Role;
import com.booking.domain.SlotType;
import com.booking.domain.TimeSlot;
import com.booking.domain.User;
import com.booking.service.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingPolicyTest {

    private final LocalDateTime now = LocalDateTime.of(2030, 5, 10, 12, 0);
    private final User user = new User("Ana", "ana@x.com", "hash", Role.USER);
    private final User provider = new User("Pro", "pro@x.com", "hash", Role.PROVIDER);

    private BookingContext context(SlotType type, LocalDateTime start, long active, long activeSameType) {
        TimeSlot slot = new TimeSlot(provider, start, start.plusHours(1), type, BigDecimal.TEN);
        return new BookingContext(user, slot, now, active, activeSameType);
    }

    @Test
    void standardNeedsTwoHoursAndHasLimit() {
        StandardPolicy policy = new StandardPolicy();
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.STANDARD, now.plusHours(1), 0, 0)));
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.STANDARD, now.plusDays(2), 3, 0)));
        assertDoesNotThrow(() -> policy.validate(context(SlotType.STANDARD, now.plusHours(3), 2, 0)));
    }

    @Test
    void premiumNeedsOneDayAndOnlyOneActive() {
        PremiumPolicy policy = new PremiumPolicy();
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.PREMIUM, now.plusHours(10), 0, 0)));
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.PREMIUM, now.plusDays(3), 1, 1)));
        assertDoesNotThrow(() -> policy.validate(context(SlotType.PREMIUM, now.plusDays(3), 1, 0)));
    }

    @Test
    void expressOnlyWithinNext24Hours() {
        ExpressPolicy policy = new ExpressPolicy();
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.EXPRESS, now.plusMinutes(5), 0, 0)));
        assertThrows(BusinessRuleException.class, () -> policy.validate(context(SlotType.EXPRESS, now.plusDays(2), 0, 0)));
        assertDoesNotThrow(() -> policy.validate(context(SlotType.EXPRESS, now.plusHours(5), 0, 0)));
    }
}
