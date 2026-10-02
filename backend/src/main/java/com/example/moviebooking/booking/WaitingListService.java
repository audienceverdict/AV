package com.example.moviebooking.booking;
import com.example.moviebooking.common.exception.ApiException;
import com.example.moviebooking.showtime.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.Instant;
import java.security.Principal;
import java.util.*;
@Service public class WaitingListService {
 private final WaitingListRepository entries; private final ShowRepository shows; @Value("${booking.waiting-list-offer-minutes:5}") private long offerMinutes;
 public WaitingListService(WaitingListRepository entries,ShowRepository shows){this.entries=entries;this.shows=shows;}
 public List<WaitingListEntry> mine(Principal p){return entries.findAll().stream().filter(e->e.userId.equals(p.getName())).sorted(Comparator.comparing((WaitingListEntry e)->e.createdAt,Comparator.reverseOrder())).toList();}
 public List<WaitingListEntry> all(){return entries.findAll().stream().sorted(Comparator.comparing((WaitingListEntry e)->e.showId).thenComparingInt(e->e.position)).toList();}
 @Transactional public WaitingListEntry join(Principal p,WaitingListRequest r){var show=shows.findById(r.showId).orElseThrow(()->new ApiException(404,"SHOW_NOT_FOUND","Show not found"));if(show.status!=ShowStatus.OPEN)throw new ApiException(400,"SHOW_CLOSED","Show is not open for booking");var current=entries.findByShowIdAndStatusOrderByPositionAscCreatedAtAsc(show.id,WaitingListStatus.WAITING);if(current.stream().anyMatch(e->e.userId.equals(p.getName())))throw new ApiException(409,"ALREADY_WAITING","You are already on this waiting list");var e=new WaitingListEntry();e.showId=show.id;e.userId=p.getName();e.requestedSeatsCount=r.requestedSeatsCount;e.position=current.size()+1;return entries.save(e);}
 @Transactional public void cancel(String id,Principal p){var e=entries.findById(id).orElseThrow(()->new ApiException(404,"WAITING_LIST_NOT_FOUND","Waiting-list entry not found"));if(!e.userId.equals(p.getName()))throw new ApiException(403,"FORBIDDEN","Access denied");e.status=WaitingListStatus.CANCELLED;}
 @Transactional public WaitingListEntry claim(String id,Principal p){var e=entries.findById(id).orElseThrow(()->new ApiException(404,"WAITING_LIST_NOT_FOUND","Waiting-list entry not found"));if(!e.userId.equals(p.getName()))throw new ApiException(403,"FORBIDDEN","Access denied");if(e.status!=WaitingListStatus.OFFERED||e.offerExpiresAt==null||e.offerExpiresAt.isBefore(Instant.now()))throw new ApiException(409,"OFFER_EXPIRED","This waiting-list offer has expired");e.status=WaitingListStatus.CONVERTED;return e;}
 @Scheduled(fixedDelay=30000) @Transactional public void expireOffers(){var now=Instant.now();entries.findAll().stream().filter(e->e.status==WaitingListStatus.OFFERED&&e.offerExpiresAt!=null&&e.offerExpiresAt.isBefore(now)).forEach(e->e.status=WaitingListStatus.EXPIRED);}
}
