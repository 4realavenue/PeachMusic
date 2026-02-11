let lastId = null;
let lastSortValue = null;
let hasNext = true;
let loading = false;

let currentSort = "LIKE";
let currentDirection = "DESC";

const listContainer = document.getElementById("songList");
const loadMoreBtn = document.getElementById("loadMoreBtn");
const title = document.getElementById("pageTitle");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");
const sortSelect = document.getElementById("sortSelect");
const directionSelect = document.getElementById("directionSelect");


document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 곡`;

    /* 초기 정렬값 세팅 */
    currentSort = sortSelect?.value || "LIKE";
    currentDirection = directionSelect?.value || "DESC";

    loadSongs();

    /* 검색 */
    searchBtn?.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/songs?word=${encodeURIComponent(word)}`;
    });

    /* 정렬 기준 변경 */
    sortSelect?.addEventListener("change", () => {
        currentSort = sortSelect.value;

        // 🔥 정렬별 기본 방향 자동 설정
        switch (currentSort) {
            case "NAME":
                currentDirection = "ASC";
                break;
            case "LIKE":
            case "PLAY":
            case "RELEASE_DATE":
            default:
                currentDirection = "DESC";
        }

        directionSelect.value = currentDirection;
        resetAndReload();
    });

    /* 방향 변경 */
    directionSelect?.addEventListener("change", () => {
        currentDirection = directionSelect.value;
        resetAndReload();
    });

    /* 더보기 */
    loadMoreBtn?.addEventListener("click", () => {
        if (hasNext && !loading) loadSongs();
    });
});


/* ===============================
   커서 초기화
================================ */
function resetAndReload() {
    lastId = null;
    lastSortValue = null;
    hasNext = true;
    loading = false;
    listContainer.innerHTML = "";
    loadMoreBtn.classList.add("hidden");
    loadSongs();
}


/* ===============================
   데이터 정규화
================================ */
function normalizeSong(song) {
    return {
        songId: song.songId ?? song.song_id,
        name: song.name,
        artistName: song.artistName,
        likeCount: song.likeCount ?? song.like_count ?? 0,
        playCount: song.playCount ?? song.play_count ?? 0,
        releaseDate: song.releaseDate ?? song.release_date,
        albumImage: song.albumImage ?? song.album_image ?? '/images/default.png',
        isDeleted: song.isDeleted ?? song.deleted ?? false
    };
}


/* ===============================
   API 호출
================================ */
async function loadSongs() {

    if (!hasNext || loading) return;
    loading = true;

    const params = new URLSearchParams({
        word: initialWord,
        sortType: currentSort,
        direction: currentDirection
    });

    if (lastId !== null) {
        params.append("lastId", lastId);

        // 🔥 sortType에 맞는 커서 파라미터
        switch (currentSort) {
            case "LIKE":
                params.append("lastLike", lastSortValue);
                break;
            case "NAME":
                params.append("lastName", lastSortValue);
                break;
            case "PLAY":
                params.append("lastPlay", lastSortValue);
                break;
            case "RELEASE_DATE":
                params.append("lastDate", lastSortValue);
                break;
        }
    }

    try {

        const res = await fetch(`/api/search/songs?${params}`);
        const response = await res.json();

        if (!response.success) {
            alert(response.message);
            loading = false;
            return;
        }

        const data = response.data;

        renderSongs(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            lastId = data.cursor.lastId;

            // 🔥 sortType별 커서값 저장
            switch (currentSort) {
                case "LIKE":
                    lastSortValue = data.cursor.lastLike;
                    break;
                case "NAME":
                    lastSortValue = data.cursor.lastName;
                    break;
                case "PLAY":
                    lastSortValue = data.cursor.lastPlay;
                    break;
                case "RELEASE_DATE":
                    lastSortValue = data.cursor.lastDate;
                    break;
            }

            loadMoreBtn.classList.remove("hidden");
        } else {
            loadMoreBtn.classList.add("hidden");
        }

    } catch (e) {
        console.error("곡 검색 오류:", e);
    }

    loading = false;
}


/* ===============================
   렌더링
================================ */
function renderSongs(list) {

    list.forEach(raw => {

        const song = normalizeSong(raw);

        if (song.isDeleted) return;

        const row = document.createElement("div");
        row.className = "song-row";

        row.innerHTML = `
            <img src="${song.albumImage}" alt="album">
            <div>${song.name}</div>
            <div>${song.artistName}</div>
            <div>❤️ ${song.likeCount}</div>
        `;

        row.onclick = () =>
            location.href = `/songs/${song.songId}`;

        listContainer.appendChild(row);
    });
}
