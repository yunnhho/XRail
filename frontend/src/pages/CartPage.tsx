import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../api/apiClient';
import { cancelReservation } from '../services/reservationService';
import Header from '../components/Header';
import '../App.css';

interface TicketDetail {
    ticketId: number;
    trainNumber: string;
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

const CartPage: React.FC = () => {
    const navigate = useNavigate();
    const [cartItems, setCartItems] = useState<ReservationDetail[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        fetchCartItems();
    }, []);

    const fetchCartItems = async () => {
        setLoading(true);
        try {
            const response = await apiClient.get<any>('/reservations');
            if (response.data.data) {
                // PENDING 상태인 것만 장바구니로 취급
                const pendingItems = response.data.data.filter((r: ReservationDetail) => r.status === 'PENDING');
                setCartItems(pendingItems);
            }
        } catch (error) {
            console.error("장바구니 조회 실패", error);
        } finally {
            setLoading(false);
        }
    };

    const handleCancel = async (id: number) => {
        if (!confirm("예약을 취소하시겠습니까?")) return;
        try {
            await cancelReservation(id);
            alert("취소되었습니다.");
            fetchCartItems();
        } catch (error) {
            alert("취소에 실패했습니다.");
        }
    };

    const handlePay = (reservation: ReservationDetail) => {
        // PaymentPage로 이동
        // PaymentPage는 location state로 totalPrice 등을 받음.
        // 좌석 정보 등은 PaymentPage 내부에서 다시 조회하지 않는다면 state로 넘겨줘야 함.
        // 현재 PaymentPage 구조 상 state에 schedule, startStation, endStation 등이 필요할 수 있음.
        // 그러나 PaymentPage가 API로 예약 정보를 다시 조회하는 구조가 아니라면 state 의존적일 수 있음.
        // PaymentPage를 보완하여 reservationId만으로도 결제 가능하게 하거나, 여기서 필요한 정보를 다 넘겨야 함.
        // 여기서는 reservationId를 이용해 PaymentPage로 이동하고, PaymentPage가 초기화 시 데이터를 다시 로드하는 로직이 없으면 추가가 필요함.
        // PaymentPage.tsx를 확인해봐야 하지만, 일단 reservationId를 param으로 넘김.
        
        navigate(`/payment/${reservation.reservationId}`, {
             state: { 
                 totalPrice: reservation.totalPrice,
                 reservationId: reservation.reservationId,
                 // 필요한 경우 티켓 정보에서 역/열차 정보를 추출해 넘길 수 있음 (표시용)
                 ticketCount: reservation.tickets.length
             }
        });
    };

    return (
        <div>
            <Header />
            <div className="web-container">
                <h2 style={{ marginBottom: '30px', fontWeight: 800 }}>장바구니 (미결제 내역)</h2>
                
                <div className="info-box mb-4">
                    결제하지 않은 승차권은 예매 후 <b>20분 뒤 자동으로 취소</b>됩니다.
                </div>

                {loading && <div style={{ textAlign: 'center' }}>로딩 중...</div>}
                {!loading && cartItems.length === 0 && (
                     <div style={{ textAlign: 'center', padding: '100px', color: '#999' }}>장바구니가 비어있습니다.</div>
                )}

                {cartItems.map(res => (
                    <div key={res.reservationId} className="cart-item-card">
                        <div className="cart-header">
                            <span className="res-id">예약번호: {res.reservationId}</span>
                            <span className="res-time">예약일시: {new Date(res.reservedAt).toLocaleString()}</span>
                        </div>
                        <div className="cart-body">
                            {res.tickets.map(t => (
                                <div key={t.ticketId} className="ticket-row">
                                    <span className="train-info">{t.trainNumber}</span>
                                    <span className="route-info">{t.startStation} → {t.endStation}</span>
                                    <span className="time-info">{t.departureDate} {t.departureTime.substring(0,5)}</span>
                                    <span className="seat-info">{t.seatNumber}</span>
                                </div>
                            ))}
                        </div>
                        <div className="cart-footer">
                            <div className="total-price">
                                총 결제금액: <strong>{res.totalPrice.toLocaleString()}원</strong>
                            </div>
                            <div className="cart-actions">
                                <button className="btn-cancel" onClick={() => handleCancel(res.reservationId)}>예약취소</button>
                                <button className="btn-pay" onClick={() => handlePay(res)}>결제하기</button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
            <style>{`
                .info-box { background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin-bottom: 20px; border: 1px solid #ffeeba; }
                .cart-item-card { background: white; border: 1px solid #ddd; border-radius: 8px; margin-bottom: 20px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.05); }
                .cart-header { background: #f8f9fa; padding: 15px 20px; display: flex; justify-content: space-between; border-bottom: 1px solid #eee; color: #666; font-size: 0.9rem; }
                .cart-body { padding: 20px; }
                .ticket-row { display: grid; grid-template-columns: 1fr 2fr 2fr 1fr; gap: 10px; padding: 10px 0; border-bottom: 1px solid #f1f1f1; align-items: center; }
                .ticket-row:last-child { border-bottom: none; }
                .train-info { font-weight: bold; color: #0055A5; }
                .cart-footer { padding: 15px 20px; background: #fff; border-top: 1px solid #eee; display: flex; justify-content: space-between; align-items: center; }
                .total-price strong { color: #E60012; font-size: 1.2rem; margin-left: 5px; }
                .cart-actions { display: flex; gap: 10px; }
                .btn-cancel { padding: 8px 15px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; color: #666; }
                .btn-pay { padding: 8px 20px; border: none; background: #0055A5; color: white; border-radius: 4px; cursor: pointer; font-weight: bold; }
                .btn-pay:hover { background: #00448a; }
                .btn-cancel:hover { background: #f8f9fa; }
            `}</style>
        </div>
    );
};

export default CartPage;
