package com.parkgrid.service;

import com.parkgrid.domain.ParkingSlot;
import com.parkgrid.domain.ParkingZone;
import com.parkgrid.repo.BookingRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BillingServiceTest {
    private final BillingService billing = new BillingService((BookingRepository) null);

    @Test
    void roundsPartialHoursUpAndChargesTheZoneRate() {
        ParkingZone zone = new ParkingZone("Downtown", "1 Main Street", 10, 25);
        ParkingSlot slot = new ParkingSlot(zone, "S-1");
        Instant start = Instant.parse("2026-10-24T10:00:00Z");
        Instant end = Instant.parse("2026-10-24T11:01:00Z");

        var quote = billing.quote(slot, start, end);

        assertEquals(2L, quote.get("durationHours"));
        assertEquals(25L, quote.get("ratePerHourCents"));
        assertEquals(50L, quote.get("totalCents"));
    }
}
