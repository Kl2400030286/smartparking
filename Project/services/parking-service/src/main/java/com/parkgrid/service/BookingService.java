package com.parkgrid.service;

import com.parkgrid.domain.*;
import com.parkgrid.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.Instant;
import java.util.*;
import org.springframework.web.client.RestTemplate;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final ParkingSlotRepository slots;
    private final UserRepository users;
    private final RestTemplate restTemplate;
    private final String billingUrl;

    public BookingService(BookingRepository bookings, ParkingSlotRepository slots, UserRepository users,
                          RestTemplate restTemplate, @org.springframework.beans.factory.annotation.Value("${parkgrid.billing-url:http://localhost:8080}") String billingUrl) {
        this.bookings = bookings; this.slots = slots; this.users = users; this.restTemplate = restTemplate; this.billingUrl = billingUrl;
    }

    @Transactional
    public Map<String, Object> create(Long slotId, Instant startTime, Instant endTime, Principal principal) {
        if (!endTime.isAfter(startTime)) throw new IllegalArgumentException("endTime must be after startTime");
        ParkingSlot slot = slots.findByIdForUpdate(slotId).orElseThrow();
        if (slot.getStatus() == ParkingSlot.Status.MAINTENANCE) throw new IllegalStateException("Slot unavailable");
        if (!bookings.findOverlaps(slotId, startTime, endTime).isEmpty())
            throw new IllegalStateException("Slot is already booked for this period");
        Map<?,?> quote = restTemplate.getForObject(
                billingUrl + "/internal/billing/quote?slotId={slotId}&startTime={startTime}&endTime={endTime}",
                Map.class, slotId, startTime, endTime);
        if (quote == null || quote.get("totalCents") == null) throw new IllegalStateException("Billing service unavailable");
        Booking booking = bookings.saveAndFlush(new Booking(users.findByEmail(principal.getName()).orElseThrow(),
                slot, startTime, endTime, ((Number) quote.get("totalCents")).longValue()));
        bookings.save(booking);
        slot.setStatus(ParkingSlot.Status.RESERVED);
        return Map.of("bookingId", booking.getId(), "slotId", slot.getId(),
                "amountCents", booking.getAmountCents(), "ratePerHourCents", slot.getZone().getRateCents(),
                "status", booking.getStatus());
    }

    @Transactional
    public int completeExpired() {
        int completed = 0;
        for (Booking booking : bookings.findByStatusAndEndTimeLessThanEqual(Booking.Status.CONFIRMED, Instant.now())) {
            booking.setStatus(Booking.Status.COMPLETED);
            if (bookings.countBySlotIdAndStatus(booking.getSlot().getId(), Booking.Status.CONFIRMED) <= 1)
                booking.getSlot().setStatus(ParkingSlot.Status.AVAILABLE);
            completed++;
        }
        return completed;
    }

    public List<Booking> findMine(Principal principal) {
        return bookings.findByUserEmailOrderByStartTimeDesc(principal.getName());
    }

    @Transactional
    public Map<String, Object> cancel(Long id, Principal principal) {
        Booking booking = bookings.findById(id).orElseThrow();
        if (!booking.getUser().getEmail().equals(principal.getName()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the booking owner can cancel it");
        if (booking.getStatus() != Booking.Status.CONFIRMED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only confirmed bookings can be cancelled");
        booking.setStatus(Booking.Status.CANCELLED);
        if (bookings.countBySlotIdAndStatus(booking.getSlot().getId(), Booking.Status.CONFIRMED) == 0)
            booking.getSlot().setStatus(ParkingSlot.Status.AVAILABLE);
        return Map.of("bookingId", id, "status", booking.getStatus(), "message", "Booking cancelled successfully");
    }
}
