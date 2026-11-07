package com.parkgrid.booking;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/api/bookings")
public class BookingProxyController {
    private final RestTemplate http = new RestTemplate();
    private final String parkingUrl;
    private final String billingUrl;

    public BookingProxyController(@Value("${parkgrid.parking-url:http://localhost:8080}") String parkingUrl,
                                   @Value("${parkgrid.billing-url:http://localhost:8083}") String billingUrl) {
        this.parkingUrl = parkingUrl;
        this.billingUrl = billingUrl;
    }

    @RequestMapping(value = {"", "/", "/mine", "/{id}/cancel"}, method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<String> forward(HttpServletRequest request, @RequestBody(required = false) String body) {
        return exchange(parkingUrl + request.getRequestURI(), request, body);
    }

    @GetMapping("/{id}/quote")
    public ResponseEntity<String> quote(@PathVariable long id, HttpServletRequest request) {
        return exchange(billingUrl + "/internal/billing/bookings/" + id + "/quote", request, null);
    }

    private ResponseEntity<String> exchange(String url, HttpServletRequest request, String body) {
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name -> headers.add(name, request.getHeader(name)));
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        try {
            return http.exchange(url, HttpMethod.valueOf(request.getMethod()), entity, String.class);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(ex.getResponseHeaders())
                    .body(ex.getResponseBodyAsString());
        }
    }
}
