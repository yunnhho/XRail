import { Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import OAuthCallback from './pages/OAuthCallback';
import Home from './pages/Home';
import ScheduleSearchPage from './pages/ScheduleSearchPage';
import SeatSelectionPage from './pages/SeatSelectionPage';
import MyTicketPage from './pages/MyTicketPage';
import PaymentPage from './pages/PaymentPage';
import CartPage from './pages/CartPage';
import AdminPage from './pages/AdminPage';
import ReservationSuccessPage from './pages/ReservationSuccessPage';
import './App.css';

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/oauth/callback" element={<OAuthCallback />} />
      <Route path="/" element={<Home />} />
      <Route path="/search" element={<ScheduleSearchPage />} />
      <Route path="/reservation/:scheduleId" element={<SeatSelectionPage />} />
      <Route path="/reservation/success" element={<ReservationSuccessPage />} />
      <Route path="/payment/:reservationId" element={<PaymentPage />} />
      <Route path="/my-tickets" element={<MyTicketPage />} />
      <Route path="/cart" element={<CartPage />} />
      <Route path="/admin" element={<AdminPage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;