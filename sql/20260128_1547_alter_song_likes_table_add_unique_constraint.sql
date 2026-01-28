ALTER TABLE song_likes
    ADD CONSTRAINT uk_song_likes_user_song UNIQUE (user_id, song_id);