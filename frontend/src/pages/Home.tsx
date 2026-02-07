import React from 'react';
import { Link } from 'react-router-dom';
import Header from '../components/Header';
import '../App.css';

const Home: React.FC = () => {
    return (
        <div className="home-wrapper">
            <Header />
            <section className="hero-section">
                <div className="main-container">
                    <div className="hero-content">
                        <span className="hero-badge">Smart Choice for Travel</span>
                        <h1>가장 스마트한<br/><span className="text-highlight">철도 여행, XRail</span></h1>
                        <p>대한민국 어디든 빠르고 편리하게,<br/>XRail과 함께 특별한 여정을 시작하세요.</p>
                        <div className="hero-btns">
                            <Link to="/search" className="btn-hero">
                                승차권 예매하기
                            </Link>
                        </div>
                    </div>
                </div>
            </section>

            <section className="main-container menu-section">
                <div className="menu-grid">
                    <Link to="/search" className="menu-card">
                        <div className="menu-icon-wrap">🎫</div>
                        <h3>승차권 예약</h3>
                        <p>실시간 열차 조회 및 간편 좌석 선택</p>
                    </Link>
                    <Link to="/my-tickets" className="menu-card">
                        <div className="menu-icon-wrap">🔍</div>
                        <h3>나의 예약 확인</h3>
                        <p>예매 내역 확인 및 승차권 반환</p>
                    </Link>
                    <div className="menu-card">
                        <div className="menu-icon-wrap">🚆</div>
                        <h3>이용 안내</h3>
                        <p>노선 정보 및 승차권 이용 가이드</p>
                    </div>
                    <div className="menu-card">
                        <div className="menu-icon-wrap">🔔</div>
                        <h3>공지사항</h3>
                        <p>XRail의 새로운 소식과 이벤트 안내</p>
                    </div>
                </div>
            </section>

            <footer className="home-footer">
                <div className="main-container">
                    <p>© 2026 XRail. All rights reserved.</p>
                </div>
            </footer>

            <style>{`
                .home-wrapper { min-height: 100vh; display: flex; flex-direction: column; }
                
                .hero-section {
                    background: linear-gradient(135deg, #0055A5 0%, #003366 100%); /* Deep Blue Gradient */
                    color: white;
                    padding: 8rem 0 10rem;
                    position: relative;
                }
                
                .hero-content {
                    max-width: 600px;
                }
                
                .hero-badge {
                    display: inline-block; padding: 8px 20px; 
                    background: rgba(255,255,255,0.15); 
                    color: #fff; border-radius: 30px; font-weight: 700; margin-bottom: 2rem;
                    border: 1px solid rgba(255,255,255,0.3);
                    font-size: 0.9rem; letter-spacing: 0.5px;
                }
                
                h1 { font-size: 3.5rem; font-weight: 800; line-height: 1.2; margin-bottom: 1.5rem; }
                .text-highlight { color: #64b5f6; } /* Light Blue for contrast on dark bg */
                
                .hero-content p { font-size: 1.25rem; opacity: 0.9; line-height: 1.6; font-weight: 400; }
                
                .hero-btns { margin-top: 3rem; }
                .btn-hero {
                    display: inline-flex; align-items: center; justify-content: center;
                    background: white; color: #0055A5; font-weight: 800; font-size: 1.1rem;
                    padding: 1rem 3rem; border-radius: 50px; text-decoration: none;
                    transition: all 0.3s ease; box-shadow: 0 10px 20px rgba(0,0,0,0.2);
                }
                .btn-hero:hover { transform: translateY(-3px); box-shadow: 0 15px 30px rgba(0,0,0,0.3); background: #f8f9fa; }
                
                .menu-section { margin-top: -6rem; z-index: 10; position: relative; padding-bottom: 5rem; }
                .menu-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.5rem; }
                .menu-card {
                    background: white; padding: 2.5rem 2rem; border-radius: 1.5rem;
                    text-decoration: none; color: inherit; transition: all 0.3s ease;
                    box-shadow: 0 10px 30px rgba(0,0,0,0.05); border: 1px solid rgba(0,0,0,0.03);
                    display: flex; flex-direction: column; align-items: flex-start;
                }
                .menu-card:hover { transform: translateY(-10px); box-shadow: 0 20px 40px rgba(0,0,0,0.1); }
                
                .menu-icon-wrap { font-size: 3rem; margin-bottom: 1.5rem; background: #f0f7ff; padding: 1rem; border-radius: 1rem; width: 80px; height: 80px; display: flex; align-items: center; justify-content: center; }
                .menu-card h3 { font-size: 1.3rem; color: #333; margin-bottom: 0.5rem; font-weight: 700; }
                .menu-card p { color: #777; font-size: 0.95rem; line-height: 1.5; margin: 0; }
                
                .home-footer {
                    margin-top: auto; padding: 3rem 0; background: #f8f9fa; text-align: center; color: #999; font-size: 0.9rem;
                }
                
                @media (max-width: 900px) {
                    .menu-grid { grid-template-columns: repeat(2, 1fr); }
                    h1 { font-size: 2.8rem; }
                }
                @media (max-width: 600px) {
                    .hero-content { text-align: center; margin: 0 auto; }
                    .menu-grid { grid-template-columns: 1fr; }
                    .menu-card { align-items: center; text-align: center; }
                    .hero-section { padding: 6rem 0 8rem; }
                }
            `}</style>
        </div>
    );
};

export default Home;