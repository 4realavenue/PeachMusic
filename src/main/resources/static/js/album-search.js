let lastId = null;
let lastSortValue = null;
let hasNext = true;
let loading = false;

const grid = document.getElementById("albumGrid");
const loadMoreBtn = document.getElementById("loadMoreBtn");
const title = document.getElementById("pageTitle");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");

document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 앨범`;

    loadAlbums();

    searchBtn.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/albums?word=${encodeURIComponent(word)}`;
    });

    loadMoreBtn.addEventListener("click", () => {
        if (hasNext && !loading) loadAlbums();
    });
});


async function loadAlbums() {

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

        const res = await fetch(`/api/search/albums?${params}`);
        const response = await res.json();

        if (!response.success) {
            alert(response.message);
            loading = false;
            return;
        }

        const data = response.data;

        renderAlbums(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            lastId = data.cursor.lastId;
            lastSortValue = data.cursor.lastSortValue;
            loadMoreBtn.classList.remove("hidden");
        } else {
            loadMoreBtn.classList.add("hidden");
        }

    } catch (e) {
        console.error("앨범 검색 오류:", e);
    }

    loading = false;
}


function renderAlbums(list) {

    list.forEach(album => {

        if (album.isDeleted) return;

        const card = document.createElement("div");
        card.className = "album-card";

        card.innerHTML = `
            <img src="${album.albumImage || '/images/default.png'}" alt="album">
            <div class="album-name">${album.albumName}</div>
            <div class="album-artist">${album.artistName}</div>
            <div class="album-like">❤️ ${album.likeCount ?? 0}</div>
        `;

        card.onclick = () =>
            location.href = `/albums/${album.albumId}`;

        grid.appendChild(card);
    });
}
