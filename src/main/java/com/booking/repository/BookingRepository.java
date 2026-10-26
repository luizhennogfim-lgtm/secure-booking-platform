package com.booking.repository;

import com.booking.domain.Booking;
import com.booking.domain.BookingStatus;
import com.booking.domain.SlotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select count(b) from Booking b where b.user.id = :userId and b.status = :status and b.slot.startTime > :now")
    long countActive(@Param("userId") Long userId, @Param("status") BookingStatus status, @Param("now") LocalDateTime now);

    @Query("select count(b) from Booking b where b.user.id = :userId and b.status = :status and b.slot.type = :type and b.slot.startTime > :now")
    long countActiveByType(@Param("userId") Long userId, @Param("status") BookingStatus status,
                           @Param("type") SlotType type, @Param("now") LocalDateTime now);

    @Query("select b from Booking b join fetch b.slot s join fetch b.user where b.user.id = :userId order by s.startTime")
    List<Booking> findByUser(@Param("userId") Long userId);

    @Query("select b from Booking b join fetch b.slot s join fetch b.user order by b.createdAt desc")
    List<Booking> findAllWithDetails();
}
