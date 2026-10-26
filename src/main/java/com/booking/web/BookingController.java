package com.booking.web;

import com.booking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) { this.service = service; }

    @PostMapping("/slots/{slotId}/book")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('USER')")
    public BookingResponse book(@PathVariable Long slotId, @AuthenticationPrincipal Jwt jwt) {
        return service.book(Long.valueOf(jwt.getSubject()), slotId);
    }

    @GetMapping("/bookings/me")
    @PreAuthorize("hasRole('USER')")
    public List<BookingResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.listByUser(Long.valueOf(jwt.getSubject()));
    }

    @DeleteMapping("/bookings/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public BookingResponse cancel(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt, Authentication auth) {
        boolean admin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return service.cancel(id, Long.valueOf(jwt.getSubject()), admin);
    }

    @GetMapping("/admin/bookings")
    @PreAuthorize("hasRole('ADMIN')")
    public List<BookingResponse> all() { return service.listAll(); }
}
