package com.booking.web;

import com.booking.domain.Booking;
import com.booking.domain.BookingStatus;
import com.booking.domain.SlotType;

import java.time.LocalDateTime;

public record BookingResponse(Long id, Long slotId, Long userId, LocalDateTime startTime,
                              SlotType type, BookingStatus status) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getSlot().getId(), b.getUser().getId(),
                b.getSlot().getStartTime(), b.getSlot().getType(), b.getStatus());
    }
}
