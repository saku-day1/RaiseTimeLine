import { useComments } from '../../hooks/useComments';
import CommentItem from './CommentItem';

interface Props {
  postId: number;
  onDeleted: () => void;
}

export default function CommentList({ postId, onDeleted }: Props) {
  const { comments, loading, removeComment } = useComments(postId);

  const handleDelete = async (commentId: number) => {
    await removeComment(commentId);
    onDeleted();
  };

  if (loading) return <p style={{ color: '#9ca3af', fontSize: '14px' }}>読み込み中...</p>;
  if (comments.length === 0) return <p style={{ color: '#9ca3af', fontSize: '14px' }}>コメントはまだありません</p>;

  return (
    <div>
      <h3 style={{ fontSize: '16px', marginBottom: '8px' }}>コメント（{comments.length}件）</h3>
      {comments.map((comment) => (
        <CommentItem key={comment.id} comment={comment} onDelete={handleDelete} />
      ))}
    </div>
  );
}
