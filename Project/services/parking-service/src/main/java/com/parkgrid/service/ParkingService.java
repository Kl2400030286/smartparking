package com.parkgrid.service;

import com.parkgrid.domain.*;
import com.parkgrid.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class ParkingService {
    private final ParkingZoneRepository zones;
    private final ParkingSlotRepository slots;
    private final BookingRepository bookings;

    public ParkingService(ParkingZoneRepository zones, ParkingSlotRepository slots, BookingRepository bookings) {
        this.zones = zones; this.slots = slots; this.bookings = bookings;
    }

    public List<ParkingZone> findZones() { return zones.findAll(); }

    @Transactional
    public ParkingZone createZone(String name, String address, int capacity, int rateCents) {
        ParkingZone zone = zones.save(new ParkingZone(name, address, capacity, rateCents));
        for (int i = 1; i <= capacity; i++) slots.save(new ParkingSlot(zone, "S-" + i));
        return zone;
    }

    public Map<String, Object> availability(Long zoneId) {
        ParkingZone zone = zones.findById(zoneId).orElseThrow();
        long available = slots.countByZoneIdAndStatus(zoneId, ParkingSlot.Status.AVAILABLE);
        return Map.of("zoneId", zoneId, "capacity", zone.getCapacity(),
                "available", slots.countByZoneIdAndStatus(zoneId, ParkingSlot.Status.AVAILABLE),
                "reserved", slots.countByZoneIdAndStatus(zoneId, ParkingSlot.Status.RESERVED),
                "occupied", slots.countByZoneIdAndStatus(zoneId, ParkingSlot.Status.OCCUPIED),
                "maintenance", slots.countByZoneIdAndStatus(zoneId, ParkingSlot.Status.MAINTENANCE));
    }

    public List<ParkingSlot> findSlots(Long zoneId) { return slots.findByZoneId(zoneId); }

    @Transactional
    public Map<String,Object> revokeSlot(Long id) {
        ParkingSlot slot = slots.findByIdForUpdate(id).orElseThrow();
        var active = bookings.findBySlotIdAndStatus(id, Booking.Status.CONFIRMED);
        active.forEach(b -> b.setStatus(Booking.Status.CANCELLED));
        slot.setStatus(ParkingSlot.Status.MAINTENANCE);
        return Map.of("slotId", id, "status", slot.getStatus(), "revokedBookings", active.size());
    }

    @Transactional
    public Map<String,Object> updateSlotStatus(Long id, ParkingSlot.Status status) {
        if (status == null) throw new IllegalArgumentException("status is required");
        ParkingSlot slot = slots.findByIdForUpdate(id).orElseThrow();
        if (status == ParkingSlot.Status.RESERVED)
            throw new IllegalArgumentException("RESERVED is assigned by the booking flow");
        slot.setStatus(status);
        return Map.of("slotId", id, "status", status);
    }
}
