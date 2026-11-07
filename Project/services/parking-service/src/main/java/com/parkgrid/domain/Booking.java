package com.parkgrid.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="bookings")
public class Booking {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private User user;
    @ManyToOne(optional=false, fetch=FetchType.EAGER) private ParkingSlot slot;
    @Column(nullable=false) private Instant startTime;
    @Column(nullable=false) private Instant endTime;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.CONFIRMED;
    @Column(nullable=false) private long amountCents;
    protected Booking(){}
    public Booking(User u,ParkingSlot s,Instant start,Instant end,long amount){user=u;slot=s;startTime=start;endTime=end;amountCents=amount;}
    public Long getId(){return id;} public User getUser(){return user;} public ParkingSlot getSlot(){return slot;}
    public Instant getStartTime(){return startTime;} public Instant getEndTime(){return endTime;} public Status getStatus(){return status;}
    public long getAmountCents(){return amountCents;} public void setStatus(Status s){status=s;}
    public void setAmountCents(long amount){amountCents=amount;}
    public enum Status { CONFIRMED, CANCELLED, COMPLETED }
}
