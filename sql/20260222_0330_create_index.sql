create index idx_artist_like_user_artist on artist_like (user_id, artist_id);
create index idx_album_like_user_album on album_like (user_id, album_id);
create index idx_song_like_user_song on song_like (user_id, song_id);
