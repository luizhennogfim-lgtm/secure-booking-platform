package com.booking.policy;

import com.booking.domain.TimeSlot;
import com.booking.domain.User;

import java.time.LocalDateTime;

/** Tudo o que uma regra de agendamento precisa saber para decidir. */
public record BookingContext(User user, TimeSlot slot, LocalDateTime now,
                             long activeBookings, long activeBookingsOfSameType) {}
