import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import Header from '../components/Header';
import '../App.css';

const ReservationSuccessPage: React.FC = () => {
    const { state } = useLocation();
    const navigate = useNavigate();

    if (!state) {
        return <div className="text-center" style={{ marginTop: '100px' }}>잘못된 접근입니다. <button onClick={() => navigate('/')}>홈으로</button></div>;
    }

    const { reservationId, accessCode, schedule, startStation, endStation, selectedSeatIds, totalPrice, isGuest } = state;

    return (
        <div>
            <Header />
            <div className="web-container">
                <div className="web-card" style={{ maxWidth: '800px', margin: '40px auto', padding: '50px' }}>
                    <div className="text-center" style={{ marginBottom: '40px' }}>
                        <div style={{ fontSize: '5rem', marginBottom: '20px' }}>✅</div>
                        <h2 style={{ color: '#0055A5', fontSize: '2.5rem', fontWeight: 900 }}>예매가 완료되었습니다!</h2>
                        <p style={{ color: '#666', fontSize: '1.1rem' }}>XRail을 이용해 주셔서 감사합니다. 즐거운 여행 되세요!</p>
                    </div>

                    <div className="success-info-grid" style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '40px', padding: '30px', background: '#f8f9fc', borderRadius: '20px', border: '1px solid #eee' }}>
                        <div className="info-section">
                            <h4 style={{ color: '#0055A5', marginBottom: '20px', borderLeft: '4px solid #0055A5', paddingLeft: '12px' }}>여정 정보</h4>
                            <div className="info-item"><span>열차</span> <strong>{schedule.trainType} {schedule.trainNumber}</strong></div>
                            <div className="info-item"><span>구간</span> <strong>{startStation.name} → {endStation.name}</strong></div>
                            <div className="info-item"><span>출발일시</span> <strong>{schedule.departureDate} {schedule.departureTime.substring(0,5)}</strong></div>
                        </div>
                        <div className="info-section">
                            <h4 style={{ color: '#0055A5', marginBottom: '20px', borderLeft: '4px solid #0055A5', paddingLeft: '12px' }}>결제 및 좌석</h4>
                            <div className="info-item"><span>선택 좌석</span> <strong>{selectedSeatIds.length}석</strong></div>
                            <div className="info-item"><span>총 결제금액</span> <strong style={{ color: '#E60012', fontSize: '1.2rem' }}>{totalPrice.toLocaleString()}원</strong></div>
                            <div className="info-item"><span>예약 번호</span> <strong style={{ letterSpacing: '1px' }}>{reservationId}</strong></div>
                        </div>
                    </div>

                    {isGuest && (
                        <div style={{ marginTop: '30px', padding: '30px', background: '#fff4f4', borderRadius: '15px', border: '2px dashed #E60012', textAlign: 'center' }}>
                            <h4 style={{ color: '#E60012', marginBottom: '10px' }}>⚠️ 비회원 예매 정보 (반드시 보관)</h4>
                            <p style={{ fontSize: '0.9rem', color: '#666', marginBottom: '15px' }}>나중에 예약을 조회하시려면 아래 <b>예매번호</b>가 꼭 필요합니다.</p>
                            <div style={{ fontSize: '2.5rem', fontWeight: 900, color: '#333', letterSpacing: '3px' }}>{accessCode}</div>
                            <p style={{ marginTop: '10px', color: '#888', fontSize: '0.85rem' }}>※ 설정하신 비밀번호 6자리도 함께 기억해 주세요.</p>
                        </div>
                    )}

                    <div className="text-center" style={{ marginTop: '40px', display: 'flex', gap: '15px', justifyContent: 'center' }}>
                        <button className="btn-primary" onClick={() => navigate('/')} style={{ padding: '18px 50px', fontSize: '1.1rem' }}>홈으로 이동</button>
                        <button className="btn-secondary" onClick={() => navigate(isGuest ? '/my-tickets' : '/my-tickets')} style={{ padding: '18px 50px', fontSize: '1.1rem' }}>예매 내역 확인</button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ReservationSuccessPage;
