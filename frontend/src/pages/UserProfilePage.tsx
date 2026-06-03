import { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import { getUserProfile, getFollowers, getFollowing, updateProfile, uploadProfileImage, uploadHeaderImage } from '../api/users';
import type { UserProfile, UserSummary } from '../types/user';
import { useAuth } from '../contexts/AuthContext';
import { useFollow } from '../hooks/useFollow';
import UserCard from '../components/user/UserCard';

type Tab = 'followers' | 'following';

export default function UserProfilePage() {
  const { id } = useParams<{ id: string }>();
  const { user: me } = useAuth();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [list, setList] = useState<UserSummary[]>([]);
  const [activeTab, setActiveTab] = useState<Tab>('followers');
  const [loading, setLoading] = useState(true);
  const [listLoading, setListLoading] = useState(false);
  const [editing, setEditing] = useState(false);

  const userId = Number(id);

  useEffect(() => {
    if (!userId) return;
    setLoading(true);
    getUserProfile(userId)
      .then(setProfile)
      .finally(() => setLoading(false));
  }, [userId]);

  useEffect(() => {
    if (!userId) return;
    setListLoading(true);
    const fetch = activeTab === 'followers' ? getFollowers : getFollowing;
    fetch(userId)
      .then(setList)
      .finally(() => setListLoading(false));
  }, [userId, activeTab]);

  const handleFollowChange = (targetId: number, following: boolean) => {
    setList((prev) => prev.map((u) => (u.id === targetId ? { ...u, following } : u)));
    if (profile && targetId === userId) {
      setProfile((prev) =>
        prev ? { ...prev, followerCount: prev.followerCount + (following ? 1 : -1) } : prev
      );
    }
  };

  if (loading) return <p>読み込み中...</p>;
  if (!profile) return <p>ユーザーが見つかりません</p>;

  const isMe = me?.userId === profile.id;

  return (
    <div>
      <ProfileHeader
        profile={profile}
        isMe={isMe}
        editing={editing}
        onEditToggle={() => setEditing((v) => !v)}
        onFollowChange={(following) => {
          setProfile((prev) =>
            prev ? { ...prev, following, followerCount: prev.followerCount + (following ? 1 : -1) } : prev
          );
        }}
        onProfileUpdate={(updated) => {
          setProfile(updated);
          setEditing(false);
        }}
      />

      <div style={{ display: 'flex', borderBottom: '1px solid #e5e7eb', marginBottom: '16px' }}>
        {(['followers', 'following'] as Tab[]).map((tab) => (
          <button
            key={tab}
            onClick={() => setActiveTab(tab)}
            style={{
              flex: 1,
              padding: '12px',
              background: 'none',
              border: 'none',
              borderBottom: activeTab === tab ? '2px solid #1d9bf0' : '2px solid transparent',
              fontWeight: activeTab === tab ? 'bold' : 'normal',
              cursor: 'pointer',
              fontSize: '14px',
            }}
          >
            {tab === 'followers' ? `フォロワー ${profile.followerCount}` : `フォロー中 ${profile.followingCount}`}
          </button>
        ))}
      </div>

      {listLoading ? (
        <p style={{ color: '#9ca3af', fontSize: '14px' }}>読み込み中...</p>
      ) : list.length === 0 ? (
        <p style={{ color: '#9ca3af', fontSize: '14px' }}>
          {activeTab === 'followers' ? 'フォロワーはまだいません' : 'フォロー中のユーザーはいません'}
        </p>
      ) : (
        list.map((u) => (
          <UserCard key={u.id} user={u} onFollowChange={handleFollowChange} />
        ))
      )}
    </div>
  );
}

function ProfileHeader({ profile, isMe, editing, onEditToggle, onFollowChange, onProfileUpdate }: {
  profile: UserProfile;
  isMe: boolean;
  editing: boolean;
  onEditToggle: () => void;
  onFollowChange: (following: boolean) => void;
  onProfileUpdate: (updated: UserProfile) => void;
}) {
  const { following, loading, toggle } = useFollow(profile.id, profile.following);
  const isMutual = following && profile.followedBack;

  const handleClick = async () => {
    await toggle();
    onFollowChange(!following);
  };

  return (
    <div style={{ borderBottom: '1px solid #e5e7eb', marginBottom: '16px' }}>
      <HeaderImageBanner profile={profile} isMe={isMe} onProfileUpdate={onProfileUpdate} />
      <div style={{ padding: '0 16px 24px' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <ProfileAvatar profile={profile} isMe={isMe} onProfileUpdate={onProfileUpdate} />
          <div>
            <div style={{ fontWeight: 'bold', fontSize: '18px' }}>{profile.displayName}</div>
            <div style={{ color: '#6b7280', fontSize: '14px' }}>@{profile.username}</div>
            {isMutual && (
              <div style={{ marginTop: '4px', fontSize: '12px', color: '#1d9bf0' }}>相互フォロー</div>
            )}
            {profile.bio && <div style={{ marginTop: '6px', fontSize: '14px' }}>{profile.bio}</div>}
          </div>
        </div>
        <div style={{ display: 'flex', gap: '8px', flexShrink: 0 }}>
          {isMe && (
            <button
              onClick={onEditToggle}
              style={{
                padding: '8px 16px',
                borderRadius: '20px',
                border: '1px solid #ccc',
                background: '#fff',
                color: '#333',
                cursor: 'pointer',
                fontWeight: 'bold',
                fontSize: '14px',
              }}
            >
              {editing ? 'キャンセル' : 'プロフィールを編集'}
            </button>
          )}
          {!isMe && (
            <button
              onClick={handleClick}
              disabled={loading}
              style={{
                padding: '8px 20px',
                borderRadius: '20px',
                border: following ? '1px solid #ccc' : 'none',
                background: following ? '#fff' : '#1d9bf0',
                color: following ? '#333' : '#fff',
                cursor: 'pointer',
                fontWeight: 'bold',
                fontSize: '14px',
              }}
            >
              {following ? 'フォロー中' : 'フォロー'}
            </button>
          )}
        </div>
      </div>

      {editing && (
        <EditForm profile={profile} onSave={onProfileUpdate} onCancel={onEditToggle} />
      )}
      </div>
    </div>
  );
}

function HeaderImageBanner({ profile, isMe, onProfileUpdate }: {
  profile: UserProfile;
  isMe: boolean;
  onProfileUpdate: (updated: UserProfile) => void;
}) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [hovered, setHovered] = useState(false);
  const [error, setError] = useState('');

  const handleChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const { imageUrl } = await uploadHeaderImage(file);
      onProfileUpdate({ ...profile, headerImageUrl: imageUrl });
    } catch (err: unknown) {
      const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'アップロードに失敗しました';
      setError(msg);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  return (
    <div
      style={{
        width: '100%',
        height: '150px',
        background: profile.headerImageUrl ? 'transparent' : '#cfe2f3',
        position: 'relative',
        overflow: 'hidden',
        cursor: isMe ? 'pointer' : 'default',
        opacity: uploading ? 0.7 : 1,
      }}
      onClick={() => isMe && fileInputRef.current?.click()}
      onMouseEnter={() => isMe && setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {profile.headerImageUrl && (
        <img
          src={profile.headerImageUrl}
          alt="header"
          style={{ width: '100%', height: '100%', objectFit: 'cover' }}
        />
      )}
      {isMe && hovered && !uploading && (
        <div style={{
          position: 'absolute',
          inset: 0,
          background: 'rgba(0,0,0,0.35)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}>
          <span style={{ color: '#fff', fontSize: '14px', fontWeight: 'bold' }}>ヘッダー画像を変更</span>
        </div>
      )}
      {isMe && (
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/gif"
          style={{ display: 'none' }}
          onChange={handleChange}
        />
      )}
      {error && (
        <div style={{ position: 'absolute', bottom: '8px', left: '50%', transform: 'translateX(-50%)', background: 'rgba(0,0,0,0.7)', color: '#fff', fontSize: '12px', padding: '4px 10px', borderRadius: '4px', whiteSpace: 'nowrap' }}>
          {error}
        </div>
      )}
    </div>
  );
}

function ProfileAvatar({ profile, isMe, onProfileUpdate }: {
  profile: UserProfile;
  isMe: boolean;
  onProfileUpdate: (updated: UserProfile) => void;
}) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [hovered, setHovered] = useState(false);
  const [uploadError, setUploadError] = useState('');

  const handleImageChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    setUploadError('');
    try {
      const { imageUrl } = await uploadProfileImage(file);
      onProfileUpdate({ ...profile, profileImageUrl: imageUrl });
    } catch {
      setUploadError('画像のアップロードに失敗しました');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  return (
    <div style={{ position: 'relative', flexShrink: 0 }}>
      <div
        style={{
          width: '64px',
          height: '64px',
          borderRadius: '50%',
          background: '#d1d5db',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontWeight: 'bold',
          fontSize: '24px',
          overflow: 'hidden',
          cursor: isMe ? 'pointer' : 'default',
          opacity: uploading ? 0.5 : 1,
          position: 'relative',
        }}
        onClick={() => isMe && fileInputRef.current?.click()}
        onMouseEnter={() => isMe && setHovered(true)}
        onMouseLeave={() => setHovered(false)}
        title={isMe ? '画像を変更' : undefined}
      >
        {profile.profileImageUrl
          ? <img src={profile.profileImageUrl} alt={profile.username} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
          : profile.username[0].toUpperCase()}
        {isMe && hovered && !uploading && (
          <div style={{
            position: 'absolute',
            inset: 0,
            borderRadius: '50%',
            background: 'rgba(0,0,0,0.4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}>
            <span style={{ color: '#fff', fontSize: '11px', fontWeight: 'bold' }}>変更</span>
          </div>
        )}
      </div>
      {isMe && (
        <input
          ref={fileInputRef}
          type="file"
          accept="image/jpeg,image/png,image/gif"
          style={{ display: 'none' }}
          onChange={handleImageChange}
        />
      )}
      {uploadError && (
        <p style={{ color: '#ef4444', fontSize: '12px', margin: '4px 0 0', whiteSpace: 'nowrap' }}>{uploadError}</p>
      )}
    </div>
  );
}

function EditForm({ profile, onSave, onCancel }: {
  profile: UserProfile;
  onSave: (updated: UserProfile) => void;
  onCancel: () => void;
}) {
  const [displayName, setDisplayName] = useState(profile.displayName);
  const [bio, setBio] = useState(profile.bio ?? '');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!displayName.trim()) {
      setError('表示名は必須です');
      return;
    }
    setSaving(true);
    setError('');
    try {
      const updated = await updateProfile({ displayName: displayName.trim(), bio: bio.trim() });
      onSave({ ...profile, ...updated });
    } catch {
      setError('保存に失敗しました');
    } finally {
      setSaving(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ marginTop: '16px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
      <div>
        <label style={{ display: 'block', fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>表示名</label>
        <input
          type="text"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          maxLength={50}
          style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '14px', boxSizing: 'border-box' }}
        />
        <div style={{ fontSize: '12px', color: '#9ca3af', textAlign: 'right' }}>{displayName.length}/50</div>
      </div>

      <div>
        <label style={{ display: 'block', fontSize: '13px', color: '#6b7280', marginBottom: '4px' }}>自己紹介（bio）</label>
        <textarea
          value={bio}
          onChange={(e) => setBio(e.target.value)}
          maxLength={160}
          rows={3}
          style={{ width: '100%', padding: '8px', border: '1px solid #d1d5db', borderRadius: '6px', fontSize: '14px', resize: 'vertical', boxSizing: 'border-box' }}
        />
        <div style={{ fontSize: '12px', color: '#9ca3af', textAlign: 'right' }}>{bio.length}/160</div>
      </div>

      {error && <p style={{ color: '#ef4444', fontSize: '14px', margin: 0 }}>{error}</p>}

      <div style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
        <button
          type="button"
          onClick={onCancel}
          style={{ padding: '8px 16px', borderRadius: '6px', border: '1px solid #d1d5db', background: '#fff', cursor: 'pointer', fontSize: '14px' }}
        >
          キャンセル
        </button>
        <button
          type="submit"
          disabled={saving}
          style={{ padding: '8px 16px', borderRadius: '6px', border: 'none', background: '#1d9bf0', color: '#fff', cursor: 'pointer', fontWeight: 'bold', fontSize: '14px' }}
        >
          {saving ? '保存中...' : '保存'}
        </button>
      </div>
    </form>
  );
}
