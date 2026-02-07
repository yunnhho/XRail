import React, { useEffect, useState } from 'react';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import '../App.css';

const Header: React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    const checkLoginStatus = () => {
        const token = localStorage.getItem('accessToken');
        const role = localStorage.getItem('role');
        setIsLoggedIn(!!token && role !== 'GUEST');
    };

    useEffect(() => {
        checkLoginStatus();
        
        // [Fix] 로그인/로그아웃 이벤트 리스너 추가
        window.addEventListener('authChange', checkLoginStatus);
        return () => {
            window.removeEventListener('authChange', checkLoginStatus);
        };
    }, [location]); 

    const handleLogout = () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('role');
        setIsLoggedIn(false);
        
        // 로그아웃 시에도 이벤트 발생
        window.dispatchEvent(new Event('authChange'));
        
        navigate('/');
    };

    return (
        <header className="web-header">
            <div className="header-inner">
                <Link to="/" className="main-logo">
                    <span className="logo-x">X</span>
                    <span className="logo-rail">Rail</span>
                </Link>
                
                <nav className="main-nav">
                    <Link to="/search" className={location.pathname === '/search' ? 'active' : ''}>승차권 예약</Link>
                    {isLoggedIn && (
                        <Link to="/cart" className={location.pathname === '/cart' ? 'active' : ''}>
                            🛒 장바구니
                        </Link>
                    )}
                    <Link to="/my-tickets" className={location.pathname === '/my-tickets' ? 'active' : ''}>나의 예약</Link>
                    <a href="#">이용안내</a>
                    <a href="#">고객센터</a>
                </nav>

                <div className="header-actions">
                    {isLoggedIn ? (
                        <div className="user-info">
                            <span className="welcome-msg">반갑습니다!</span>
                            <button onClick={handleLogout} className="action-btn logout">로그아웃</button>
                        </div>
                    ) : (
                        <div className="auth-btns">
                            <Link to="/login" className="action-btn login">로그인</Link>
                            <Link to="/register" className="action-btn signup">회원가입</Link>
                        </div>
                    )}
                </div>
            </div>
            <style>{`
                .web-header {
                    background: #fff;
                    height: 100px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #eef2f7;
                    position: sticky;
                    top: 0;
                    z-index: 1000;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.03);
                    width: 100%;
                }
                .header-inner {
                    width: 100%;
                    max-width: 1600px; /* 창을 넓게 쓰기 위해 최대 너비 확장 */
                    margin: 0 auto;
                    padding: 0 40px;
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                
                /* 로고 디자인 수정: 혼동을 주던 'X' 버튼 제거 및 세련된 텍스트 로고 */
                .main-logo {
                    text-decoration: none;
                    display: flex;
                    align-items: center;
                    font-size: 36px;
                    font-weight: 900;
                    letter-spacing: -1.5px;
                }
                .logo-x {
                    color: #fff;
                    background: #0055A5;
                    width: 48px;
                    height: 48px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    border-radius: 12px;
                    margin-right: 8px;
                    font-size: 32px;
                }
                .logo-rail {
                    color: #0055A5;
                }

                .main-nav {
                    display: flex;
                    gap: 50px;
                    position: absolute;
                    left: 50%;
                    transform: translateX(-50%);
                }
                .main-nav a {
                    text-decoration: none;
                    color: #444;
                    font-size: 18px;
                    font-weight: 600;
                    padding: 10px 0;
                    border-bottom: 3px solid transparent;
                    transition: all 0.2s ease;
                }
                .main-nav a:hover, .main-nav a.active {
                    color: #0055A5;
                    border-bottom-color: #0055A5;
                }

                .header-actions {
                    display: flex;
                    align-items: center;
                    gap: 15px;
                }
                .user-info {
                    display: flex;
                    align-items: center;
                    gap: 15px;
                }
                .welcome-msg {
                    font-size: 15px;
                    color: #777;
                    font-weight: 500;
                }
                .action-btn {
                    padding: 10px 24px;
                    border-radius: 8px;
                    font-size: 15px;
                    font-weight: 700;
                    text-decoration: none;
                    cursor: pointer;
                    transition: all 0.2s;
                    border: none;
                }
                .login {
                    background: #f1f5f9;
                    color: #475569;
                }
                .login:hover { background: #e2e8f0; }
                .signup, .logout {
                    background: #0055A5;
                    color: white;
                }
                .signup:hover, .logout:hover { background: #003d7a; }
                
                @media (max-width: 1100px) {
                    .main-nav { position: static; transform: none; gap: 30px; }
                }
                @media (max-width: 850px) {
                    .main-nav { display: none; }
                }
            `}</style>
        </header>
    );
};

export default Header;