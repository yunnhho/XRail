import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getStations } from '../services/stationService';
import type { Station } from '../services/stationService';
import { searchSchedules } from '../services/scheduleService';
import type { Schedule } from '../services/scheduleService';
import Header from '../components/Header';
import '../App.css';

const ScheduleSearchPage: React.FC = () => {
    const navigate = useNavigate();
    const [stations, setStations] = useState<Station[]>([]);
    const [departureId, setDepartureId] = useState<number>(0);
    const [arrivalId, setArrivalId] = useState<number>(0);
    const [date, setDate] = useState<string>(new Date().toISOString().split('T')[0]);
    const [schedules, setSchedules] = useState<Schedule[]>([]);
    const [loading, setLoading] = useState(false);
    const [stationsLoading, setStationsLoading] = useState(true);
    const [searched, setSearched] = useState(false);

    useEffect(() => {
        const fetchStations = async () => {
            try {
                const data = await getStations();
                if (data && data.length > 0) {
                    setStations(data);
                    setDepartureId(data[0].id);
                    setArrivalId(data[data.length - 1].id);
                }
            } catch (error) {
                console.error("역 목록 로딩 실패:", error);
                // 비회원도 조회 가능해야 하므로 에러 발생 시 알림
                // alert("역 정보를 불러오는데 실패했습니다."); // 너무 잦은 알림 방지 위해 주석 처리
            } finally {
                setStationsLoading(false);
            }
        };
        fetchStations();
    }, []);

    const handleSearch = async () => {
        if (departureId === arrivalId) {
            alert("출발역과 도착역이 같을 수 없습니다.");
            return;
        }
        setLoading(true);
        setSearched(true);
        try {
            const result = await searchSchedules(departureId, arrivalId, date); 
            setSchedules(result);
        } catch (error) {
            console.error("조회 실패", error);
            alert("열차 조회에 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    const handleSelect = (schedule: Schedule) => {
        if (isTooLate(schedule)) {
            alert("출발 5분 전에는 예약이 불가능합니다.");
            return;
        }
        const start = stations.find(s => s.id === Number(departureId));
        const end = stations.find(s => s.id === Number(arrivalId));
        if (!start || !end) return;

        navigate(`/reservation/${schedule.scheduleId}`, {
            state: { schedule, startStation: start, endStation: end }
        });
    };

    const isTooLate = (sch: Schedule) => {
        const departureStr = `${sch.departureDate}T${sch.departureTime}`;
        const departureTime = new Date(departureStr);
        const now = new Date();
        const diff = departureTime.getTime() - now.getTime();
        return diff < 5 * 60 * 1000; // 5분 미만 (300,000ms)
    };

    return (
        <div>
            <Header />
            <div className="main-container">
                <h2 className="page-title">승차권 조회 및 예매</h2>
                
                <div className="search-card">
                    <div className="search-flex">
                        <div className="input-box">
                            <label>출발역</label>
                            <select 
                                value={departureId} 
                                onChange={(e) => setDepartureId(Number(e.target.value))}
                                disabled={stationsLoading}
                            >
                                {stationsLoading ? <option>로딩 중...</option> : 
                                    stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)
                                }
                            </select>
                        </div>
                        <div className="swap-btn" onClick={() => {
                            const temp = departureId;
                            setDepartureId(arrivalId);
                            setArrivalId(temp);
                        }}>⇄</div>
                        <div className="input-box">
                            <label>도착역</label>
                            <select 
                                value={arrivalId} 
                                onChange={(e) => setArrivalId(Number(e.target.value))}
                                disabled={stationsLoading}
                            >
                                {stationsLoading ? <option>로딩 중...</option> : 
                                    stations.map(s => <option key={s.id} value={s.id}>{s.name}</option>)
                                }
                            </select>
                        </div>
                        <div className="input-box">
                            <label>출발일</label>
                            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
                        </div>
                        <button className="btn-search" onClick={handleSearch} disabled={loading || stationsLoading}>
                            {loading ? '조회 중...' : '열차 조회'}
                        </button>
                    </div>
                </div>

                {searched && (
                    <div className="table-container">
                        {schedules.length === 0 && !loading ? (
                            <div className="empty-msg">해당 날짜와 구간에 운행하는 열차가 없습니다.</div>
                        ) : (
                                <table className="web-table">
                                <thead>
                                    <tr>
                                        <th>열차종류/번호</th>
                                        <th>출발역/시간</th>
                                        <th>도착역/시간</th>
                                        <th>특실/우등</th>
                                        <th>일반실</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {schedules.map(sch => {
                                        const late = isTooLate(sch);
                                        const isUnavailable = late || sch.isSoldOut;

                                        return (
                                            <tr key={sch.scheduleId} style={isUnavailable ? { opacity: 0.5, background: '#f9f9f9' } : {}}>
                                                <td className="font-bold text-primary">
                                                    {sch.trainType}<br/>
                                                    <span style={{ fontSize: '0.85rem', color: '#666' }}>{sch.trainNumber}</span>
                                                </td>
                                                <td>
                                                    <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{sch.departureTime.substring(0, 5)}</div>
                                                    <div style={{ fontSize: '0.9rem' }}>{stations.find(s => s.id === departureId)?.name}</div>
                                                </td>
                                                <td>
                                                    <div style={{ fontSize: '1.4rem', fontWeight: 800 }}>{sch.arrivalTime.substring(0, 5)}</div>
                                                    <div style={{ fontSize: '0.9rem' }}>{stations.find(s => s.id === arrivalId)?.name}</div>
                                                </td>
                                                <td>
                                                    {/* 특실 - 롤백: 항상 매진 처리 */}
                                                    <button className="seat-btn-sm disabled" disabled>매진</button>
                                                </td>
                                                <td>
                                                    {isUnavailable ? (
                                                        <button className="seat-btn-sm disabled" disabled>
                                                            {late ? '-' : '매진'}
                                                        </button>
                                                    ) : (
                                                        <button 
                                                            className="seat-btn-sm" 
                                                            onClick={() => handleSelect(sch)}
                                                            style={{ minWidth: '100px' }}
                                                        >
                                                            {(sch.price || 0).toLocaleString()}원
                                                        </button>
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        )}
                    </div>
                )}
            </div>
            <style>{`
                .page-title { border-left: 6px solid var(--primary); padding-left: 15px; margin-bottom: 2.5rem; }
                .swap-btn {
                    font-size: 2rem; color: #aaa; cursor: pointer; padding-bottom: 5px;
                    transition: color 0.2s;
                }
                .swap-btn:hover { color: var(--primary); }
                
                .seat-btn-sm {
                    padding: 8px 24px; border: 1.5px solid var(--primary); background: white; color: var(--primary);
                    border-radius: 4px; font-weight: 700; cursor: pointer; transition: 0.2s;
                }
                .seat-btn-sm:hover { background: var(--primary); color: white; }
                .seat-btn-sm.disabled { border-color: #ddd; color: #ccc; cursor: default; }
                
                .empty-msg { text-align: center; padding: 100px; color: #999; font-size: 1.2rem; }
            `}</style>
        </div>
    );
};

export default ScheduleSearchPage;
