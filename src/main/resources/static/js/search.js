document.addEventListener("DOMContentLoaded", () => {
    const $ = (id) => document.getElementById(id);

    const input = $("searchInput");
    const button = $("searchBtn");

    const popularSection = $("popularSection");
    const popularList = $("popularList");

    const resultWrapper = $("searchResult");
    const emptyBox = $("emptyResult");

    const songTitle = $("songTitle");
    const albumTitle = $("albumTitle");
    const artistTitle = $("artistTitle");

    const songList = $("songList");
    const albumList = $("albumList");
    const artistList = $("artistList");

    const songMoreBtn = $("songMoreBtn");
    const albumMoreBtn = $("albumMoreBtn");
    const artistMoreBtn = $("artistMoreBtn");

    const required = [
        input, button,
        popularSection, popularList,
        resultWrapper, emptyBox,
        songTitle, albumTitle, artistTitle,
        songList, albumList, artistList,
        songMoreBtn, albumMoreBtn, artistMoreBtn,
    ];
    if (required.some((el) => !el)) {
        console.error("[search] 필수 DOM id 누락.");
        return;
    }

    /* =========================
       검색 버튼
    ========================= */
    button.addEventListener("click", () => {
        const word = input.value.trim();
        if (!word) return;
        location.href = `/search?word=${encodeURIComponent(word)}`;
    });

    input.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            button.click();
        }
    });

    loadPopular();

    if (typeof initialWord !== "undefined" && initialWord && String(initialWord).trim() !== "") {
        popularSection.classList.add("hidden");
        search(initialWord);
    }

    /* =========================
       Search
    ========================= */
    async function search(word) {
        resultWrapper.classList.add("hidden");
        emptyBox.classList.add("hidden");

        try {
            const url = `/api/search?word=${encodeURIComponent(word)}`;
            const res = await fetch(url);
            const response = await res.json();

            if (!response?.success) {
                showEmpty("검색 실패");
                return;
            }

            renderResult(response.data);
        } catch (e) {
            console.error(e);
            showEmpty("오류 발생");
        }
    }

    function renderResult(data) {
        const keyword = data.keyword ?? "";

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
       Songs (좋아요 제거)
    =============================== */
    function renderSongs(list) {
        songList.innerHTML = "";

        list.slice(0, 5).forEach((song) => {
            const div = document.createElement("div");
            div.className = "item";

            div.innerHTML = `
                <img src="${song.albumImage ?? "/images/default.png"}"
                     alt=""
                     loading="lazy">

                <div class="song-info">
                    <div class="song-title">${song.name ?? "-"}</div>
                    <div class="song-sub">
                        ${song.artistName ?? "-"} · ${song.albumName ?? "-"}
                    </div>
                </div>
            `;

            div.addEventListener("click", () => {
                location.href = `/songs/${song.songId}/page`;
            });

            songList.appendChild(div);
        });
    }

    /* ===============================
       Albums (좋아요 제거)
    =============================== */
    function renderAlbums(list) {
        albumList.innerHTML = "";

        list.slice(0, 5).forEach((album) => {
            const card = document.createElement("div");
            card.className = "card";

            card.innerHTML = `
                <img src="${album.albumImage ?? "/images/default.png"}"
                     alt=""
                     loading="lazy">

                <div class="card-title">${album.albumName ?? "-"}</div>
                <div class="card-sub">${album.artistName ?? "-"}</div>
            `;

            card.addEventListener("click", () => {
                location.href = `/albums/${album.albumId}/page`;
            });

            albumList.appendChild(card);
        });
    }

    /* ===============================
       Artists (좋아요 제거)
    =============================== */
    function renderArtists(list) {
        artistList.innerHTML = "";

        list.slice(0, 5).forEach((artist) => {
            const card = document.createElement("div");
            card.className = "card artist-card";

            const name = artist.artistName ?? "-";

            const profileWrap = document.createElement("div");
            profileWrap.className = "artist-profile";

            if (artist.profileImage) {
                const img = document.createElement("img");
                img.src = artist.profileImage;
                img.className = "artist-profile-img";
                profileWrap.appendChild(img);
            } else {
                const placeholder = document.createElement("div");
                placeholder.className = "artist-profile placeholder";
                placeholder.textContent = name.charAt(0).toUpperCase();
                profileWrap.appendChild(placeholder);
            }

            const title = document.createElement("div");
            title.className = "card-title";
            title.textContent = name;

            card.appendChild(profileWrap);
            card.appendChild(title);

            card.addEventListener("click", () => {
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
            if (!response?.success) return;

            popularList.innerHTML = "";

            const data = Array.isArray(response.data) ? response.data : [];

            for (let i = 0; i < 10; i++) {
                const keyword = data[i]?.keyword ?? "-";

                const row = document.createElement("div");
                row.className = "popular-row";
                row.innerHTML = `
                    <span class="rank">${i + 1}.</span>
                    <span>${keyword}</span>
                `;

                if (keyword !== "-") {
                    row.onclick = () => {
                        location.href = `/search?word=${encodeURIComponent(keyword)}`;
                    };
                }

                popularList.appendChild(row);
            }
        } catch (e) {
            console.log("인기검색어 로드 실패", e);
        }
    }
});