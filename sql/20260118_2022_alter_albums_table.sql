ALTER TABLE albums
    ADD COLUMN jamendo_albums_id BIGINT NULL COMMENT 'Jamendo 앨범 고유 식별자',
    ADD UNIQUE KEY uk_songs_jamendo_album_id (jamendo_albums_id);
