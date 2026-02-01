package com.example.peachmusic.domain.song.repository;

import com.example.peachmusic.common.enums.SortDirection;
import com.example.peachmusic.common.enums.SortType;
import com.example.peachmusic.domain.song.dto.response.SongSearchResponseDto;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import static com.example.peachmusic.domain.artist.entity.QArtist.artist;
import static com.example.peachmusic.domain.artistsong.entity.QArtistSong.artistSong;
import static com.example.peachmusic.domain.song.entity.QSong.song;

public class SongCustomRepositoryImpl implements SongCustomRepository {

    private final JPAQueryFactory queryFactory;

    public SongCustomRepositoryImpl(EntityManager em) {
        queryFactory = new JPAQueryFactory(em);
    }

    /**
     * 검색 - 자세히 보기
     */
    @Override
    public List<SongSearchResponseDto> findSongKeysetPageByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {
        return baseQuery(word, isAdmin, sortType, direction, lastId, lastLike, lastName)
                .limit(size+1).fetch(); // 요청한 사이즈보다 하나 더 많은 데이터를 조회
    }

    /**
     * 검색 - 미리보기
     */
    @Override
    public List<SongSearchResponseDto> findSongListByWord(String word, int size, boolean isAdmin, SortType sortType, SortDirection direction) {
        return baseQuery(word, isAdmin, sortType, direction, null, null, null).limit(size).fetch();
    }

    /**
     * 기본 쿼리
     */
    private JPAQuery<SongSearchResponseDto> baseQuery(String word, boolean isAdmin, SortType sortType, SortDirection direction, Long lastId, Long lastLike, String lastName) {

        boolean isAsc = direction == SortDirection.ASC;

        List<OrderSpecifier<?>> orders = new ArrayList<>();
        OrderSpecifier<?> main = mainOrder(sortType, isAsc);
        if (main != null) {
            orders.add(main);
        }
        orders.add(idOrder(isAsc)); // id 정렬은 항상 함

        // 아티스트 이름을 문자열로 합치기
        StringTemplate artistNames = Expressions.stringTemplate("GROUP_CONCAT({0})", artist.artistName);

        return queryFactory
                .select(Projections.constructor(SongSearchResponseDto.class, song.songId, song.name, artistNames, song.likeCount, song.album.albumImage, song.isDeleted))
                .from(song)
                .join(artistSong).on(artistSong.song.eq(song))
                .join(artist).on(artistSong.artist.eq(artist))
                .where(searchCondition(word, isAdmin), keysetCondition(sortType, isAsc, lastId, lastLike, lastName)) // 검색어 조건, Keyset 조건
                .groupBy(song.songId) // 아티스트 이름을 문자열로 합치는데 음원 id를 기준으로 함
                .orderBy(orders.toArray(OrderSpecifier[]::new)); // Keyset 조건에 사용되는 커서 순서대로 정렬
    }

    /**
     * 검색 조건
     */
    private BooleanExpression searchCondition(String word, boolean isAdmin) {

        if (isAdmin) { // 관리자용은 삭제된 음원도 조회되어야 함
            return songNameContains(word);
        }

        BooleanExpression keywordCondition = songNameContains(word).or(artistNameContains(word));
        return keywordCondition.and(isActive());
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

    /**
     * Keyset 조건
     */
    private BooleanExpression keysetCondition(SortType sortType, boolean asc, Long lastId, Long lastLike, String lastName) {

        // lastId가 없으면 Keyset 조건 없음
        if (lastId == null) {
            return null;
        }

        // 메인 정렬이 없는 경우 → id만
        if (sortType == null) {
            return idKeyset(asc, lastId);
        }

        return switch (sortType) {
            case LIKE -> likeCountKeyset(asc, lastId, lastLike);
            case NAME -> nameKeyset(asc, lastId, lastName);
        };
    }

    /**
     * id가 Keyset이 되는 경우
     */
    private BooleanExpression idKeyset(boolean asc, Long lastId) {
        return asc ? song.songId.gt(lastId) : song.songId.lt(lastId);
    }

    /**
     * 좋아요 수가 Keyset이 되는 경우
     */
    private BooleanExpression likeCountKeyset(boolean asc, Long lastId, Long lastLike) {
        BooleanExpression likeCondition = asc ? song.likeCount.gt(lastLike) : song.likeCount.lt(lastLike);
        return likeCondition.or(song.likeCount.eq(lastLike).and(idKeyset(asc, lastId)));
    }

    /**
     * 이름이 Keyset이 되는 경우
     */
    private BooleanExpression nameKeyset(boolean asc, Long lastId, String lastName) {
        BooleanExpression nameCondition = asc ? song.name.gt(lastName) : song.name.lt(lastName);
        return nameCondition.or(song.name.eq(lastName).and(idKeyset(asc, lastId)));
    }

    /**
     * 메인 정렬
     */
    private OrderSpecifier<?> mainOrder(SortType sortType, boolean asc) {
        if (sortType == null) {
            return null;
        }

        return switch (sortType) {
            case LIKE -> asc ? song.likeCount.asc() : song.likeCount.desc();
            case NAME -> asc ? song.name.asc() : song.name.desc();
        };
    }

    /**
     * id 정렬
     */
    private OrderSpecifier<Long> idOrder(boolean asc) {
        return asc ? song.songId.asc(): song.songId.desc();
    }
}
