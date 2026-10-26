package com.booking.service;

import com.booking.domain.Role;
import com.booking.domain.SlotType;
import com.booking.domain.TimeSlot;
import com.booking.domain.User;
import com.booking.repository.TimeSlotRepository;
import com.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prova do lock pessimista: 10 pessoas tentam reservar o MESMO horário ao mesmo tempo.
 * Exatamente uma deve conseguir; as outras 9 recebem "horário já reservado".
 */
@SpringBootTest
@ActiveProfiles("test")
class BookingConcurrencyTest {

    @Autowired BookingService bookingService;
    @Autowired UserRepository users;
    @Autowired TimeSlotRepository slots;

    @Test
    void onlyOneUserCanBookTheSameSlot() throws Exception {
        User provider = users.save(new User("Prestador", "prov@test.com", "hash", Role.PROVIDER));
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        TimeSlot slot = slots.save(new TimeSlot(provider, start, start.plusHours(1), SlotType.STANDARD, BigDecimal.TEN));

        int people = 10;
        List<Long> userIds = new ArrayList<>();
        for (int i = 0; i < people; i++) {
            userIds.add(users.save(new User("User " + i, "user" + i + "@test.com", "hash", Role.USER)).getId());
        }

        ExecutorService pool = Executors.newFixedThreadPool(people);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<Boolean>> results = new ArrayList<>();
        for (Long userId : userIds) {
            results.add(pool.submit(() -> {
                startSignal.await();
                try {
                    bookingService.book(userId, slot.getId());
                    return true;
                } catch (ConflictException e) {
                    return false;
                }
            }));
        }
        startSignal.countDown(); // todos largam juntos

        int successes = 0;
        for (Future<Boolean> result : results) {
            if (result.get(30, TimeUnit.SECONDS)) successes++;
        }
        pool.shutdown();

        assertEquals(1, successes, "somente uma reserva pode ser confirmada");
    }
}
