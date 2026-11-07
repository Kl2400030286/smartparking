package com.parkgrid.web;

import com.parkgrid.service.BillingService;
import com.parkgrid.repo.ParkingSlotRepository;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/internal/billing")
public class InternalBillingController {
    private final BillingService billing;
    private final ParkingSlotRepository slots;

    public InternalBillingController(BillingService billing, ParkingSlotRepository slots) {
        this.billing = billing;
        this.slots = slots;
    }

    @GetMapping("/bookings/{id}/quote")
    public Map<String,Object> quote(@PathVariable Long id) {
        return billing.quote(id);
    }

    @GetMapping("/quote")
    public Map<String,Object> quote(@RequestParam Long slotId,
                                    @RequestParam Instant startTime,
                                    @RequestParam Instant endTime) {
        return billing.quote(slots.findById(slotId).orElseThrow(), startTime, endTime);
    }
}
