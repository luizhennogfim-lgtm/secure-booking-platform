package com.booking.repository;

import com.booking.domain.SlotStatus;
import com.booking.domain.TimeSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {

    /**
     * SELECT ... FOR UPDATE: a linha fica "trancada" até o fim da transação.
     * Quem chegar depois espera na fila e, quando entrar, já enxerga o horário como BOOKED.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TimeSlot s where s.id = :id")
    Optional<TimeSlot> lockById(@Param("id") Long id);

    @Query("select s from TimeSlot s join fetch s.provider where s.status = :status and s.startTime > :now order by s.startTime")
    List<TimeSlot> findAvailable(@Param("status") SlotStatus status, @Param("now") LocalDateTime now);

    @Query("select s from TimeSlot s join fetch s.provider where s.provider.id = :providerId order by s.startTime")
    List<TimeSlot> findByProvider(@Param("providerId") Long providerId);

    @Query("select count(s) > 0 from TimeSlot s where s.provider.id = :providerId and s.startTime < :end and s.endTime > :start")
    boolean existsOverlap(@Param("providerId") Long providerId,
                          @Param("start") LocalDateTime start,
                          @Param("end") LocalDateTime end);
}
