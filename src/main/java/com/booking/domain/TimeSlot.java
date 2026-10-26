package com.booking.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Um horário oferecido por um prestador (PROVIDER) que um cliente (USER) pode reservar. */
@Entity
@Table(name = "time_slots", indexes = @Index(name = "idx_slot_status_start", columnList = "status,startTime"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User provider;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotType type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SlotStatus status = SlotStatus.AVAILABLE;

    public TimeSlot(User provider, LocalDateTime startTime, LocalDateTime endTime, SlotType type, BigDecimal price) {
        this.provider = provider;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.price = price;
    }
}
