export interface CommentUser {
  id: number;
  username: string;
  profileImageUrl: string | null;
}

export interface Comment {
  id: number;
  content: string;
  createdAt: string;
  user: CommentUser;
}

export interface CreateCommentRequest {
  content: string;
}
