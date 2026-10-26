package com.booking.service;

import com.booking.domain.*;
import com.booking.policy.BookingContext;
import com.booking.policy.BookingPolicy;
import com.booking.repository.BookingRepository;
import com.booking.repository.TimeSlotRepository;
import com.booking.repository.UserRepository;
import com.booking.web.BookingResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    private final TimeSlotRepository slots;
    private final BookingRepository bookings;
    private final UserRepository users;
    private final Map<SlotType, BookingPolicy> policies = new EnumMap<>(SlotType.class);
    private final Clock clock;

    public BookingService(TimeSlotRepository slots, BookingRepository bookings, UserRepository users,
                          List<BookingPolicy> policyList, Clock clock) {
        this.slots = slots;
        this.bookings = bookings;
        this.users = users;
        this.clock = clock;
        policyList.forEach(p -> policies.put(p.supports(), p));
    }

    /**
     * Reserva um horário. O ponto crítico é o lock pessimista: se 10 pessoas clicarem ao mesmo tempo,
     * o banco as coloca em fila; só a primeira encontra o horário AVAILABLE, as outras recebem 409.
     */
    @Transactional
    public BookingResponse book(Long userId, Long slotId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
        TimeSlot slot = slots.lockById(slotId)
                .orElseThrow(() -> new NotFoundException("Horário não encontrado."));

        if (slot.getStatus() != SlotStatus.AVAILABLE) {
            throw new ConflictException("Este horário já foi reservado.");
        }
        if (slot.getProvider().getId().equals(userId)) {
            throw new BusinessRuleException("Você não pode reservar o seu próprio horário.");
        }

        BookingPolicy policy = policies.get(slot.getType());
        if (policy == null) {
            throw new IllegalStateException("Nenhuma regra configurada para o tipo " + slot.getType());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        policy.validate(new BookingContext(user, slot, now,
                bookings.countActive(userId, BookingStatus.CONFIRMED, now),
                bookings.countActiveByType(userId, BookingStatus.CONFIRMED, slot.getType(), now)));

        slot.setStatus(SlotStatus.BOOKED);
        return BookingResponse.from(bookings.save(new Booking(user, slot)));
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long requesterId, boolean admin) {
        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada."));
        if (!admin && !booking.getUser().getId().equals(requesterId)) {
            throw new AccessDeniedException("Você só pode cancelar as suas próprias reservas.");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Esta reserva já foi cancelada.");
        }
        TimeSlot slot = slots.lockById(booking.getSlot().getId())
                .orElseThrow(() -> new NotFoundException("Horário não encontrado."));
        if (Duration.between(LocalDateTime.now(clock), slot.getStartTime()).toMinutes() < 60) {
            throw new BusinessRuleException("O cancelamento só é permitido até 1 hora antes do horário.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        slot.setStatus(SlotStatus.AVAILABLE);
        return BookingResponse.from(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listByUser(Long userId) {
        return bookings.findByUser(userId).stream().map(BookingResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listAll() {
        return bookings.findAllWithDetails().stream().map(BookingResponse::from).toList();
    }
}
