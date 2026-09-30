package com.example.moviebooking.showtime;
import com.example.moviebooking.booking.BookingRepository;
import com.example.moviebooking.booking.BookingStatus;
import com.example.moviebooking.catalog.*;
import com.example.moviebooking.common.exception.ApiException;
import com.example.moviebooking.venue.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
@Service public class ShowService {
 private final ShowRepository shows; private final MovieRepository movies; private final TheatreRepository theatres; private final ScreenRepository screens; private final SeatRepository seats; private final BookingRepository bookings;
 public ShowService(ShowRepository shows,MovieRepository movies,TheatreRepository theatres,ScreenRepository screens,SeatRepository seats,BookingRepository bookings){this.shows=shows;this.movies=movies;this.theatres=theatres;this.screens=screens;this.seats=seats;this.bookings=bookings;}
 public List<Show> list(String movieId,String theatreId,LocalDate date){if(movieId!=null)return shows.findByMovieIdAndDateGreaterThanEqual(movieId,date==null?LocalDate.now():date);if(theatreId!=null&&date!=null)return shows.findByTheatreIdAndDate(theatreId,date);return shows.findAll();}
 public Show get(String id){return shows.findById(id).orElseThrow(()->new ApiException(404,"SHOW_NOT_FOUND","Show not found"));}
 public Map<String,Object> availability(String id){var show=get(id);var screen=screens.findById(show.screenId).orElseThrow(()->new ApiException(404,"SCREEN_NOT_FOUND","Screen not found"));var booked=bookings.findBookedSeatIds(id,List.of(BookingStatus.PENDING,BookingStatus.CONFIRMED));return Map.of("show",show,"screen",screen,"bookedSeatIds",booked);}
 @Transactional public Show create(Show s){validateRelations(s);validateOverlap(null,s);s.id=UUID.randomUUID().toString();return shows.save(s);}
 @Transactional public Show update(String id,Show next){var s=get(id);next.id=id;validateRelations(next);validateOverlap(id,next);s.movieId=next.movieId;s.theatreId=next.theatreId;s.screenId=next.screenId;s.date=next.date;s.startTime=next.startTime;s.endTime=next.endTime;s.ticketType=next.ticketType;s.ticketPrice=next.ticketPrice;s.maxTicketsPerMobile=next.maxTicketsPerMobile;s.requireAdminConfirmation=next.requireAdminConfirmation;s.bookingOpens=next.bookingOpens;s.bookingCloses=next.bookingCloses;s.status=next.status;return s;}
 @Transactional public void cancel(String id){get(id).status=ShowStatus.CANCELLED;}
 private void validateRelations(Show s){movies.findById(s.movieId).filter(m->m.status!=MovieStatus.ENDED).orElseThrow(()->new ApiException(400,"INVALID_MOVIE","Movie is unavailable"));theatres.findById(s.theatreId).filter(t->t.status==ActiveStatus.ACTIVE).orElseThrow(()->new ApiException(400,"INVALID_THEATRE","Theatre is unavailable"));screens.findById(s.screenId).filter(sc->sc.status==ActiveStatus.ACTIVE&&sc.theatreId.equals(s.theatreId)).orElseThrow(()->new ApiException(400,"INVALID_SCREEN","Screen is unavailable"));}
 private void validateOverlap(String id,Show s){if(!s.startTime.isBefore(s.endTime))throw new ApiException(400,"INVALID_TIME","Show start time must be before end time");var overlaps=shows.overlapping(s.screenId,s.date,s.startTime,s.endTime,ShowStatus.CANCELLED).stream().anyMatch(o->!o.id.equals(id));if(overlaps)throw new ApiException(409,"SHOW_OVERLAP","Screen already has a show in this time range");}
}
