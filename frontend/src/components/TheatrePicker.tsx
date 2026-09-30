import {MapPin, MonitorPlay, Ticket} from 'lucide-react';
import type {Show, Theatre} from '../types';
import {date, money} from './Common';

type Props = {
  theatres: Theatre[];
  shows: Show[];
  selectedShowId: string;
  onSelect: (show: Show) => void;
};

export function TheatrePicker({theatres, shows, selectedShowId, onSelect}: Props) {
  const dates = [...new Set(shows.map(show => show.date))].sort();
  return <div className="theatre-picker">
    <div className="theatre-picker-head"><div><span className="eyebrow">CHOOSE YOUR CINEMA</span><h3>Pick a theatre and showtime</h3></div><span className="muted">{dates.map(date).join(' · ')}</span></div>
    {theatres.filter(theatre => shows.some(show => show.theatreId === theatre.id)).map(theatre => {
      const venueShows = shows.filter(show => show.theatreId === theatre.id);
      return <article className="venue-card" key={theatre.id}>
        <div className="venue-icon"><MapPin size={20}/></div>
        <div className="venue-info"><h3>{theatre.name}</h3><p>{theatre.address}, {theatre.city}, {theatre.state}</p><small>{theatre.contact || 'Contact available at venue'} · {new Set(venueShows.map(show => show.screenId)).size} screens</small></div>
        <div className="venue-shows">{venueShows.map(show => <button type="button" className={selectedShowId === show.id ? 'show-chip selected' : 'show-chip'} key={show.id} onClick={() => onSelect(show)}><MonitorPlay size={13}/><span>{date(show.date)}<strong>{show.startTime}</strong></span><small>{show.ticketType === 'FREE' ? 'FREE' : money(show.ticketPrice)}</small></button>)}</div>
      </article>;
    })}
    <p className="fine-print"><Ticket size={12}/> Select a showtime to reveal the seat map. Green times are available.</p>
  </div>;
}
