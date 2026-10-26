package com.booking.service;

import com.booking.domain.SlotStatus;
import com.booking.domain.SlotType;
import com.booking.domain.TimeSlot;
import com.booking.domain.User;
import com.booking.repository.TimeSlotRepository;
import com.booking.repository.UserRepository;
import com.booking.web.SlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlotService {

    private final TimeSlotRepository slots;
    private final UserRepository users;
    private final Clock clock;

    public SlotService(TimeSlotRepository slots, UserRepository users, Clock clock) {
        this.slots = slots;
        this.users = users;
        this.clock = clock;
    }

    @Transactional
    public SlotResponse create(Long providerId, LocalDateTime start, LocalDateTime end, SlotType type, BigDecimal price) {
        if (!end.isAfter(start)) {
            throw new BusinessRuleException("O fim do horário deve ser depois do início.");
        }
        if (!start.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("O horário deve estar no futuro.");
        }
        User provider = users.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Prestador não encontrado."));
        if (slots.existsOverlap(providerId, start, end)) {
            throw new ConflictException("Você já tem um horário cadastrado nesse período.");
        }
        return SlotResponse.from(slots.save(new TimeSlot(provider, start, end, type, price)));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listAvailable() {
        return slots.findAvailable(SlotStatus.AVAILABLE, LocalDateTime.now(clock)).stream()
                .map(SlotResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> listByProvider(Long providerId) {
        return slots.findByProvider(providerId).stream().map(SlotResponse::from).toList();
    }
}
