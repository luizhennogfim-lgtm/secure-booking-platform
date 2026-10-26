package com.booking.web;

import com.booking.domain.SlotStatus;
import com.booking.domain.SlotType;
import com.booking.domain.TimeSlot;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SlotResponse(Long id, String provider, LocalDateTime startTime, LocalDateTime endTime,
                           SlotType type, BigDecimal price, SlotStatus status) {

    public static SlotResponse from(TimeSlot s) {
        return new SlotResponse(s.getId(), s.getProvider().getName(), s.getStartTime(), s.getEndTime(),
                s.getType(), s.getPrice(), s.getStatus());
    }
}
