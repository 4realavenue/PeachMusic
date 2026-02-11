let lastId = null;
let lastSortValue = null;
let hasNext = true;
let loading = false;

const listContainer = document.getElementById("songList");
const loadMoreBtn = document.getElementById("loadMoreBtn");
const title = document.getElementById("pageTitle");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");

document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 곡`;

    loadSongs();

    searchBtn.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/songs?word=${encodeURIComponent(word)}`;
    });

    loadMoreBtn.addEventListener("click", () => {
        if (hasNext && !loading) loadSongs();
    });
});


/* ===============================
   데이터 정규화 (snake + camel 대응)
================================ */
function normalizeSong(song) {
    return {
        songId: song.songId ?? song.song_id,
        name: song.name,
        artistName: song.artistName,
        likeCount: song.likeCount ?? song.like_count ?? 0,
        albumImage: song.albumImage ?? song.album_image ?? '/images/default.png',
        isDeleted: song.isDeleted ?? song.deleted ?? false
    };
}


/* ===============================
   API 호출
================================ */
async function loadSongs() {

    if (!hasNext) return;
    loading = true;

    const params = new URLSearchParams({
        word: initialWord,
        sortType: "LIKE",
        direction: "DESC"
    });

    if (lastId !== null) {
        params.append("lastId", lastId);
        params.append("lastLike", lastSortValue);
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
            lastSortValue = data.cursor.lastSortValue;
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
