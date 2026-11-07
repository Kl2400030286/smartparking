package com.parkgrid.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingCompletionScheduler {
    private final BookingService bookings;

    public BookingCompletionScheduler(BookingService bookings) {
        this.bookings = bookings;
    }

    @Scheduled(fixedDelayString = "${parkgrid.booking-completion-delay-ms:60000}")
    public void completeExpiredBookings() {
        bookings.completeExpired();
    }
}
