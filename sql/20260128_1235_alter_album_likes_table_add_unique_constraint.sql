ALTER TABLE album_likes
    ADD CONSTRAINT uk_album_likes_user_album UNIQUE (user_id, album_id);