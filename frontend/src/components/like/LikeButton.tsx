import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useLike } from '../../hooks/useLike';

interface Props {
  postId: number;
  initialLiked: boolean;
  initialCount: number;
  onLikeChange?: (postId: number, liked: boolean, count: number) => void;
}

export default function LikeButton({ postId, initialLiked, initialCount, onLikeChange }: Props) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { liked, count, toggle } = useLike(postId, initialLiked, initialCount);

  const handleClick = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    const res = await toggle();
    if (res && onLikeChange) {
      onLikeChange(postId, res.liked, res.count);
    }
  };

  return (
    <button
      onClick={handleClick}
      style={{
        background: 'none',
        border: 'none',
        cursor: 'pointer',
        padding: 0,
        color: liked ? '#ef4444' : '#6b7280',
        fontSize: 'inherit',
        display: 'flex',
        alignItems: 'center',
        gap: '4px',
      }}
    >
      {liked ? '❤️' : '🤍'} {count}
    </button>
  );
}
