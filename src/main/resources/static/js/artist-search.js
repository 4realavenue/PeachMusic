let lastId = null;
let lastSortValue = null;
let hasNext = true;
let loading = false;

const grid = document.getElementById("artistGrid");
const loadMoreBtn = document.getElementById("loadMoreBtn");
const title = document.getElementById("pageTitle");
const searchBtn = document.getElementById("searchBtn");
const searchInput = document.getElementById("searchInput");

document.addEventListener("DOMContentLoaded", () => {

    if (!initialWord || initialWord.trim() === "") return;

    title.textContent = `"${initialWord}"와 관련된 아티스트`;

    loadArtists();

    searchBtn.addEventListener("click", () => {
        const word = searchInput.value.trim();
        if (!word) return;
        location.href = `/search/artists?word=${encodeURIComponent(word)}`;
    });

    loadMoreBtn.addEventListener("click", () => {
        if (hasNext && !loading) loadArtists();
    });
});


async function loadArtists() {

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

        const res = await fetch(`/api/search/artists?${params}`);
        const response = await res.json();

        if (!response.success) {
            alert(response.message);
            loading = false;
            return;
        }

        const data = response.data;

        renderArtists(data.content);

        hasNext = data.hasNext;

        if (hasNext && data.cursor) {
            lastId = data.cursor.lastId;
            lastSortValue = data.cursor.lastSortValue;
            loadMoreBtn.classList.remove("hidden");
        } else {
            loadMoreBtn.classList.add("hidden");
        }

    } catch (e) {
        console.error("아티스트 검색 오류:", e);
    }

    loading = false;
}


function renderArtists(list) {

    list.forEach(artist => {

        if (artist.isDeleted) return;

        const card = document.createElement("div");
        card.className = "artist-card";

        card.innerHTML = `
            <div class="artist-img"></div>
            <div class="artist-name">${artist.artistName}</div>
            <div class="artist-like">❤️ ${artist.likeCount ?? 0}</div>
        `;

        card.onclick = () =>
            location.href = `/artists/${artist.artistId}`;

        grid.appendChild(card);
    });
}
