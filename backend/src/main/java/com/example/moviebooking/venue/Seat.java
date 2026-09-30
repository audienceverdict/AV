package com.example.moviebooking.venue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
@Entity @Table(name="seats",uniqueConstraints=@UniqueConstraint(columnNames={"screen_id","label"}))
public class Seat {
 @Id @Column(length=80) public String id;
 @JsonIgnore @ManyToOne(optional=false) @JoinColumn(name="screen_id") public Screen screen;
 @Column(nullable=false,length=20) public String label;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public SeatCategory category=SeatCategory.REGULAR;
 @Column(nullable=false) public boolean disabled=false;
 @Column(nullable=false) public int rowNumber;
 @Column(nullable=false) public int columnNumber;
}
