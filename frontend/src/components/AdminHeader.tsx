import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../App.css';

const AdminHeader: React.FC = () => {
    const navigate = useNavigate();

    const handleLogout = () => {
        // 관리자 로그아웃 로직 (현재는 단순히 홈으로 이동)
        if(confirm("관리자 모드를 종료하시겠습니까?")) {
            navigate('/');
        }
    };

    return (
        <header className="admin-header">
            <div className="header-inner">
                <Link to="/admin" className="admin-logo">
                    XRail <span className="admin-badge">ADMIN</span>
                </Link>
                
                <nav className="admin-nav" style={{ marginLeft: 'auto' }}>
                    <button onClick={handleLogout} className="action-btn logout-admin" style={{ padding: '12px 32px', fontSize: '1rem' }}>
                        나가기 ➜
                    </button>
                </nav>
            </div>
            <style>{`
                .admin-header {
                    background: #222;
                    height: 80px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #333;
                    position: sticky;
                    top: 0;
                    z-index: 1000;
                    width: 100%;
                    color: white;
                }
                .header-inner {
                    width: 100%;
                    max-width: 1600px;
                    margin: 0 auto;
                    padding: 0 40px;
                    display: flex;
                    align-items: center;
                }
                .admin-logo {
                    text-decoration: none;
                    display: flex;
                    align-items: center;
                    font-size: 28px;
                    font-weight: 900;
                    color: white;
                    letter-spacing: -1px;
                }
                .admin-badge {
                    background: #E60012;
                    font-size: 12px;
                    padding: 2px 6px;
                    border-radius: 4px;
                    margin-left: 8px;
                    vertical-align: middle;
                }
                .logout-admin {
                    background: #333;
                    border: 1px solid #555;
                    color: #ccc;
                }
                .logout-admin:hover {
                    background: #444;
                    color: white;
                }
            `}</style>
        </header>
    );
};

export default AdminHeader;