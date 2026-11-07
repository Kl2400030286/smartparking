package com.parkgrid.domain;

import jakarta.persistence.*;

@Entity
public class ParkingZone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable=false) private String name;
    @Column(nullable=false) private String address;
    @Column(nullable=false) private int capacity;
    @Column(nullable=false, columnDefinition="int default 5") private int rateCents = 5;
    @Column(nullable=false) private boolean active = true;
    protected ParkingZone() {}
    public ParkingZone(String name,String address,int capacity,int rateCents){this.name=name;this.address=address;this.capacity=capacity;this.rateCents=rateCents;}
    public Long getId(){return id;} public String getName(){return name;} public String getAddress(){return address;}
    public int getCapacity(){return capacity;} public int getRateCents(){return rateCents;} public boolean isActive(){return active;} public void setActive(boolean a){active=a;}
}
