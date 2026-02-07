import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/apiClient';
import '../App.css';

const LoginPage: React.FC = () => {
    const navigate = useNavigate();
    const [loginId, setLoginId] = useState('');
    const [password, setPassword] = useState('');

    const handleSocialLogin = (provider: string) => {
        window.location.href = `http://localhost:8088/oauth2/authorization/${provider}`;
    };

    const handleMemberLogin = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            const response = await apiClient.post('/auth/login', { loginId, password });
            localStorage.setItem('accessToken', response.data.accessToken);
            localStorage.setItem('refreshToken', response.data.refreshToken);
            localStorage.removeItem('role'); // 기존 비회원 플래그가 있다면 삭제
            
            // [Fix] 헤더 상태 즉시 업데이트를 위한 이벤트 발생
            window.dispatchEvent(new Event('authChange'));
            
            navigate('/');
        } catch (error: any) {
            alert(error.response?.data?.message || "아이디 또는 비밀번호가 틀렸습니다.");
        }
    };

    return (
        <div className="auth-wrapper">
            <div className="auth-card-wide">
                <div className="auth-welcome">
                    <h1>반가워요!<br/><span className="text-highlight-login">XRail</span> 입니다.</h1>
                    <p>스마트한 철도 여행의 시작, 로그인을 통해 더 많은 혜택을 누리세요.<br/>비회원 예약은 '나의 예약' 메뉴를 이용해주세요.</p>
                </div>
                
                <div className="auth-form-section">
                    <h2 style={{ fontSize: '1.8rem', marginBottom: '2rem', color: '#333' }}>회원 로그인</h2>
                    
                    <form onSubmit={handleMemberLogin} className="login-form fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '1.2rem' }}>
                        <div className="auth-input-group">
                            <label>아이디</label>
                            <input type="text" value={loginId} onChange={(e) => setLoginId(e.target.value)} placeholder="아이디" required />
                        </div>
                        <div className="auth-input-group">
                            <label>비밀번호</label>
                            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" required />
                        </div>
                        <button type="submit" className="btn-primary" style={{ height: '3.5rem', fontSize: '1.1rem', marginTop: '1.5rem' }}>로그인</button>
                        
                        <div className="divider"><span>또는 소셜 계정으로 로그인</span></div>
                        <div className="social-grid">
                            <button type="button" onClick={() => handleSocialLogin('kakao')} className="social-btn kakao-btn">
                                <span className="icon">💬</span> 카카오
                            </button>
                            <button type="button" onClick={() => handleSocialLogin('naver')} className="social-btn naver-btn">
                                <span className="icon">N</span> 네이버
                            </button>
                        </div>
                        <div style={{ marginTop: '2rem', textAlign: 'center', color: '#666', fontSize: '0.95rem' }}>
                            아직 회원이 아니신가요? <Link to="/register" style={{ color: '#0055A5', fontBold: 700, textDecoration: 'none', marginLeft: '5px' }}>회원가입</Link>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
