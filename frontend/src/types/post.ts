export interface PostSummary {
  id: number;
  content: string;
  imageUrl: string | null;
  createdAt: string;
  userId: number;
  username: string;
  profileImageUrl: string | null;
  likeCount: number;
  commentCount: number;
  liked: boolean;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  last: boolean;
}
