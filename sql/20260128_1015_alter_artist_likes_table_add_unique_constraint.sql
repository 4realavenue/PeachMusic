ALTER TABLE artist_likes
    ADD CONSTRAINT uk_artist_likes_user_artist UNIQUE (user_id, artist_id);