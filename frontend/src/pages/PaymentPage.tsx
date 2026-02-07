import React, { useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { payReservation } from '../services/reservationService';
import Header from '../components/Header';
import '../App.css';

const PaymentPage: React.FC = () => {
    const { reservationId } = useParams<{ reservationId: string }>();
    const { state } = useLocation();
    const navigate = useNavigate();
    
    const [loading, setLoading] = useState(false);
    const [tempAccessCode, setTempAccessCode] = useState<string | null>(null);
    const [showFinalCode, setShowFinalCode] = useState(false);
    const [confirmPw, setConfirmPw] = useState('');

    if (!state) {
        return <div style={{ textAlign: 'center', marginTop: '50px' }}>잘못된 접근입니다. <button onClick={() => navigate('/')}>메인으로</button></div>;
    }

    // [Safety Check] 변수 정의를 최상단으로 이동 (Rendering 블록 이전에)
    const trainInfo = state.schedule ? `${state.schedule.trainNumber} (${state.schedule.trainType})` : '정보 없음';
    const routeInfo = (state.startStation && state.endStation) ? `${state.startStation.name} → ${state.endStation.name}` : '정보 없음';
    const timeInfo = state.schedule ? `${state.schedule.departureDate} ${state.schedule.departureTime.substring(0,5)}` : '';
    const seatCount = state.selectedSeatIds ? state.selectedSeatIds.length : (state.ticketCount || 0);

    const handlePay = async () => {
        setLoading(true);
        try {
            const code = await payReservation(Number(reservationId));
            const isGuest = !!code;

            // [통합] 결과 페이지로 이동하며 정보 전달
            navigate(`/reservation/success`, { 
                state: { 
                    ...state, 
                    reservationId, 
                    accessCode: code, 
                    isGuest 
                } 
            });

            if (isGuest) {
                localStorage.removeItem('accessToken');
                localStorage.removeItem('role');
            }
        } catch (error) {
            alert("결제 중 오류가 발생했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const handleVerifyPassword = (e: React.FormEvent) => {
        e.preventDefault();
        // 비회원 비밀번호 확인 로직 (프론트엔드 단순 검증)
        // 실제로는 이미 결제 성공했으므로 확인만 하는 절차
        if (confirmPw.length === 6) {
            setShowFinalCode(true);
        } else {
            alert("비밀번호 6자리를 입력해주세요.");
        }
    };

    const handlePayLater = () => {
        navigate('/cart');
    };

    // 1. 결제 완료 최종 화면 (비회원)
    if (showFinalCode && tempAccessCode) {
        return (
            <div>
                <Header />
                <div className="web-container">
                    <div className="web-card" style={{ maxWidth: '800px', margin: '0 auto' }}>
                        <div className="text-center" style={{ marginBottom: '40px' }}>
                            <div style={{ fontSize: '4rem', marginBottom: '10px' }}>🎟️</div>
                            <h2 style={{ color: '#0055A5' }}>승차권 예매가 완료되었습니다!</h2>
                            <p style={{ color: '#666' }}>예매번호를 반드시 저장해 주세요.</p>
                        </div>

                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '30px', marginBottom: '40px' }}>
                            <div>
                                <h4 style={{ borderLeft: '4px solid #0055A5', paddingLeft: '10px', marginBottom: '15px' }}>예약 정보</h4>
                                <div className="info-item"><span>예약번호</span> <strong>{reservationId}</strong></div>
                                <div className="info-item"><span>열차번호</span> <strong>{trainInfo}</strong></div>
                                <div className="info-item"><span>운행구간</span> <strong>{routeInfo}</strong></div>
                                {timeInfo && <div className="info-item"><span>출발시간</span> <strong>{timeInfo}</strong></div>}
                            </div>
                            <div>
                                <h4 style={{ borderLeft: '4px solid #0055A5', paddingLeft: '10px', marginBottom: '15px' }}>결제 및 좌석</h4>
                                <div className="info-item"><span>좌석정보</span> <strong>{seatCount}석</strong></div>
                                <div className="info-item"><span>총 결제금액</span> <strong style={{ color: '#E60012' }}>{Number(state.totalPrice).toLocaleString()}원</strong></div>
                                <div style={{ marginTop: '20px', background: '#f0f7ff', padding: '15px', borderRadius: '8px' }}>
                                    <span style={{ display: 'block', fontSize: '0.8rem', color: '#666' }}>비회원 예매번호 (Access Code)</span>
                                    <strong style={{ fontSize: '1.8rem', color: '#0055A5', letterSpacing: '2px' }}>{tempAccessCode}</strong>
                                </div>
                            </div>
                        </div>

                        <div className="text-center">
                            <button className="btn-primary" onClick={() => navigate('/')} style={{ padding: '15px 40px' }}>홈으로 이동</button>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // 2. 비회원 비밀번호 확인 화면
    if (tempAccessCode && !showFinalCode) {
        return (
            <div>
                <Header />
                <div className="web-container">
                    <div className="web-card text-center" style={{ maxWidth: '500px', margin: '0 auto' }}>
                        <h2 style={{ marginBottom: '20px' }}>비밀번호 확인</h2>
                        <p style={{ color: '#666', marginBottom: '30px' }}>예매번호를 확인하기 위해<br/>설정하신 <b>비밀번호 6자리</b>를 입력해주세요.</p>
                        <form onSubmit={handleVerifyPassword}>
                            <input 
                                type="password" 
                                value={confirmPw} 
                                onChange={(e) => setConfirmPw(e.target.value)} 
                                maxLength={6}
                                style={{ width: '100%', padding: '15px', fontSize: '1.5rem', textAlign: 'center', letterSpacing: '10px', borderRadius: '8px', border: '2px solid #ddd', marginBottom: '20px' }}
                                placeholder="******"
                                required
                            />
                            <button type="submit" className="btn-primary full-width" style={{ padding: '15px' }}>확인</button>
                        </form>
                    </div>
                </div>
            </div>
        );
    }

    // 3. 기본 결제 대기 화면
    return (
        <div>
            <Header />
            <div className="web-container">
                <div className="web-card" style={{ maxWidth: '600px', margin: '0 auto' }}>
                    <h2 className="text-center" style={{ color: '#0055A5' }}>💳 결제하기</h2>
                    <p className="text-center" style={{ color: '#666', marginBottom: '30px' }}>
                        20분 내에 결제하지 않으면 예약이 자동으로 취소됩니다.
                    </p>

                    <div style={{ background: '#f8f9fa', padding: '25px', borderRadius: '10px', marginBottom: '30px' }}>
                        <div className="info-row"><span>예약 번호</span> <strong>{reservationId}</strong></div>
                        <div className="info-row"><span>여정</span> <strong>{routeInfo}</strong></div>
                        {timeInfo && <div className="info-row"><span>일시</span> <strong>{timeInfo}</strong></div>}
                        <div className="info-row"><span>선택 좌석</span> <strong>{seatCount}개</strong></div>
                        <div className="info-row" style={{ borderTop: '1px solid #ddd', paddingTop: '15px', color: '#E60012', fontSize: '1.4rem' }}>
                            <span>최종 결제 금액</span> <strong>{Number(state.totalPrice).toLocaleString()}원</strong>
                        </div>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                        <button className="btn-primary full-width" onClick={handlePay} disabled={loading} style={{ padding: '18px', fontSize: '1.2rem' }}>
                            {loading ? '처리 중...' : '지금 결제하기'}
                        </button>
                        <button className="btn-secondary full-width" onClick={() => navigate('/')} disabled={loading} style={{ padding: '18px', fontSize: '1.1rem' }}>
                            결제 취소 (메인으로)
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PaymentPage;