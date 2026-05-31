import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { register as registerApi } from '../api/auth';
import { useAuth } from '../contexts/AuthContext';

type FormData = {
  username: string;
  displayName: string;
  email: string;
  password: string;
  passwordConfirm: string;
};

type FieldErrors = Partial<Record<keyof FormData, string>>;

const inputBase: React.CSSProperties = {
  padding: '10px 12px',
  border: '1px solid #d1d5db',
  borderRadius: '6px',
  fontSize: '14px',
  width: '100%',
  boxSizing: 'border-box',
  outline: 'none',
};

const inputError: React.CSSProperties = { ...inputBase, border: '1px solid #ef4444' };
const label: React.CSSProperties = { fontSize: '13px', fontWeight: 600, color: '#374151', marginBottom: '4px', display: 'block' };
const hint: React.CSSProperties = { fontSize: '11px', color: '#9ca3af', marginTop: '3px' };
const errorText: React.CSSProperties = { fontSize: '12px', color: '#ef4444', marginTop: '3px' };
const field: React.CSSProperties = { display: 'flex', flexDirection: 'column' };
const required = <span style={{ color: '#ef4444', marginLeft: '2px' }}>*</span>;

export default function RegisterPage() {
  const [form, setForm] = useState<FormData>({ username: '', displayName: '', email: '', password: '', passwordConfirm: '' });
  const [errors, setErrors] = useState<FieldErrors>({});
  const [showConfirm, setShowConfirm] = useState(false);
  const [submitError, setSubmitError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const validate = (): FieldErrors => {
    const e: FieldErrors = {};
    if (!form.username) e.username = 'ユーザー名を入力してください';
    else if (form.username.length > 50) e.username = '50文字以内で入力してください';
    if (!form.displayName) e.displayName = '表示名を入力してください';
    else if (form.displayName.length > 50) e.displayName = '50文字以内で入力してください';
    if (!form.email) e.email = 'メールアドレスを入力してください';
    if (!form.password) e.password = 'パスワードを入力してください';
    else if (form.password.length < 8) e.password = '8文字以上で入力してください';
    if (!form.passwordConfirm) e.passwordConfirm = 'パスワード（確認）を入力してください';
    else if (form.password !== form.passwordConfirm) e.passwordConfirm = 'パスワードが一致しません';
    return e;
  };

  const handleBlur = (field: keyof FormData) => {
    const e = validate();
    setErrors(prev => ({ ...prev, [field]: e[field] }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const e2 = validate();
    if (Object.keys(e2).length > 0) {
      setErrors(e2);
      return;
    }
    setShowConfirm(true);
  };

  const handleConfirm = async () => {
    setSubmitting(true);
    setSubmitError('');
    try {
      const res = await registerApi({ username: form.username, displayName: form.displayName, email: form.email, password: form.password });
      login(res.token, { userId: res.userId, username: res.username });
      navigate('/');
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { error?: string } } })?.response?.data?.error;
      setSubmitError(msg ?? '登録に失敗しました');
      setShowConfirm(false);
    } finally {
      setSubmitting(false);
    }
  };

  const passwordMatch = form.passwordConfirm.length > 0 && form.password === form.passwordConfirm;

  return (
    <>
      <div style={{ maxWidth: '440px', margin: '60px auto', padding: '36px', border: '1px solid #e5e7eb', borderRadius: '12px', backgroundColor: '#fff' }}>
        <Link to="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '13px', color: '#6b7280', textDecoration: 'none', marginBottom: '20px' }}>
          ← ログインに戻る
        </Link>

        <h1 style={{ fontSize: '22px', marginBottom: '6px' }}>新規登録</h1>
        <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '24px' }}>アカウントを作成してタイムラインに参加しましょう</p>

        {submitError && <p style={{ color: '#ef4444', fontSize: '14px', marginBottom: '16px' }}>{submitError}</p>}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '18px' }}>
          <div style={field}>
            <label style={label}>ユーザー名{required}</label>
            <input
              type="text"
              placeholder="例: tanaka_taro"
              value={form.username}
              onChange={e => setForm(f => ({ ...f, username: e.target.value }))}
              onBlur={() => handleBlur('username')}
              maxLength={50}
              style={errors.username ? inputError : inputBase}
            />
            {errors.username
              ? <span style={errorText}>{errors.username}</span>
              : <span style={hint}>@の後に使われるIDです（1〜50文字）</span>}
          </div>

          <div style={field}>
            <label style={label}>表示名{required}</label>
            <input
              type="text"
              placeholder="例: 田中太郎"
              value={form.displayName}
              onChange={e => setForm(f => ({ ...f, displayName: e.target.value }))}
              onBlur={() => handleBlur('displayName')}
              maxLength={50}
              style={errors.displayName ? inputError : inputBase}
            />
            {errors.displayName
              ? <span style={errorText}>{errors.displayName}</span>
              : <span style={hint}>プロフィールに表示される名前です</span>}
          </div>

          <div style={field}>
            <label style={label}>メールアドレス{required}</label>
            <input
              type="email"
              placeholder="例: taro@example.com"
              value={form.email}
              onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
              onBlur={() => handleBlur('email')}
              style={errors.email ? inputError : inputBase}
            />
            {errors.email && <span style={errorText}>{errors.email}</span>}
          </div>

          <div style={field}>
            <label style={label}>パスワード{required}</label>
            <input
              type="password"
              placeholder="8文字以上"
              value={form.password}
              onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
              onBlur={() => handleBlur('password')}
              style={errors.password ? inputError : inputBase}
            />
            {errors.password && <span style={errorText}>{errors.password}</span>}
          </div>

          <div style={field}>
            <label style={label}>パスワード（確認）{required}</label>
            <input
              type="password"
              placeholder="もう一度パスワードを入力"
              value={form.passwordConfirm}
              onChange={e => setForm(f => ({ ...f, passwordConfirm: e.target.value }))}
              onBlur={() => handleBlur('passwordConfirm')}
              style={errors.passwordConfirm ? inputError : inputBase}
            />
            {errors.passwordConfirm
              ? <span style={errorText}>{errors.passwordConfirm}</span>
              : passwordMatch && <span style={{ fontSize: '12px', color: '#16a34a', marginTop: '3px' }}>✓ パスワードが一致しています</span>}
          </div>

          <button
            type="submit"
            style={{ marginTop: '4px', padding: '12px', backgroundColor: '#1d4ed8', color: 'white', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '15px', fontWeight: 600 }}
          >
            内容を確認する →
          </button>
        </form>

        <p style={{ marginTop: '20px', textAlign: 'center', fontSize: '13px', color: '#6b7280' }}>
          すでにアカウントをお持ちの方は <Link to="/login">ログイン</Link>
        </p>
      </div>

      {showConfirm && (
        <div style={{ position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.45)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 50 }}>
          <div style={{ backgroundColor: '#fff', borderRadius: '12px', padding: '32px', maxWidth: '400px', width: '90%', boxShadow: '0 20px 60px rgba(0,0,0,0.2)' }}>
            <h2 style={{ fontSize: '18px', marginBottom: '6px' }}>登録内容の確認</h2>
            <p style={{ fontSize: '13px', color: '#6b7280', marginBottom: '20px' }}>以下の内容でよろしいですか？</p>

            <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '14px', marginBottom: '24px' }}>
              <tbody>
                {[
                  { label: 'ユーザー名', value: `@${form.username}` },
                  { label: '表示名', value: form.displayName },
                  { label: 'メールアドレス', value: form.email },
                  { label: 'パスワード', value: '••••••••' },
                ].map(({ label, value }) => (
                  <tr key={label} style={{ borderBottom: '1px solid #f3f4f6' }}>
                    <td style={{ padding: '10px 0', color: '#6b7280', width: '42%', fontSize: '13px' }}>{label}</td>
                    <td style={{ padding: '10px 0', fontWeight: 500 }}>{value}</td>
                  </tr>
                ))}
              </tbody>
            </table>

            <div style={{ display: 'flex', gap: '12px' }}>
              <button
                onClick={() => setShowConfirm(false)}
                style={{ flex: 1, padding: '10px', backgroundColor: '#f3f4f6', border: 'none', borderRadius: '6px', cursor: 'pointer', fontSize: '14px' }}
              >
                ← 修正する
              </button>
              <button
                onClick={handleConfirm}
                disabled={submitting}
                style={{ flex: 1, padding: '10px', backgroundColor: '#1d4ed8', color: 'white', border: 'none', borderRadius: '6px', cursor: submitting ? 'not-allowed' : 'pointer', fontSize: '14px', fontWeight: 600, opacity: submitting ? 0.7 : 1 }}
              >
                {submitting ? '登録中...' : 'この内容で登録する'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
