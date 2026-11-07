package com.parkgrid.web;
import com.parkgrid.domain.*; import com.parkgrid.service.ParkingService; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import java.util.*;
@RestController @RequestMapping("/api/parking")
public class ParkingController {
 private final ParkingService parking;
 public ParkingController(ParkingService parking){this.parking=parking;}
 public record ZoneRequest(@NotBlank String name,@NotBlank String address,@Min(1) int capacity,@Min(5) @Max(100) int rateCents){}
 @GetMapping("/zones") public List<ParkingZone> zones(){return parking.findZones();}
 @PostMapping("/zones") @PreAuthorize("hasRole('ADMIN')") public ParkingZone create(@Valid @RequestBody ZoneRequest r){return parking.createZone(r.name(),r.address(),r.capacity(),r.rateCents());}
 @GetMapping("/zones/{zoneId}/availability") public Map<String,Object> availability(@PathVariable Long zoneId){return parking.availability(zoneId);}
 @GetMapping("/zones/{zoneId}/slots") public List<ParkingSlot> slots(@PathVariable Long zoneId){return parking.findSlots(zoneId);}
}
