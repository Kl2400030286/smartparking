package com.parkgrid.domain;

import jakarta.persistence.*;

@Entity
@Table(name="parking_slots", uniqueConstraints=@UniqueConstraint(columnNames={"zone_id","slot_number"}))
public class ParkingSlot {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private ParkingZone zone;
    @Column(name="slot_number", nullable=false) private String slotNumber;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.AVAILABLE;
    protected ParkingSlot(){}
    public ParkingSlot(ParkingZone zone,String slotNumber){this.zone=zone;this.slotNumber=slotNumber;}
    public Long getId(){return id;} public ParkingZone getZone(){return zone;} public String getSlotNumber(){return slotNumber;}
    public Status getStatus(){return status;} public void setStatus(Status s){status=s;}
    public enum Status { AVAILABLE, RESERVED, OCCUPIED, MAINTENANCE }
}
