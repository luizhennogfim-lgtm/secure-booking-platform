package com.booking.web;

import com.booking.domain.SlotType;
import com.booking.service.SlotService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/slots")
public class SlotController {

    private final SlotService service;

    public SlotController(SlotService service) { this.service = service; }

    public record SlotRequest(@NotNull LocalDateTime startTime, @NotNull LocalDateTime endTime,
                              @NotNull SlotType type, @NotNull @DecimalMin("0.0") BigDecimal price) {}

    @GetMapping
    public List<SlotResponse> available() { return service.listAvailable(); }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public List<SlotResponse> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.listByProvider(Long.valueOf(jwt.getSubject()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROVIDER','ADMIN')")
    public SlotResponse create(@Valid @RequestBody SlotRequest r, @AuthenticationPrincipal Jwt jwt) {
        return service.create(Long.valueOf(jwt.getSubject()), r.startTime(), r.endTime(), r.type(), r.price());
    }
}
