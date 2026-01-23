package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.UserRole;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
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
import static com.example.peachmusic.common.enums.UserRole.USER;
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
     * @param word     검색어
     * @param pageable 페이징 정보
     * @return 페이징 처리된 음원 검색 결과
     */
    @Override
    public Page<SongSearchResponseDto> findSongPageByWord(String word, Pageable pageable, UserRole role) {

        List<SongSearchResponseDto> content = baseQuery(word, role)
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 개수
                .fetch();

        Long total = queryFactory
                .select(song.songId.countDistinct()) // join으로 인한 중복 카운트 발생 방지
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .where(searchCondition(word, role))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 검색 - 미리보기
     * @param word  검색어
     * @param limit 검색 개수
     * @return 음원 검색 미리보기
     */
    @Override
    public List<SongSearchResponseDto> findSongListByWord(String word, int limit) {
        return baseQuery(word, USER).limit(limit).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<SongSearchResponseDto> baseQuery(String word, UserRole role) {

        return queryFactory
                .select(Projections.constructor(SongSearchResponseDto.class, song.songId, song.name, artist.artistName, song.likeCount, song.album.albumImage, song.isDeleted))
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .where(searchCondition(word, role));
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word, UserRole role) {
        if (role.equals(USER)) {
            BooleanExpression keywordCondition = songNameContains(word).or(artistNameContains(word));
            return keywordCondition.and(isActive());
        }
        return songNameContains(word);
    }

    /**
     * 검색 조건 1
     * - 검색어가 음원 이름에 포함된 경우
     */
    private BooleanExpression songNameContains(String word) {
        return StringUtils.hasText(word) ? song.name.contains(word) : null;
    }

    /**
     * 검색 조건 2
     * - 검색어가 아티스트 이름에 포함된 경우
     */
    private BooleanExpression artistNameContains(String word) {
        return StringUtils.hasText(word) ? artist.artistName.startsWith(word) : null;
    }

    /**
     * 검색 조건 3
     * - 음원이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return song.isDeleted.isFalse();
    }
}
