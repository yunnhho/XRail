import React, { useEffect, useState } from 'react';
import apiClient from '../api/apiClient';
import { cancelReservation } from '../services/reservationService';
import Header from '../components/Header';
import '../App.css';

interface TicketDetail {
    ticketId: number;
    trainNumber: string;
    carriageNumber: number; // 호차 추가
    seatNumber: string;
    startStation: string;
    endStation: string;
    departureTime: string;
    arrivalTime: string;
    departureDate: string;
}

interface ReservationDetail {
    reservationId: number;
    status: 'PENDING' | 'PAID' | 'CANCELLED';
    totalPrice: number;
    reservedAt: string;
    tickets: TicketDetail[];
}

const MyTicketPage: React.FC = () => {
    const [reservations, setReservations] = useState<ReservationDetail[]>([]);
    const [loading, setLoading] = useState(false);
    const [isLoggedIn, setIsLoggedIn] = useState(false);
    
    // Pagination State
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    // 비회원 조회용 상태
    const [accessCode, setAccessCode] = useState('');
    const [password, setPassword] = useState('');

    useEffect(() => {
        const token = localStorage.getItem('accessToken');
        if (token) {
            setIsLoggedIn(true);
            fetchReservations();
        } else {
            setIsLoggedIn(false);
        }
    }, [page]); // 페이지 변경 시 재조회

    const fetchReservations = async () => {
        setLoading(true);
        try {
            const response = await apiClient.get<any>('/reservations', {
                params: { page, size: 5 }
            });
            // Pagination Response 처리
            if (response.data.data && response.data.data.content) {
                const historyItems = response.data.data.content.filter((r: ReservationDetail) => r.status !== 'PENDING');
                setReservations(historyItems);
                setTotalPages(response.data.data.totalPages);
            }
        } catch (error) {
            console.error("조회 실패", error);
        } finally {
            setLoading(false);
        }
    };

    const handleGuestLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await apiClient.post('/auth/guest/login', {
                accessCode,
                password
            });
            localStorage.setItem('accessToken', response.data.accessToken);
            localStorage.setItem('role', 'GUEST'); // [Flag] 비회원 조회 모드
            
            // [Fix] 헤더 상태 업데이트 유도
            window.dispatchEvent(new Event('authChange'));
            
            setIsLoggedIn(true);
            fetchReservations();
        } catch (error: any) {
            alert(error.response?.data?.message || "조회 정보가 올바르지 않습니다.");
        }
    };

    const handleCancel = async (id: number) => {
        if (!confirm("정말 예약을 취소하시겠습니까?")) return;
        try {
            await cancelReservation(id);
            alert("취소되었습니다.");
            fetchReservations();
        } catch (error) {
            alert("취소에 실패했습니다.");
        }
    };

    if (!isLoggedIn) {
        return (
            <div>
                <Header />
                <div className="web-container" style={{ display: 'flex', justifyContent: 'center', paddingTop: '50px' }}>
                    <div className="guest-login-card">
                        <h2>비회원 예약 조회</h2>
                        <p style={{ color: '#666', marginBottom: '2rem' }}>예매 시 발급받은 예매번호와 비밀번호를 입력해주세요.</p>
                        <form onSubmit={handleGuestLogin} className="login-form">
                            <div className="input-box">
                                <label>예매번호 (Access Code)</label>
                                <input type="text" value={accessCode} onChange={(e) => setAccessCode(e.target.value)} placeholder="예매번호 입력" required />
                            </div>
                            <div className="input-box">
                                <label>비밀번호 (6자리 숫자)</label>
                                <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} maxLength={6} placeholder="비밀번호" required />
                            </div>
                            <button type="submit" className="btn-search full-width mt-2">내 티켓 조회하기</button>
                        </form>
                    </div>
                </div>
                <style>{`
                    .guest-login-card {
                        background: white; padding: 3rem; border-radius: 1rem; width: 100%; max-width: 500px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.1); text-align: center;
                    }
                    .login-form { text-align: left; }
                    .full-width { width: 100%; }
                    .mt-2 { margin-top: 1.5rem; }
                `}</style>
            </div>
        );
    }

    return (
        <div>
            <Header />
            <div className="web-container">
                <h2 style={{ marginBottom: '30px', fontWeight: 800 }}>나의 예약 내역</h2>
                
                {loading && <div style={{ textAlign: 'center' }}>로딩 중...</div>}
                {!loading && reservations.length === 0 && (
                     <div style={{ textAlign: 'center', padding: '100px', color: '#999' }}>예매 내역이 없습니다.</div>
                )}

                {reservations.length > 0 && (
                    <div className="table-wrapper">
                        <table className="web-table">
                            <thead>
                                <tr>
                                    <th>예약번호</th>
                                    <th>출발일</th>
                                    <th>열차</th>
                                    <th>구간</th>
                                    <th>출발/도착 시간</th>
                                    <th>좌석</th>
                                    <th>상태</th>
                                    <th>관리</th>
                                </tr>
                            </thead>
                            <tbody>
                                {reservations.flatMap(res => 
                                    res.tickets.map((t, idx) => (
                                        <tr key={t.ticketId}>
                                            {idx === 0 && <td rowSpan={res.tickets.length}>{res.reservationId}</td>}
                                            <td>{t.departureDate}</td>
                                            <td>{t.trainNumber}</td>
                                            <td>{t.startStation} → {t.endStation}</td>
                                            <td><strong>{t.departureTime.substring(0,5)}</strong> / {t.arrivalTime.substring(0,5)}</td>
                                            <td>{t.carriageNumber}호차 {t.seatNumber}</td>
                                            {idx === 0 && (
                                                <>
                                                    <td rowSpan={res.tickets.length}>{res.status}</td>
                                                    <td rowSpan={res.tickets.length}>
                                                        {res.status !== 'CANCELLED' && <button onClick={() => handleCancel(res.reservationId)} className="cancel-btn">반환</button>}
                                                    </td>
                                                </>
                                            )}
                                        </tr>
                                    ))
                                )}
                            </tbody>
                        </table>
                        
                        {/* Pagination Controls */}
                        {totalPages > 1 && (
                            <div className="pagination" style={{ marginTop: '20px', display: 'flex', justifyContent: 'center', gap: '20px' }}>
                                <button 
                                    onClick={() => setPage(p => Math.max(0, p - 1))} 
                                    disabled={page === 0}
                                    style={{ padding: '8px 16px', border: '1px solid #ddd', background: 'white', borderRadius: '4px', cursor: page === 0 ? 'default' : 'pointer', opacity: page === 0 ? 0.5 : 1 }}
                                >
                                    &lt; 이전
                                </button>
                                <span style={{ display: 'flex', alignItems: 'center' }}>Page {page + 1} / {totalPages}</span>
                                <button 
                                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} 
                                    disabled={page >= totalPages - 1}
                                    style={{ padding: '8px 16px', border: '1px solid #ddd', background: 'white', borderRadius: '4px', cursor: page >= totalPages - 1 ? 'default' : 'pointer', opacity: page >= totalPages - 1 ? 0.5 : 1 }}
                                >
                                    다음 &gt;
                                </button>
                            </div>
                        )}
                    </div>
                )}
            </div>
            <style>{`
                .cancel-btn { padding: 6px 15px; background: white; border: 1px solid #d32f2f; color: #d32f2f; border-radius: 4px; cursor: pointer; }
                .cancel-btn:hover { background: #ffebee; }
            `}</style>
        </div>
    );
};

export default MyTicketPage;
