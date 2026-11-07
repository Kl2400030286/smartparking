package com.parkgrid.repo;
import com.parkgrid.domain.Booking;
import org.springframework.data.jpa.repository.*;
import java.time.Instant;
import java.util.*;
public interface BookingRepository extends JpaRepository<Booking,Long>{
    @Query("select b from Booking b where b.slot.id=:slotId and b.status='CONFIRMED' and b.startTime < :endTime and b.endTime > :startTime")
    List<Booking> findOverlaps(Long slotId,Instant startTime,Instant endTime);
    List<Booking> findByUserEmailOrderByStartTimeDesc(String email);
    long countBySlotIdAndStatus(Long slotId, Booking.Status status);
    List<Booking> findBySlotIdAndStatus(Long slotId, Booking.Status status);
    List<Booking> findByStatusAndEndTimeLessThanEqual(Booking.Status status, Instant endTime);
}
