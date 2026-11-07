package com.parkgrid.web;
import com.parkgrid.domain.Booking;import com.parkgrid.service.BookingService;import jakarta.validation.Valid;import jakarta.validation.constraints.*;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;import java.time.*;import java.util.*;
@RestController @RequestMapping("/api/bookings") @PreAuthorize("hasRole('USER')")
public class BookingController {
 private final BookingService bookingService;
 public BookingController(BookingService bookingService){this.bookingService=bookingService;}
 public record Request(@NotNull Long slotId,@FutureOrPresent Instant startTime,@Future Instant endTime){}
 public record Response(Long bookingId,String userEmail,Long slotId,String slotNumber,Instant startTime,Instant endTime,String status,long amountCents){}
 private Response response(Booking b){return new Response(b.getId(),b.getUser().getEmail(),b.getSlot().getId(),b.getSlot().getSlotNumber(),b.getStartTime(),b.getEndTime(),b.getStatus().name(),b.getAmountCents());}
 @PostMapping public Map<String,Object> book(@Valid @RequestBody Request r,java.security.Principal principal){return bookingService.create(r.slotId(),r.startTime(),r.endTime(),principal);}
 @GetMapping("/mine") public List<Response> mine(java.security.Principal p){return bookingService.findMine(p).stream().map(this::response).toList();}
 @PostMapping("/{id}/cancel") public Map<String,Object> cancel(@PathVariable Long id,java.security.Principal p){return bookingService.cancel(id,p);}
}
