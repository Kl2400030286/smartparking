package com.parkgrid.repo;
import com.parkgrid.domain.ParkingSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.*;
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot,Long>{
    List<ParkingSlot> findByZoneId(Long zoneId);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select s from ParkingSlot s where s.id=:id") Optional<ParkingSlot> findByIdForUpdate(Long id);
    long countByZoneIdAndStatus(Long zoneId, ParkingSlot.Status status);
}
