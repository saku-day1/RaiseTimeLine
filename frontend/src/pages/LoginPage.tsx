import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { login as loginApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

const inputBase: React.CSSProperties = {
  padding: '10px 12px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  fontSize: '14px',
  width: '100%',
  boxSizing: 'border-box',
  outline: 'none',
};

const label: React.CSSProperties = { fontSize: '13px', fontWeight: 600, color: '#374151', marginBottom: '4px', display: 'block' };

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    try {
      const res = await loginApi({ email, password });
      login(res.token, { userId: res.userId, username: res.username });
      navigate('/');
    } catch {
      setError('メールアドレスまたはパスワードが正しくありません');
    }
  };

  return (
    <div style={{ maxWidth: '400px', margin: '80px auto', padding: '36px', border: '1px solid #e5e7eb', borderRadius: '12px', backgroundColor: '#fff' }}>
      <Link to="/" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '13px', color: '#6b7280', textDecoration: 'none', marginBottom: '20px' }}>
        ← トップに戻る
      </Link>

      <h1 style={{ fontSize: '22px', marginBottom: '6px' }}>ログイン</h1>
      <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '24px' }}>アカウントにサインインしてください</p>

      {error && <p style={{ color: '#ef4444', fontSize: '14px', marginBottom: '16px', padding: '10px 12px', backgroundColor: '#fef2f2', borderRadius: '6px' }}>{error}</p>}

      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label style={label}>メールアドレス</label>
          <input
            type="email"
            placeholder="例: taro@example.com"
            value={email}
            onChange={e => setEmail(e.target.value)}
            required
            style={inputBase}
          />
        </div>

        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <label style={label}>パスワード</label>
          <input
            type="password"
            placeholder="パスワードを入力"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
            style={inputBase}
          />
        </div>

        <button
          type="submit"
          style={{ marginTop: '4px', padding: '12px', backgroundColor: '#1d4ed8', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '15px', fontWeight: 600 }}
        >
          ログイン
        </button>
      </form>

      <p style={{ marginTop: '20px', textAlign: 'center', fontSize: '13px', color: '#6b7280' }}>
        アカウントをお持ちでない方は <Link to="/register">新規登録</Link>
      </p>
    </div>
  );
}
