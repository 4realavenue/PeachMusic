ALTER TABLE artists
    ADD CONSTRAINT uk_artists_artist_name UNIQUE (artist_name);