package com.example.peachmusic.domain.album.repository;

import com.example.peachmusic.domain.album.model.response.AlbumSearchResponse;
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
import static com.example.peachmusic.domain.album.entity.QAlbum.album;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistAlbum.entity.QArtistAlbum.artistAlbum;

public class AlbumCustomRepositoryImpl implements AlbumCustomRepository {

    private final JPAQueryFactory queryFactory;

    public AlbumCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     * @param word 검색어
     * @param pageable 페이징 정보
     * @return 페이징 처리된 앨범 검색 결과
     */
    @Override
    public Page<AlbumSearchResponse> findAlbumPageByWord(String word, Pageable pageable) {

        List<AlbumSearchResponse> content = baseQuery(word)
                .offset(pageable.getOffset()) // 시작 위치
                .limit(pageable.getPageSize()) // 개수
                .fetch();

        Long total = queryFactory
                .select(album.albumId.countDistinct()) // join으로 인한 중복 카운트 발생 방지
                .from(album)
                .join(artistAlbum).on(artistAlbum.album.eq(album))
                .join(artist).on(artistAlbum.artist.eq(artist))
                .where(SearchCondition(word))
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    /**
     * 검색 - 미리보기
     * @param word 검색어
     * @param limit 검색 개수
     * @return 앨범 검색 미리보기
     */
    @Override
    public List<AlbumSearchResponse> findAlbumListByWord(String word, int limit) {
        return baseQuery(word).limit(limit).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<AlbumSearchResponse> baseQuery(String word) {

        return queryFactory
                .selectDistinct(Projections.constructor(AlbumSearchResponse.class, album.albumId, album.albumName, artist.artistName, album.albumReleaseDate, album.albumImage, album.likeCount))
                .from(album)
                .join(artistAlbum).on(artistAlbum.album.eq(album))
                .join(artist).on(artistAlbum.artist.eq(artist))
                .where(SearchCondition(word)); // 검색어 포함 조건
    }

    /**
     * 검색 조건
     */
    private BooleanExpression SearchCondition(String word) {
        return albumOrArtistContains(word).and(isActive());
    }

    /**
     * 검색 조건 1
     * - 검색어가 앨범 이름 또는 아티스트 이름에 포함된 경우
     */
    private BooleanExpression albumOrArtistContains(String word) {
        return StringUtils.hasText(word) ? album.albumName.contains(word).or(artist.artistName.startsWith(word)) : null;
    }

    /**
     * 검색 조건 2
     * - 앨범이 삭제된 상태가 아닌 경우
     */
    private BooleanExpression isActive() {
        return album.isDeleted.isFalse();
    }
}
