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

    // 🔥 더보기 버튼
    const songMoreBtn = document.getElementById("songMoreBtn");
    const albumMoreBtn = document.getElementById("albumMoreBtn");
    const artistMoreBtn = document.getElementById("artistMoreBtn");

    /* ===============================
       검색 버튼
    =============================== */
    button.addEventListener("click", () => {
        const word = input.value.trim();
        if (!word) return;
        location.href = `/search?word=${encodeURIComponent(word)}`;
    });

    /* ===============================
       초기 인기검색어 로드
    =============================== */
    loadPopular();

    if (initialWord && initialWord.trim() !== "") {
        popularSection.classList.add("hidden");
        search(initialWord);
    }

    /* ===============================
       검색 API 호출
    =============================== */
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

    /* ===============================
       결과 렌더링
    =============================== */
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

        songTitle.textContent = `"${keyword}" 관련 곡 (${songs.length})`;
        albumTitle.textContent = `"${keyword}" 관련 앨범 (${albums.length})`;
        artistTitle.textContent = `"${keyword}" 관련 아티스트 (${artists.length})`;

        renderSongs(songs);
        renderAlbums(albums);
        renderArtists(artists);

        // 🔥 더보기 버튼 처리
        handleMore(songMoreBtn, songs.length, "songs", keyword);
        handleMore(albumMoreBtn, albums.length, "albums", keyword);
        handleMore(artistMoreBtn, artists.length, "artists", keyword);
    }

    /* ===============================
       더보기 버튼 로직
    =============================== */
    function handleMore(button, totalCount, type, keyword) {

        if (totalCount === 0) {
            button.classList.add("hidden");
            return;
        }

        button.classList.remove("hidden");

        button.onclick = () => {
            location.href =
                `/search/${type}?word=${encodeURIComponent(keyword)}`;
        };
    }

    /* ===============================
       곡 렌더링 (5개 미리보기)
    =============================== */
    function renderSongs(list) {

        songList.innerHTML = "";

        list.slice(0, 5).forEach(song => {

            const div = document.createElement("div");
            div.className = "item";

            div.innerHTML = `
                <img src="${song.albumImage || '/images/default.png'}">
                <div>${song.name}</div>
                <div>${song.artistName || ""}</div>
                <div>${song.likeCount ?? 0}</div>
            `;

            songList.appendChild(div);
        });
    }

    /* ===============================
       앨범 렌더링 (5개)
    =============================== */
    function renderAlbums(list) {

        albumList.innerHTML = "";

        list.slice(0, 5).forEach(album => {

            const card = document.createElement("div");
            card.className = "card";

            card.innerHTML = `
                <img src="${album.albumImage || '/images/default.png'}">
                <div>${album.albumName}</div>
                <div style="font-size:12px;color:#666">${album.artistName}</div>
            `;

            albumList.appendChild(card);
        });
    }

    /* ===============================
       아티스트 렌더링 (5개)
    =============================== */
    function renderArtists(list) {

        artistList.innerHTML = "";

        list.slice(0, 5).forEach(artist => {

            const card = document.createElement("div");
            card.className = "card";

            card.innerHTML = `
                <div style="font-weight:600">${artist.artistName}</div>
                <div style="font-size:12px;color:#666">
                    좋아요 ${artist.likeCount ?? 0}
                </div>
            `;

            artistList.appendChild(card);
        });
    }

    /* ===============================
       결과 없음
    =============================== */
    function showEmpty(message) {
        emptyBox.classList.remove("hidden");
        emptyBox.textContent = message;
    }

    /* ===============================
       인기 검색어 (Top 10 고정)
    =============================== */
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
