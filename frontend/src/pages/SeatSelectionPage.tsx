import React, { useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { getSeats, createReservation } from '../services/reservationService';
import apiClient from '../api/apiClient';
import type { Seat } from '../services/reservationService';
import Header from '../components/Header';
import '../App.css';

const SeatSelectionPage: React.FC = () => {
    const { scheduleId } = useParams<{ scheduleId: string }>();
    const { state } = useLocation();
    const navigate = useNavigate();

    const [allSeats, setAllSeats] = useState<Seat[]>([]);
    const [selectedCarriage, setSelectedCarriage] = useState<number>(1);
    const [selectedSeatIds, setSelectedSeatIds] = useState<number[]>([]);
    const [loading, setLoading] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    
    // Guest Modal State
    const [showGuestModal, setShowGuestModal] = useState(false);
    const [guestName, setGuestName] = useState('');
    const [guestPhone, setGuestPhone] = useState('');
    const [guestPw, setGuestPw] = useState('');

    useEffect(() => {
        setIsLoggedIn(!!localStorage.getItem('accessToken'));
        loadSeats();
    }, [scheduleId]);

    const loadSeats = async () => {
        if (!scheduleId || !state) return;
        setLoading(true);
        try {
            const data = await getSeats(Number(scheduleId), state.startStation.id, state.endStation.id);
            setAllSeats(data || []);
            if (data && data.length > 0) {
                setSelectedCarriage(data[0].carriageNumber);
            }
        } catch (error) {
            alert("좌석 정보를 불러오지 못했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const carriages = Array.from(new Set(allSeats.map(s => s.carriageNumber))).sort((a,b) => a-b);
    const currentSeats = allSeats.filter(s => s.carriageNumber === selectedCarriage);

    const toggleSeat = (seatId: number) => {
        setSelectedSeatIds(prev => 
            prev.includes(seatId) ? prev.filter(id => id !== seatId) : [...prev, seatId]
        );
    };

    const handleProceed = () => {
        if (selectedSeatIds.length === 0) {
            alert("좌석을 선택해주세요.");
            return;
        }
        if (isLoggedIn) {
            handleReserve();
        } else {
            setShowGuestModal(true);
        }
    };

    const handleGuestRegisterAndReserve = async (e: React.FormEvent) => {
        e.preventDefault();
        if (guestPw.length !== 6 || isNaN(Number(guestPw))) {
            alert("비밀번호는 6자리 숫자여야 합니다.");
            return;
        }
        try {
            // 1. 비회원 등록 (토큰 발급)
            const authResponse = await apiClient.post('/auth/guest/register', {
                name: guestName,
                phone: guestPhone,
                password: guestPw
            });
            localStorage.setItem('accessToken', authResponse.data.accessToken);
            localStorage.setItem('role', 'GUEST'); // [Flag] 비회원 트랜잭션 표시
            setIsLoggedIn(true); // 상태 업데이트
            
            // 2. 예약 진행
            await handleReserve();
        } catch (error: any) {
            alert(error.response?.data?.message || "비회원 등록에 실패했습니다.");
        }
    };

    const handleReserve = async () => {
        try {
            const reservationId = await createReservation({
                scheduleId: Number(scheduleId),
                seatIds: selectedSeatIds,
                startStationId: state.startStation.id,
                endStationId: state.endStation.id,
                startStationIdx: state.startStation.idx,
                endStationIdx: state.endStation.idx,
                price: calculatePrice()
            });
            navigate(`/payment/${reservationId}`, { state: { ...state, selectedSeatIds, totalPrice: calculatePrice() } });
        } catch (error) {
            alert("예약에 실패했습니다. 이미 선택된 좌석이 포함되어 있을 수 있습니다.");
        }
    };

    const calculatePrice = () => {
        const basePrice = state.schedule.price || 0;
        return basePrice * selectedSeatIds.length;
    };

    return (
        <div>
            <Header />
            <div className="web-container">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
                    <h2>좌석 선택 <span style={{ fontSize: '1rem', color: '#666' }}>({state?.schedule.trainNumber})</span></h2>
                    <div className="total-price-box">
                        예정금액: <strong>{calculatePrice().toLocaleString()}원</strong>
                    </div>
                </div>

                <div className="carriage-tabs">
                    {carriages.map(c => (
                        <button 
                            key={c} 
                            className={`carriage-tab ${selectedCarriage === c ? 'active' : ''}`}
                            onClick={() => setSelectedCarriage(c)}
                        >
                            {c}호차
                        </button>
                    ))}
                </div>

                <div className="web-card seat-layout">
                    <div className="screen-indicator">앞 쪽 (Train Head)</div>
                    <div className="seat-grid">
                        {loading ? <p>로딩 중...</p> : currentSeats.map(seat => (
                            <button 
                                key={seat.seatId}
                                className={`seat-item ${seat.isReserved ? 'reserved' : selectedSeatIds.includes(seat.seatId) ? 'selected' : ''}`}
                                disabled={seat.isReserved}
                                onClick={() => toggleSeat(seat.seatId)}
                            >
                                <span className="seat-head"></span>
                                {seat.seatNumber}
                            </button>
                        ))}
                    </div>
                    
                    <div className="reservation-action-bar">
                        <div className="selection-summary">
                            {selectedSeatIds.length > 0 ? `${selectedSeatIds.length}석 선택됨` : '좌석을 선택하세요'}
                        </div>
                        <button className={`btn-reserve ${selectedSeatIds.length === 0 ? 'disabled' : ''}`} onClick={handleProceed} disabled={selectedSeatIds.length === 0}>
                            {isLoggedIn ? '예매하기' : '비회원 예매 / 로그인'}
                        </button>
                    </div>
                </div>
            </div>

            {/* Guest Info Modal */}
            {showGuestModal && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <h3 style={{ marginBottom: '20px', color: '#0055A5' }}>비회원 예매 정보 입력</h3>
                        <p style={{ marginBottom: '20px', color: '#666', fontSize: '0.9rem' }}>
                            예매 내역 확인을 위해 정확한 정보를 입력해주세요.<br/>
                            입력하신 정보로 예매가 진행됩니다.
                        </p>
                        <form onSubmit={handleGuestRegisterAndReserve}>
                            <div className="input-group" style={{ marginBottom: '15px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>이름</label>
                                <input type="text" value={guestName} onChange={(e) => setGuestName(e.target.value)} required style={{ width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd' }} />
                            </div>
                            <div className="input-group" style={{ marginBottom: '15px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>휴대폰 번호</label>
                                <input type="text" value={guestPhone} onChange={(e) => setGuestPhone(e.target.value)} placeholder="010-0000-0000" required style={{ width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd' }} />
                            </div>
                            <div className="input-group" style={{ marginBottom: '25px' }}>
                                <label style={{ display: 'block', marginBottom: '5px', fontWeight: 'bold' }}>비밀번호 (6자리 숫자)</label>
                                <input type="password" value={guestPw} onChange={(e) => setGuestPw(e.target.value)} maxLength={6} placeholder="예매 확인용 비밀번호" required style={{ width: '100%', padding: '10px', borderRadius: '5px', border: '1px solid #ddd' }} />
                            </div>
                            <div style={{ display: 'flex', gap: '10px' }}>
                                <button type="button" onClick={() => setShowGuestModal(false)} className="btn-secondary" style={{ flex: 1, padding: '12px', borderRadius: '5px', border: '1px solid #ddd', background: '#fff' }}>취소</button>
                                <button type="submit" className="btn-primary" style={{ flex: 2, padding: '12px', borderRadius: '5px', border: 'none', background: '#0055A5', color: 'white', fontWeight: 'bold' }}>확인 및 결제</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <style>{`
                .carriage-tabs { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #ddd; padding-bottom: 10px; overflow-x: auto; }
                .carriage-tab { padding: 10px 20px; border: 1px solid #ccc; background: #fff; cursor: pointer; border-radius: 4px; font-weight: bold; white-space: nowrap; transition: 0.2s; }
                .carriage-tab.active { background: #0055A5; color: white; border-color: #0055A5; transform: scale(1.05); }
                
                .seat-layout { display: flex; flex-direction: column; align-items: center; padding: 40px; position: relative; }
                .screen-indicator { width: 100%; text-align: center; background: #f1f3f5; padding: 5px; margin-bottom: 20px; color: #999; font-size: 0.9rem; border-radius: 4px; }
                
                .seat-grid { 
                    display: grid; grid-template-columns: repeat(4, auto); column-gap: 15px; row-gap: 15px;
                }
                /* 통로 만들기: 2번째 컬럼(4n+2) 다음에 마진 추가 */
                .seat-item:nth-of-type(4n+2) { margin-right: 40px; }

                .seat-item { 
                    width: 60px; height: 70px; border: 2px solid #ddd; background: #fff; 
                    color: #555; border-radius: 8px 8px 12px 12px; font-weight: bold; cursor: pointer;
                    display: flex; flex-direction: column; align-items: center; justify-content: center;
                    transition: all 0.2s; position: relative;
                }
                .seat-head { width: 40px; height: 8px; background: #eee; border-radius: 4px; margin-bottom: 5px; }
                .seat-item:hover { border-color: #0055A5; transform: translateY(-2px); }
                
                .seat-item.selected { background: #0055A5; color: white; border-color: #0055A5; box-shadow: 0 4px 10px rgba(0,85,165,0.3); }
                .seat-item.selected .seat-head { background: rgba(255,255,255,0.3); }
                
                .seat-item.reserved { 
                    background: #f1f3f5; border-color: #e0e0e0; color: #ccc; cursor: not-allowed; 
                    pointer-events: none; opacity: 0.6;
                }
                .seat-item.reserved .seat-head { background: #e9ecef; }
                
                .total-price-box { font-size: 1.2rem; }
                .total-price-box strong { color: #E60012; font-size: 2rem; }
                
                .reservation-action-bar {
                    margin-top: 40px; width: 100%; max-width: 500px;
                    display: flex; gap: 10px; align-items: stretch;
                }
                .selection-summary {
                    flex: 1; display: flex; align-items: center; justify-content: center;
                    background: #f8f9fa; border-radius: 8px; font-weight: 700; color: #0055A5;
                }
                .btn-reserve {
                    flex: 2; height: 3.5rem; font-size: 1.2rem; font-weight: 800;
                    background: #0055A5; color: white; border: none; border-radius: 8px; cursor: pointer;
                    transition: 0.2s; box-shadow: 0 5px 15px rgba(0,85,165,0.2);
                }
                .btn-reserve:hover { background: #00448a; transform: translateY(-2px); }
                .btn-reserve.disabled { background: #ccc; cursor: not-allowed; transform: none; box-shadow: none; }

                /* Modal Styles */
                .modal-overlay {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5);
                    display: flex; align-items: center; justify-content: center; z-index: 2000;
                }
                .modal-content {
                    background: white; padding: 30px; border-radius: 15px; width: 90%; max-width: 400px;
                    box-shadow: 0 10px 40px rgba(0,0,0,0.2); animation: slideUp 0.3s ease;
                }
                @keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
            `}</style>
        </div>
    );
};

export default SeatSelectionPage;