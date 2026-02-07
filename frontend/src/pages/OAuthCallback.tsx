import React, { useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

const OAuthCallback: React.FC = () => {
    const [searchParams] = useSearchParams();
    const navigate = useNavigate();

    useEffect(() => {
        const accessToken = searchParams.get('accessToken');
        const refreshToken = searchParams.get('refreshToken');

        if (accessToken) {
            localStorage.setItem('accessToken', accessToken);
            if (refreshToken) {
                localStorage.setItem('refreshToken', refreshToken);
            }
            localStorage.removeItem('role'); // 기존 비회원 플래그 제거
            
            // [Fix] 헤더 상태 즉시 업데이트를 위한 이벤트 발생
            window.dispatchEvent(new Event('authChange'));
            
            navigate('/');
        } else {
            console.error('Login failed: No access token received');
            navigate('/login');
        }
    }, [searchParams, navigate]);

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
            Processing login...
        </div>
    );
};

export default OAuthCallback;
