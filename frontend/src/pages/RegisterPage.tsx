import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import apiClient from '../api/apiClient';
import '../App.css';

const RegisterPage: React.FC = () => {
    const navigate = useNavigate();
    const [formData, setFormData] = useState({ 
        loginId: '', 
        password: '', 
        name: '', 
        email: '', 
        phone: '', 
        birthDate: '',
        website: '' // Honeypot field
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await apiClient.post('/auth/signup', formData);
            alert("회원가입이 완료되었습니다. 로그인해주세요.");
            navigate('/login');
        } catch (error) {
            alert("회원가입에 실패했습니다. 다시 확인해주세요.");
        }
    };

    return (
        <div className="auth-wrapper">
            <div className="auth-card">
                <h1 style={{ color: '#0055A5', textAlign: 'center', marginBottom: '30px', fontWeight: 900, fontSize: '2.5rem' }}>회원가입</h1>
                <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                    <div className="auth-input-group"><label>아이디</label><input type="text" name="loginId" required onChange={handleChange} /></div>
                    <div className="auth-input-group"><label>비밀번호</label><input type="password" name="password" required onChange={handleChange} /></div>
                    <div className="auth-input-group"><label>이름</label><input type="text" name="name" required onChange={handleChange} /></div>
                    <div className="auth-input-group"><label>이메일</label><input type="email" name="email" required onChange={handleChange} /></div>
                    <div className="auth-input-group"><label>전화번호</label><input type="text" name="phone" placeholder="010-0000-0000" onChange={handleChange} /></div>
                    <div className="auth-input-group"><label>생년월일</label><input type="text" name="birthDate" placeholder="YYYYMMDD" onChange={handleChange} /></div>
                    
                    {/* Honeypot Field (Hidden from real users) */}
                    <div style={{ display: 'none' }}>
                        <label>Leave this field empty</label>
                        <input type="text" name="website" value={formData.website} onChange={handleChange} tabIndex={-1} autoComplete="off" />
                    </div>

                    <button type="submit" className="btn-primary" style={{ padding: '15px', marginTop: '10px', fontSize: '1.1rem' }}>가입하기</button>
                </form>
                <div style={{ marginTop: '20px', textAlign: 'center', color: '#666' }}>
                    이미 계정이 있으신가요? <Link to="/login" style={{ color: '#0055A5', fontWeight: 'bold', textDecoration: 'none' }}>로그인</Link>
                </div>
            </div>
        </div>
    );
};

export default RegisterPage;