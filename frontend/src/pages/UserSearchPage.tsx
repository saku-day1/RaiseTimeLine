import { useState } from 'react';
import { searchUsers } from '../api/users';
import type { UserSummary } from '../types/user';
import UserCard from '../components/user/UserCard';

export default function UserSearchPage() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<UserSummary[]>([]);
  const [searched, setSearched] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;
    setLoading(true);
    try {
      const data = await searchUsers(query.trim());
      setResults(data);
      setSearched(true);
    } finally {
      setLoading(false);
    }
  };

  const handleFollowChange = (userId: number, following: boolean) => {
    setResults((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, following } : u))
    );
  };

  return (
    <div>
      <h2 style={{ marginBottom: '16px' }}>ユーザー検索</h2>
      <form onSubmit={handleSearch} style={{ display: 'flex', gap: '8px', marginBottom: '24px' }}>
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="ユーザー名で検索"
          style={{ flex: 1, padding: '8px 12px', fontSize: '14px', borderRadius: '4px', border: '1px solid #ccc' }}
        />
        <button
          type="submit"
          disabled={loading || !query.trim()}
          style={{ padding: '8px 16px', borderRadius: '4px', border: 'none', background: '#1d9bf0', color: '#fff', cursor: 'pointer' }}
        >
          {loading ? '検索中...' : '検索'}
        </button>
      </form>

      {searched && results.length === 0 && (
        <p style={{ color: '#555' }}>ユーザーが見つかりませんでした。</p>
      )}

      {results.map((user) => (
        <UserCard key={user.id} user={user} onFollowChange={handleFollowChange} />
      ))}
    </div>
  );
}
