package com.example.moviebooking.venue;
import com.example.moviebooking.common.exception.ApiException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service public class TheatreService {
 private final TheatreRepository theatres; private final ScreenRepository screens;
 public TheatreService(TheatreRepository theatres,ScreenRepository screens){this.theatres=theatres;this.screens=screens;}
 public Page<Theatre> list(int page,int size){return theatres.findAll(PageRequest.of(page,Math.min(size,100),Sort.by("name")));}
 public Theatre get(String id){return theatres.findById(id).orElseThrow(()->new ApiException(404,"THEATRE_NOT_FOUND","Theatre not found"));}
 @Transactional public Theatre create(Theatre t){t.id=UUID.randomUUID().toString();attachMedia(t);return theatres.save(t);}
 @Transactional public Theatre update(String id,Theatre next){var t=get(id);t.name=next.name;t.address=next.address;t.city=next.city;t.state=next.state;t.contact=next.contact;t.status=next.status;t.mapUrl=next.mapUrl;t.media.clear();if(next.media!=null){next.media.forEach(m->{m.id=UUID.randomUUID().toString();m.theatre=t;t.media.add(m);});}return t;}
 @Transactional public void delete(String id){get(id).status=ActiveStatus.INACTIVE;}
 public List<Screen> screens(String theatreId){get(theatreId);return screens.findByTheatreId(theatreId);}
 @Transactional public Screen createScreen(String theatreId,Screen s){get(theatreId);s.id=UUID.randomUUID().toString();s.theatreId=theatreId;attachSeats(s);return screens.save(s);}
 @Transactional public Screen updateScreen(String id,Screen next){var s=screens.findById(id).orElseThrow(()->new ApiException(404,"SCREEN_NOT_FOUND","Screen not found"));s.name=next.name;s.number=next.number;s.rows=next.rows;s.seatsPerRow=next.seatsPerRow;s.status=next.status;s.seats.clear();if(next.seats!=null){next.seats.forEach(seat->{seat.id=seat.id==null||seat.id.isBlank()?s.id+":"+seat.label:seat.id;seat.screen=s;s.seats.add(seat);});}return s;}
 private void attachMedia(Theatre t){if(t.media!=null)t.media.forEach(m->{m.id=UUID.randomUUID().toString();m.theatre=t;});}
 private void attachSeats(Screen s){if(s.seats!=null)s.seats.forEach(seat->{seat.id=seat.id==null||seat.id.isBlank()?UUID.randomUUID().toString():seat.id;seat.screen=s;});}
}
