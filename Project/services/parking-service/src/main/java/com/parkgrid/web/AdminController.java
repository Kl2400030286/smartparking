package com.parkgrid.web;
import com.parkgrid.repo.BookingRepository;import com.parkgrid.service.AuthService;import com.parkgrid.service.ParkingService;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;import java.util.Map;
@RestController @RequestMapping("/api/admin") @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 private final BookingRepository bookings; private final AuthService auth; private final ParkingService parking;
 public AdminController(BookingRepository b,AuthService a,ParkingService p){bookings=b;auth=a;parking=p;}
 public record AdminRegistration(@Email @NotBlank String email,@Size(min=8) String password){}
 public record SlotStatusRequest(com.parkgrid.domain.ParkingSlot.Status status){}
 @PostMapping("/admins") public Map<String,Object> createAdmin(@Valid @RequestBody AdminRegistration r){return auth.createAdmin(r.email(),r.password());}
 @PostMapping("/slots/{id}/revoke") public Map<String,Object> revokeSlot(@PathVariable Long id){return parking.revokeSlot(id);}
 @PatchMapping("/slots/{id}/status") public Map<String,Object> updateSlotStatus(@PathVariable Long id,@RequestBody SlotStatusRequest r){return parking.updateSlotStatus(id,r.status());}
 @GetMapping("/metrics") public Map<String,Object> metrics(){var all=bookings.findAll();return Map.of("totalBookings",all.size(),"confirmedBookings",all.stream().filter(b->b.getStatus().name().equals("CONFIRMED")).count(),"revenueCents",all.stream().mapToLong(b->b.getAmountCents()).sum());}
}
