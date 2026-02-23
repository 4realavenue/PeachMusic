CREATE INDEX idx_active_artist_like_id ON artists (is_deleted, like_count, artist_id);
CREATE INDEX idx_active_artist_name_id ON artists (is_deleted, artist_name, artist_id);
CREATE INDEX idx_active_album_like_id ON albums (is_deleted, like_count, album_id);
CREATE INDEX idx_active_album_name_id ON albums (is_deleted, album_name, album_id);
CREATE INDEX idx_active_album_date_id ON albums (is_deleted, album_release_date, album_id);
CREATE INDEX idx_active_song_like_id ON songs (is_deleted, like_count, song_id);
CREATE INDEX idx_active_song_name_id ON songs (is_deleted, name, song_id);
CREATE INDEX idx_active_song_date_id ON songs (is_deleted, release_date, song_id);