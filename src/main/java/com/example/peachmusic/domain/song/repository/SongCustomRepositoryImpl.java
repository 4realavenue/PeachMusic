package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.domain.song.model.response.SongSearchResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;
import java.util.List;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistSong.entity.QArtistSong.artistSong;
import static com.example.peachmusic.domain.song.entity.QSong.song;

public class SongCustomRepositoryImpl implements SongCustomRepository {

    private final JPAQueryFactory queryFactory;

    public SongCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보
     * @return 페이징 처리된 음원 검색 결과
     */
    @Override
    public Page<SongSearchResponse> findSongPageByWord(String word, Pageable pageable) {

        List<SongSearchResponse> content = baseQuery(word)
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 개수
                .fetch();

        Long total = queryFactory
                .select(song.songId.countDistinct()) // join으로 인한 중복 카운트 발생 방지
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .where(SearchCondition(word))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 검색 - 미리보기
     * @param word 검색어
     * @param limit 검색 개수
     * @return 음원 검색 미리보기
     */
    @Override
    public List<SongSearchResponse> findSongListByWord(String word, int limit) {
        return baseQuery(word).limit(limit).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<SongSearchResponse> baseQuery(String word) {

        return queryFactory
                .select(Projections.constructor(SongSearchResponse.class, song.songId, song.name, artist.artistName, song.likeCount))
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .where(SearchCondition(word));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression SearchCondition(String word) {
        return songOrArtistContains(word).and(isActive());
    }

    /**
     * 검색 조건 1
     * - 검색어가 음원 이름 또는 아티스트 이름에 포함된 경우
     */
    private BooleanExpression songOrArtistContains(String word) {
        return StringUtils.hasText(word) ? song.name.contains(word).or(artist.artistName.startsWith(word)) : null;
    }

    /**
     * 검색 조건 2
     * - 음원이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return song.isDeleted.isFalse();
    }
}
