CREATE TABLE likes (
    id         BIGSERIAL PRIMARY KEY,
    post_id    BIGINT    NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_likes_post_user UNIQUE (post_id, user_id)
);
