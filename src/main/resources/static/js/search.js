document.addEventListener("DOMContentLoaded", () => {

    const input = document.getElementById("searchInput");
    const button = document.getElementById("searchBtn");

    const popularSection = document.getElementById("popularSection");
    const resultWrapper = document.getElementById("searchResult");
    const emptyBox = document.getElementById("emptyResult");

    const songTitle = document.getElementById("songTitle");
    const albumTitle = document.getElementById("albumTitle");
    const artistTitle = document.getElementById("artistTitle");

    const songList = document.getElementById("songList");
    const albumList = document.getElementById("albumList");
    const artistList = document.getElementById("artistList");

    const songMoreBtn = document.getElementById("songMoreBtn");
    const albumMoreBtn = document.getElementById("albumMoreBtn");
    const artistMoreBtn = document.getElementById("artistMoreBtn");

    button.addEventListener("click", () => {
        const word = input.value.trim();
        if (!word) return;
        location.href = `/search?word=${encodeURIComponent(word)}`;
    });

    loadPopular();

    if (initialWord && initialWord.trim() !== "") {
        popularSection.classList.add("hidden");
        search(initialWord);
    }

    async function search(word) {

        resultWrapper.classList.add("hidden");
        emptyBox.classList.add("hidden");

        try {
            const res = await fetch(`/api/search?word=${encodeURIComponent(word)}`);
            const response = await res.json();

            if (!response.success) {
                showEmpty("검색 실패");
                return;
            }

            renderResult(response.data);

        } catch (e) {
            showEmpty("오류 발생");
        }
    }

    function renderResult(data) {

        const keyword = data.keyword;

        const songs = data.songs || [];
        const albums = data.albums || [];
        const artists = data.artists || [];

        if (!songs.length && !albums.length && !artists.length) {
            showEmpty(`"${keyword}" 검색 결과가 없습니다.`);
            return;
        }

        resultWrapper.classList.remove("hidden");

        songTitle.textContent = `"${keyword}" 관련 음원`;
        albumTitle.textContent = `"${keyword}" 관련 앨범`;
        artistTitle.textContent = `"${keyword}" 관련 아티스트`;

        renderSongs(songs);
        renderAlbums(albums);
        renderArtists(artists);

        handleMore(songMoreBtn, songs.length, "songs", keyword);
        handleMore(albumMoreBtn, albums.length, "albums", keyword);
        handleMore(artistMoreBtn, artists.length, "artists", keyword);
    }

    function handleMore(button, totalCount, type, keyword) {

        if (totalCount === 0) {
            button.classList.add("hidden");
            return;
        }

        button.classList.remove("hidden");

        button.onclick = () => {
            location.href = `/search/${type}?word=${encodeURIComponent(keyword)}`;
        };
    }

    /* ===============================
       ✅ 곡 렌더링 + 클릭 이동
       - 클릭 → /songs/{songId}/page
    =============================== */
    function renderSongs(list) {

        songList.innerHTML = "";

        list.slice(0, 5).forEach(song => {

            const div = document.createElement("div");
            div.className = "item";
            div.dataset.songId = song.songId;

            div.innerHTML = `
                <img src="${song.albumImage || '/images/default.png'}">
                <div>${song.name ?? "-"}</div>
                <div>${song.artistName || ""}</div>
                <div>${song.likeCount ?? 0}</div>
            `;

            // ✅ 곡 클릭 → 단건조회 페이지
            div.addEventListener("click", () => {
                if (!song.songId) return;
                location.href = `/songs/${song.songId}/page`;
            });

            songList.appendChild(div);
        });
    }

    /* ===============================
       ✅ 앨범 렌더링 + 클릭 이동
       - 클릭 → /albums/{albumId}/page
    =============================== */
    function renderAlbums(list) {

        albumList.innerHTML = "";

        list.slice(0, 5).forEach(album => {

            const card = document.createElement("div");
            card.className = "card";
            card.dataset.albumId = album.albumId;

            card.innerHTML = `
                <img src="${album.albumImage || '/images/default.png'}">
                <div>${album.albumName ?? "-"}</div>
                <div style="font-size:12px;color:#666">${album.artistName ?? ""}</div>
            `;

            // ✅ 앨범 클릭 → 앨범 상세
            card.addEventListener("click", () => {
                if (!album.albumId) return;
                location.href = `/albums/${album.albumId}/page`;
            });

            albumList.appendChild(card);
        });
    }

    /* ===============================
       ✅ 아티스트 렌더링 + 클릭 이동
       - 클릭 → /artists/{artistId}
    =============================== */
    function renderArtists(list) {

        artistList.innerHTML = "";

        list.slice(0, 5).forEach(artist => {

            const card = document.createElement("div");
            card.className = "card";
            card.dataset.artistId = artist.artistId;

            card.innerHTML = `
                <div style="font-weight:600">${artist.artistName ?? "-"}</div>
                <div style="font-size:12px;color:#666">
                    좋아요 ${artist.likeCount ?? 0}
                </div>
            `;

            // ✅ 아티스트 클릭 → 아티스트 상세
            card.addEventListener("click", () => {
                if (!artist.artistId) return;
                location.href = `/artists/${artist.artistId}`;
            });

            artistList.appendChild(card);
        });
    }

    function showEmpty(message) {
        emptyBox.classList.remove("hidden");
        emptyBox.textContent = message;
    }

    async function loadPopular() {

        try {
            const res = await fetch("/api/search/popular");
            const response = await res.json();

            if (!response.success) return;

            const container = document.getElementById("popularList");
            container.innerHTML = "";

            const data = response.data || [];

            for (let i = 0; i < 10; i++) {

                const item = data[i];

                const keyword =
                    item && item.keyword
                        ? item.keyword
                        : "-";

                const rank =
                    item && item.rank
                        ? item.rank
                        : i + 1;

                const row = document.createElement("div");
                row.className = "popular-row";

                row.innerHTML = `
                    <span class="rank">${rank}.</span>
                    <span>${keyword}</span>
                `;

                if (keyword !== "-") {
                    row.onclick = () => {
                        location.href = `/search?word=${keyword}`;
                    };
                }

                container.appendChild(row);
            }

        } catch (e) {
            console.log("인기검색어 로드 실패", e);
        }
    }
});
