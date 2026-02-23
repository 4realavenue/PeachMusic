ALTER TABLE artists
    ADD FULLTEXT INDEX ft_artist_name (artist_name);

ALTER TABLE albums
    ADD FULLTEXT INDEX ft_album_name (album_name);

ALTER TABLE songs
    ADD FULLTEXT INDEX ft_song_name (name);
