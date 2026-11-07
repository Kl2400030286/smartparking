package com.parkgrid.billing;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
public class BillingProxyController {
    private final RestTemplate http = new RestTemplate();
    private final String parkingUrl;

    public BillingProxyController(@Value("${parkgrid.parking-url:http://localhost:8080}") String parkingUrl) {
        this.parkingUrl = parkingUrl;
    }

    @RequestMapping(value = {"/api/billing/bookings/{id}/quote", "/internal/billing/bookings/{id}/quote"},
            method = RequestMethod.GET)
    public ResponseEntity<String> quote(@PathVariable long id, HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> headers.add(name, request.getHeader(name)));
        return http.exchange(parkingUrl + "/internal/billing/bookings/" + id + "/quote",
                HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }
}
