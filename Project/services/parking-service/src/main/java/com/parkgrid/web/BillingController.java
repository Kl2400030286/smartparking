package com.parkgrid.web;
import com.parkgrid.service.BillingService;import org.springframework.security.access.prepost.PreAuthorize;import org.springframework.web.bind.annotation.*;import java.util.Map;
@RestController @RequestMapping("/api/billing") @PreAuthorize("hasRole('USER')")
public class BillingController {
 private final BillingService billing;public BillingController(BillingService billing){this.billing=billing;}
 @GetMapping("/bookings/{id}/quote") public Map<String,Object> quote(@PathVariable Long id){return billing.quote(id);}
}
