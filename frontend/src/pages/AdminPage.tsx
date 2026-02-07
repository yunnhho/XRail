import React, { useEffect, useState } from 'react';
import apiClient from '../api/apiClient';
import AdminHeader from '../components/AdminHeader';
import '../App.css';

// DTO Interfaces
interface DailyStats {
    totalRevenue: number;
    totalTickets: number;
}
interface AdminSchedule {
    id: number;
    routeName: string;
    trainInfo: string;
    dateTime: string;
    arrivalTime: string;
}
interface AdminTicket {
    ticketId: number;
    reservationId: number;
    userType: string;
    userName: string; // Simplified Name
    userDetail: string; // Full Detail for Modal
    routeInfo: string;
    seatInfo: string;
    trainInfo: string;
    status: string;
}
interface PageResponse<T> {
    content: T[];
    totalPages: number;
    number: number; // current page
}

type SortDirection = 'asc' | 'desc' | '';

const AdminPage: React.FC = () => {
    const [activeTab, setActiveTab] = useState<'dashboard' | 'schedules' | 'tickets'>('dashboard');
    
    // Data States
    const [stats, setStats] = useState<DailyStats>({ totalRevenue: 0, totalTickets: 0 });
    const [schedules, setSchedules] = useState<AdminSchedule[]>([]);
    const [tickets, setTickets] = useState<AdminTicket[]>([]);
    
    // Filter & Pagination States
    const [filterDate, setFilterDate] = useState<string>(new Date().toISOString().split('T')[0]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(false);

    // Sorting State
    const [sortConfig, setSortConfig] = useState<{ key: string; direction: SortDirection }>({ key: '', direction: '' });

    // Modal State
    const [selectedUser, setSelectedUser] = useState<{ name: string; detail: string } | null>(null);

    useEffect(() => {
        setPage(0); // 탭이나 날짜 변경 시 페이지 리셋
        setSortConfig({ key: '', direction: '' }); // 정렬 초기화
    }, [activeTab, filterDate]);

    useEffect(() => {
        fetchData();
    }, [activeTab, filterDate, page, sortConfig]);

    const fetchData = async () => {
        setLoading(true);
        try {
            const sortParam = sortConfig.key && sortConfig.direction ? [`${sortConfig.key},${sortConfig.direction}`] : undefined;

            if (activeTab === 'dashboard') {
                const res = await apiClient.get<DailyStats>('/admin/stats');
                setStats(res.data);
            } else if (activeTab === 'schedules') {
                const res = await apiClient.get<PageResponse<AdminSchedule>>('/admin/schedules', {
                    params: { date: filterDate, page, size: 10, sort: sortParam }
                });
                setSchedules(res.data.content);
                setTotalPages(res.data.totalPages);
            } else if (activeTab === 'tickets') {
                const res = await apiClient.get<PageResponse<AdminTicket>>('/admin/tickets', {
                    params: { date: filterDate, page, size: 10, sort: sortParam }
                });
                setTickets(res.data.content);
                setTotalPages(res.data.totalPages);
            }
        } catch (error) {
            console.error("Data Load Error", error);
        } finally {
            setLoading(false);
        }
    };

    const requestSort = (key: string) => {
        let direction: SortDirection = 'asc';
        if (sortConfig.key === key && sortConfig.direction === 'asc') {
            direction = 'desc';
        } else if (sortConfig.key === key && sortConfig.direction === 'desc') {
            direction = ''; // Reset
            key = '';
        }
        setSortConfig({ key, direction });
    };

    const getSortIndicator = (key: string) => {
        if (sortConfig.key !== key) return '↕';
        if (sortConfig.direction === 'asc') return '↑';
        if (sortConfig.direction === 'desc') return '↓';
        return '↕';
    };

    const handleReset = async () => {
        if (!confirm("모든 데이터를 초기화하시겠습니까? (경고: 모든 예약 삭제됨)")) return;
        try {
            await apiClient.post('/admin/reset-data');
            alert("초기화 완료");
            window.location.reload();
        } catch (error) {
            alert("초기화 실패");
        }
    };

    const handlePageChange = (newPage: number) => {
        if (newPage >= 0 && newPage < totalPages) {
            setPage(newPage);
        }
    };

    return (
        <div style={{ background: '#f4f6f8', minHeight: '100vh' }}>
            <AdminHeader />
            <div className="web-container" style={{ maxWidth: '1400px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '30px' }}>
                    <h2 style={{ fontSize: '1.8rem', fontWeight: 800, color: '#333' }}>관리자 대시보드</h2>
                    <button onClick={handleReset} className="reset-btn">
                        ⚠️ 시스템 데이터 초기화
                    </button>
                </div>

                <div className="admin-tabs">
                    <button className={`tab ${activeTab === 'dashboard' ? 'active' : ''}`} onClick={() => setActiveTab('dashboard')}>📊 대시보드</button>
                    <button className={`tab ${activeTab === 'schedules' ? 'active' : ''}`} onClick={() => setActiveTab('schedules')}>📅 스케줄 관리</button>
                    <button className={`tab ${activeTab === 'tickets' ? 'active' : ''}`} onClick={() => setActiveTab('tickets')}>🎟️ 티켓 예매 현황</button>
                </div>

                {/* Dashboard View */}
                {activeTab === 'dashboard' && (
                    <div className="dashboard-grid fade-in">
                        <div className="stat-card">
                            <h3>오늘의 총 매출</h3>
                            <div className="stat-value text-primary">{(stats.totalRevenue || 0).toLocaleString()}원</div>
                            <p>실시간 집계 (결제 완료 기준)</p>
                        </div>
                        <div className="stat-card">
                            <h3>오늘 예매된 티켓</h3>
                            <div className="stat-value">{(stats.totalTickets || 0).toLocaleString()}매</div>
                            <p>실시간 집계 (취소 제외)</p>
                        </div>
                    </div>
                )}

                {/* Filter Bar (Shared for Schedules & Tickets) */}
                {activeTab !== 'dashboard' && (
                    <div className="filter-bar fade-in">
                        <label>날짜 조회: </label>
                        <input type="date" value={filterDate} onChange={(e) => setFilterDate(e.target.value)} />
                        <span style={{ marginLeft: '10px', fontSize: '0.9rem', color: '#666' }}>※ 선택한 날짜의 데이터만 표시됩니다.</span>
                    </div>
                )}

                {/* Table View */}
                {activeTab !== 'dashboard' && (
                    <div className="table-wrapper fade-in" style={{ background: 'white', padding: '0', borderRadius: '8px', overflow: 'hidden', boxShadow: '0 2px 8px rgba(0,0,0,0.05)' }}>
                        {loading && <div style={{ padding: '40px', textAlign: 'center' }}>데이터 로딩 중...</div>}
                        
                        {!loading && activeTab === 'schedules' && (
                            <table className="admin-table">
                                <colgroup>
                                    <col style={{ width: '10%' }} />
                                    <col style={{ width: '20%' }} />
                                    <col style={{ width: '25%' }} />
                                    <col style={{ width: '25%' }} />
                                    <col style={{ width: '20%' }} />
                                </colgroup>
                                <thead>
                                    <tr>
                                        <th onClick={() => requestSort('id')} style={{ cursor: 'pointer' }}>ID {getSortIndicator('id')}</th>
                                        <th>노선</th>
                                        <th>열차 정보</th>
                                        <th onClick={() => requestSort('departureDate')} style={{ cursor: 'pointer' }}>출발 일시 {getSortIndicator('departureDate')}</th>
                                        <th>도착 시간</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {schedules.map(s => (
                                        <tr key={s.id}>
                                            <td>{s.id}</td>
                                            <td><span className="route-badge">{s.routeName}</span></td>
                                            <td>{s.trainInfo}</td>
                                            <td>{s.dateTime}</td>
                                            <td>{s.arrivalTime}</td>
                                        </tr>
                                    ))}
                                    {schedules.length === 0 && <tr><td colSpan={5} className="empty-cell">데이터가 없습니다.</td></tr>}
                                </tbody>
                            </table>
                        )}

                        {!loading && activeTab === 'tickets' && (
                            <table className="admin-table">
                                <colgroup>
                                    <col style={{ width: '10%' }} />
                                    <col style={{ width: '10%' }} />
                                    <col style={{ width: '15%' }} />
                                    <col style={{ width: '10%' }} />
                                    <col style={{ width: '20%' }} />
                                    <col style={{ width: '20%' }} />
                                    <col style={{ width: '15%' }} />
                                </colgroup>
                                <thead>
                                    <tr>
                                        <th onClick={() => requestSort('id')} style={{ cursor: 'pointer' }}>T-ID {getSortIndicator('id')}</th>
                                        <th onClick={() => requestSort('reservationId')} style={{ cursor: 'pointer' }}>Res ID {getSortIndicator('reservationId')}</th>
                                        <th>예약자</th>
                                        <th>유형</th>
                                        <th>경로</th>
                                        <th>열차/좌석</th>
                                        <th onClick={() => requestSort('status')} style={{ cursor: 'pointer' }}>상태 {getSortIndicator('status')}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {tickets.map(t => (
                                        <tr key={t.ticketId}>
                                            <td>{t.ticketId}</td>
                                            <td>{t.reservationId}</td>
                                            <td>
                                                <button 
                                                    className="user-link-btn" 
                                                    onClick={() => setSelectedUser({ name: t.userName, detail: t.userDetail })}
                                                >
                                                    {t.userName}
                                                </button>
                                            </td>
                                            <td style={{ textAlign: 'center' }}><span className={`badge ${t.userType === 'MEMBER' ? 'member' : 'guest'}`}>{t.userType}</span></td>
                                            <td>{t.routeInfo}</td>
                                            <td>{t.trainInfo} <span style={{ color: '#888' }}>|</span> {t.seatInfo}</td>
                                            <td style={{ textAlign: 'center' }}><span className={`status-badge ${t.status}`}>{t.status}</span></td>
                                        </tr>
                                    ))}
                                    {tickets.length === 0 && <tr><td colSpan={7} className="empty-cell">데이터가 없습니다.</td></tr>}
                                </tbody>
                            </table>
                        )}
                        
                        {/* Pagination */}
                        <div className="pagination">
                            <button onClick={() => handlePageChange(page - 1)} disabled={page === 0}>&lt; 이전</button>
                            <span>Page {page + 1} / {totalPages || 1}</span>
                            <button onClick={() => handlePageChange(page + 1)} disabled={page >= totalPages - 1}>다음 &gt;</button>
                        </div>
                    </div>
                )}
            </div>

            {/* User Detail Modal */}
            {selectedUser && (
                <div className="modal-overlay" onClick={() => setSelectedUser(null)}>
                    <div className="modal-content" onClick={e => e.stopPropagation()} style={{ padding: '0', overflow: 'hidden' }}>
                        <div style={{ background: '#0055A5', color: 'white', padding: '20px 30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <h3 style={{ margin: 0 }}>상세 사용자 정보</h3>
                            <button onClick={() => setSelectedUser(null)} style={{ background: 'none', border: 'none', color: 'white', fontSize: '1.5rem', cursor: 'pointer' }}>&times;</button>
                        </div>
                        <div style={{ padding: '30px' }}>
                            <div className="user-info-box">
                                {selectedUser.detail.split('\n').map((line, i) => {
                                    const [label, ...value] = line.split(':');
                                    return (
                                        <div key={i} style={{ display: 'flex', padding: '12px 0', borderBottom: '1px solid #f0f0f0' }}>
                                            <span style={{ width: '120px', fontWeight: 'bold', color: '#666' }}>{label.trim()}</span>
                                            <span style={{ flex: 1, color: '#333' }}>{value.join(':').trim()}</span>
                                        </div>
                                    );
                                })}
                            </div>
                            <div style={{ textAlign: 'center', marginTop: '30px' }}>
                                <button className="btn-primary" onClick={() => setSelectedUser(null)} style={{ padding: '10px 40px' }}>닫기</button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            <style>{`
                .admin-tabs { display: flex; gap: 10px; margin-bottom: 20px; border-bottom: 2px solid #ddd; }
                .tab { padding: 12px 24px; background: none; border: none; cursor: pointer; font-size: 1.1rem; font-weight: bold; color: #666; transition: 0.2s; }
                .tab.active { color: #0055A5; border-bottom: 4px solid #0055A5; margin-bottom: -2px; }
                .tab:hover { color: #333; background: rgba(0,0,0,0.03); }

                .dashboard-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; margin-bottom: 40px; }
                .stat-card { background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); border: 1px solid #eee; }
                .stat-card h3 { color: #666; font-size: 1.1rem; margin-bottom: 10px; }
                .stat-value { font-size: 2.5rem; font-weight: 800; margin-bottom: 5px; }
                .stat-card p { font-size: 0.9rem; color: #999; }

                .filter-bar { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; display: flex; align-items: center; box-shadow: 0 2px 5px rgba(0,0,0,0.03); }
                .filter-bar input { padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 1rem; }

                .admin-table { width: 100%; border-collapse: collapse; table-layout: fixed; }
                .admin-table th { background: #f8f9fa; padding: 15px; text-align: center; border-bottom: 2px solid #eee; color: #555; font-size: 0.95rem; user-select: none; }
                .admin-table td { padding: 15px; border-bottom: 1px solid #eee; color: #333; font-size: 0.95rem; vertical-align: middle; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
                .empty-cell { text-align: center; padding: 50px; color: #999; }

                .pagination { display: flex; justify-content: center; align-items: center; gap: 20px; padding: 20px; border-top: 1px solid #eee; background: #fafafa; }
                .pagination button { padding: 8px 16px; border: 1px solid #ddd; background: white; border-radius: 4px; cursor: pointer; }
                .pagination button:disabled { background: #eee; color: #aaa; cursor: default; }

                .reset-btn { background: #d32f2f; color: white; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; transition: 0.2s; }
                .reset-btn:hover { background: #b71c1c; }

                .route-badge { background: #e3f2fd; color: #1565c0; padding: 4px 8px; border-radius: 4px; font-weight: 600; font-size: 0.85rem; }
                .status-badge { padding: 4px 8px; border-radius: 4px; font-weight: 600; font-size: 0.8rem; }
                .status-badge.PAID { background: #e8f5e9; color: #2e7d32; }
                .status-badge.PENDING { background: #fff3e0; color: #ef6c00; }
                .status-badge.CANCELLED { background: #ffebee; color: #c62828; }
                
                .badge { padding: 4px 8px; border-radius: 4px; font-size: 0.8rem; font-weight: bold; color: white; }
                .badge.member { background: #0055A5; }
                .badge.guest { background: #E60012; }

                .user-link-btn { background: none; border: none; color: #0055A5; font-weight: bold; cursor: pointer; text-decoration: underline; padding: 0; font-size: 0.95rem; }
                .user-link-btn:hover { color: #003d7a; }

                /* Modal Styles */
                .modal-overlay {
                    position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5);
                    display: flex; align-items: center; justify-content: center; z-index: 2000;
                }
                .modal-content {
                    background: white; padding: 30px; border-radius: 12px; width: 90%; max-width: 500px;
                    box-shadow: 0 10px 40px rgba(0,0,0,0.2); animation: slideUp 0.3s ease;
                }
                @keyframes slideUp { from { opacity: 0; transform: translateY(30px); } to { opacity: 1; transform: translateY(0); } }
            `}</style>
        </div>
    );
};

export default AdminPage;