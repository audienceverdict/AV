import {useEffect, useState} from 'react';
import {Link, useNavigate, useParams} from 'react-router-dom';
import {ArrowLeft, ArrowRight, MapPin, Ticket} from 'lucide-react';
import {useApp} from '../context/AppContext';
import {createBooking, getShowAvailability} from '../services/api';
import {Empty, Notice, Poster, SectionHeading, date, money} from '../components/Common';
import {TheatrePicker} from '../components/TheatrePicker';

export function BookMyShow() {
  const {movieId} = useParams();
  const {data, user} = useApp();
  const navigate = useNavigate();
  const movie = data.movies.find(item => item.id === movieId);
  const [showId, setShowId] = useState(''); const [seatMapOpen,setSeatMapOpen]=useState(false);
  const [selectedSeats, setSelectedSeats] = useState<string[]>([]);
  const [confirming, setConfirming] = useState(false);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [versionSeats,setVersionSeats]=useState<any[]|null>(null);
  useEffect(()=>{if(!showId){setVersionSeats(null);return;}let active=true;getShowAvailability(showId).then(result=>{if(active)setVersionSeats((result.layout?.seats||[]).map((seat:any)=>({...seat,id:seat.id||seat.label,row:seat.row??seat.rowNumber,column:seat.column??seat.columnNumber})));}).catch(()=>{if(active)setVersionSeats(null);});return()=>{active=false;};},[showId]);

  if (!movie || !user) return <Empty title="Movie not found"/>;
  const shows = data.shows.filter(show => show.movieId === movie.id && show.status === 'OPEN' && new Date(`${show.date}T${show.startTime}`) > new Date() && data.theatres.some(theatre => theatre.id === show.theatreId && theatre.status === 'ACTIVE') && data.screens.some(screen => screen.id === show.screenId && screen.status === 'ACTIVE'));
  const show = shows.find(item => item.id === showId);
  const screen = data.screens.find(item => item.id === show?.screenId);
  const positionedSeats = (versionSeats||screen?.seats||[]).map((seat, index) => ({...seat, row: seat.row ?? Math.floor(index / (screen?.seatsPerRow||1)) + 1, column: seat.column ?? index % (screen?.seatsPerRow||1) + 1}));
  const booked = data.bookings.filter(booking => booking.showId === showId && booking.status !== 'CANCELLED');
  const used = booked.filter(booking => booking.snapshot.mobile === user.mobile).reduce((total, booking) => total + booking.ticketCount, 0);
  const amount = show?.ticketType === 'PAID' ? selectedSeats.reduce((sum,id)=>{const seat=screen?.seats.find(item=>item.id===id);return sum+(show.seatPrices?.[seat?.category||'']??show.ticketPrice);},0) : 0;
  const chooseShow = (nextShow: typeof shows[number]) => {setShowId(nextShow.id);setSelectedSeats([]);setConfirming(false);setSeatMapOpen(true);setError('');};
  const toggleSeat = (seatId: string) => {if (!show) return;setError('');if (selectedSeats.includes(seatId)) return setSelectedSeats(selectedSeats.filter(id => id !== seatId));if (selectedSeats.length + used >= show.maxTicketsPerMobile) return setError(`You can book a maximum of ${show.maxTicketsPerMobile} tickets for this screening using the same mobile number.`);setSelectedSeats([...selectedSeats, seatId]);};
  const confirm = async () => {if (!show || !user) {navigate(`/login?next=${encodeURIComponent(window.location.pathname)}`);return;}setBusy(true);try {const booking = await createBooking(user, show.id, selectedSeats);navigate(`/booking/${booking.id}`);} catch (reason) {const message=(reason as Error).message;if(message.includes('Authentication required')||message.includes('Backend request failed (401)')){navigate(`/login?next=${encodeURIComponent(window.location.pathname)}`);return;}setError(message);setConfirming(false);} finally {setBusy(false);}};

  return <div className="container page"><Link className="muted" to={`/movie/${movie.id}`}>? Back to {movie.title}</Link><SectionHeading eyebrow="BOOK YOUR EXPERIENCE" title="Choose a cinema, then choose your seat."/><div className="booking-steps"><span className={!show ? 'current' : ''}>01 · Theatre & show</span><i/><span className={show && !confirming ? 'current' : ''}>02 · Seats</span><i/><span className={confirming ? 'current' : ''}>03 · Confirm</span></div>{!shows.length ? <Empty title="No shows are currently available for this movie."/> : <div className="booking-grid"><div className="panel">{!confirming && <>{!seatMapOpen&&<TheatrePicker theatres={data.theatres} screens={data.screens} shows={shows} selectedShowId={showId} onSelect={chooseShow}/>}{show && screen && <>{seatMapOpen && <><button type="button" className="text-button" onClick={()=>{setSeatMapOpen(false);setShowId('');setSelectedSeats([]);}}> <ArrowLeft size={16}/> Back to screens</button><div className="seat-heading"><div><h3>{screen.name}</h3><p className="muted"><MapPin size={13}/> {data.theatres.find(theatre => theatre.id === show.theatreId)?.name} · {date(show.date)} · {show.startTime}</p></div><span className="muted">Up to {show.maxTicketsPerMobile - used} tickets available</span></div><div className="seat-scroll"><div className="cinema-screen">SCREEN THIS WAY</div><div className="seat-grid" style={{gridTemplateColumns:`repeat(${screen.seatsPerRow},32px)`}}>{positionedSeats.map(seat => {const unavailable = seat.disabled || booked.some(booking => booking.seatIds.includes(seat.id));return <button key={seat.id} style={{gridColumn:seat.column,gridRow:seat.row,borderColor:seat.color||undefined}} className={`seat ${seat.category === 'PREMIUM' ? 'premium' : ''} ${selectedSeats.includes(seat.id) ? 'selected' : ''} ${unavailable ? 'booked' : ''}`} disabled={unavailable} onClick={() => toggleSeat(seat.id)}>{seat.id}</button>;})}</div></div><div className="seat-legend">{[...new Set(positionedSeats.filter(seat=>!seat.disabled).map(seat=>seat.category))].map(type=>{const color=positionedSeats.find(seat=>seat.category===type)?.color;return <span key={type}><i style={{borderColor:color||undefined}}/> {type[0]+type.slice(1).toLowerCase()}</span>})}<span><i className="selected"/>Selected</span><span><i className="booked"/>Unavailable</span></div></>}</>}</>}{confirming && show && <><h3>Review your booking</h3><dl className="details-list"><dt>Movie</dt><dd>{movie.title}</dd><dt>Theatre</dt><dd>{data.theatres.find(theatre => theatre.id === show.theatreId)?.name}</dd><dt>Screening</dt><dd>{date(show.date)} · {show.startTime}</dd><dt>Seats</dt><dd>{selectedSeats.join(', ')}</dd></dl><Notice>{show.ticketType === 'FREE' ? 'This is a free demo screening.' : 'Payment is simulated. No money is charged.'}</Notice><button className="text-button" onClick={() => setConfirming(false)}>? Change your seats</button></>}</div><aside className="panel booking-aside"><div className="mini-movie"><Poster movie={movie}/><div><span className="eyebrow">YOUR MOVIE</span><h3>{movie.title}</h3><p className="muted">{movie.language} · {movie.certification}</p></div></div><hr/><div className="summary-line"><span>Showtime</span><strong>{show ? `${date(show.date)} · ${show.startTime}` : 'Choose a time'}</strong></div><div className="summary-line"><span>Seats</span><strong>{selectedSeats.join(', ') || 'Choose seats'}</strong></div><div className="summary-line"><span>Tickets</span><strong>{selectedSeats.length}</strong></div><hr/><div className="summary-line total"><span>Total</span><strong>{money(amount)}</strong></div>{error && <Notice error>{error}</Notice>}<button className="button full" disabled={!show || !selectedSeats.length || busy} onClick={() => confirming ? confirm() : setConfirming(true)}>{busy ? 'Preparing your ticketâ€¦' : confirming ? 'Confirm booking' : <><Ticket size={16}/> Review booking</>} <ArrowRight size={16}/></button></aside></div>}</div>;
}




