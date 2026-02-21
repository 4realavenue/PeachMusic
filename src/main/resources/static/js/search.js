import { authFetch } from "/js/auth.js";

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

    // ✅ HTML에 필수 요소가 없으면 JS가 죽으니까 방어
    const required = [
        input, button,
        popularSection, popularList,
        resultWrapper, emptyBox,
        songTitle, albumTitle, artistTitle,
        songList, albumList, artistList,
        songMoreBtn, albumMoreBtn, artistMoreBtn,
    ];
    if (required.some((el) => !el)) {
        console.error("[search] 필수 DOM id 누락. search.html 구조를 확인하세요.");
        return;
    }

    /* =========================
       Token
    ========================= */
    const hasToken = () => !!localStorage.getItem("accessToken");
    const authHeaderValue = () => localStorage.getItem("accessToken") || "";

    /* =========================
       Login toast
    ========================= */
    function showLoginToast() {
        let el = $("loginPopup");
        if (!el) {
            el = document.createElement("div");
            el.id = "loginPopup";
            el.className = "login-popup hidden";
            el.textContent = "로그인이 필요합니다.";
            document.body.appendChild(el);
        }

        el.classList.remove("hidden");
        el.classList.add("show");
        setTimeout(() => {
            el.classList.remove("show");
            el.classList.add("hidden");
        }, 2000);
    }

    /* =========================
       Utils
    ========================= */
    function escapeHtml(str) {
        return String(str ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll('"', "&quot;")
            .replaceAll("'", "&#039;");
    }

    function resolveImageUrl(path, fallback = "/images/default.png") {
        if (!path) return fallback;
        if (path.startsWith("http://") || path.startsWith("https://")) return path;
        if (path.startsWith("/")) return path;
        return `/${path}`;
    }

    function resolveArtistProfileUrl(imagePath) {
        if (!imagePath) return "";
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
        if (imagePath.startsWith("/")) return imagePath;
        return `/${imagePath}`;
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

    /* =========================
       Popular
    ========================= */
    loadPopular();

    if (typeof initialWord !== "undefined" && initialWord && String(initialWord).trim() !== "") {
        popularSection.classList.add("hidden");
        search(initialWord);
    }

    /* =========================
       Like toggle
    ========================= */
    async function likeToggle(url) {
        if (!hasToken()) {
            showLoginToast();
            return null;
        }
        try {
            const res = await authFetch(url, { method: "POST" });
            if (!res) return null;
            const json = await res.json();
            if (!json?.success) return null;
            return json.data; // { liked, likeCount }
        } catch (e) {
            console.error("like toggle fail:", e);
            return null;
        }
    }

    /* =========================
       Search (토큰 있으면 Authorization 붙임)
    ========================= */
    async function search(word) {
        resultWrapper.classList.add("hidden");
        emptyBox.classList.add("hidden");
        emptyBox.textContent = "";

        try {
            const url = `/api/search?word=${encodeURIComponent(word)}`;

            let res;
            if (hasToken()) {
                res = await fetch(url, {
                    method: "GET",
                    headers: { Authorization: authHeaderValue() },
                });
            } else {
                res = await fetch(url, { method: "GET" });
            }

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
       Songs
    =============================== */
    function renderSongs(list) {
        songList.innerHTML = "";

        list.slice(0, 5).forEach((song) => {
            const div = document.createElement("div");
            div.className = "item";
            div.dataset.songId = song.songId;

            const imgSrc = resolveImageUrl(song.albumImage);
            const title = escapeHtml(song.name ?? "-");
            const artist = escapeHtml(song.artistName ?? "-");
            const album = escapeHtml(song.albumName ?? "-");
            const likeCount = song.likeCount ?? 0;

            const disabled = !hasToken();

            div.innerHTML = `
                <img src="${escapeHtml(imgSrc)}" alt="" loading="lazy"
                     onerror="this.onerror=null; this.src='/images/default.png';">

                <div class="song-info">
                    <div class="song-title">${title}</div>
                    <div class="song-sub">${artist} · ${album}</div>
                </div>

                <div class="like-area">
                    <span class="like-number">${likeCount}</span>
                    <button class="heart-btn ${song.liked ? "liked" : ""} ${disabled ? "disabled" : ""}"
                            type="button" aria-label="좋아요">❤</button>
                </div>
            `;

            const heartBtn = div.querySelector(".heart-btn");
            const likeNumEl = div.querySelector(".like-number");

            heartBtn.addEventListener("click", async (e) => {
                e.stopPropagation();
                if (!hasToken()) {
                    showLoginToast();
                    return;
                }

                const result = await likeToggle(`/api/songs/${song.songId}/likes`);
                if (!result) return;

                const likedNow = !!result.liked;
                const countNow = result.likeCount ?? 0;

                heartBtn.classList.toggle("liked", likedNow);
                likeNumEl.textContent = String(countNow);
            });

            div.addEventListener("click", () => {
                if (!song.songId) return;
                location.href = `/songs/${song.songId}/page`;
            });

            songList.appendChild(div);
        });
    }

    /* ===============================
       Albums
    =============================== */
    function renderAlbums(list) {
        albumList.innerHTML = "";

        list.slice(0, 5).forEach((album) => {
            const card = document.createElement("div");
            card.className = "card";
            card.dataset.albumId = album.albumId;

            const imgSrc = resolveImageUrl(album.albumImage);
            const name = escapeHtml(album.albumName ?? "-");
            const artist = escapeHtml(album.artistName ?? "-");
            const likeCount = album.likeCount ?? 0;

            const disabled = !hasToken();

            card.innerHTML = `
                <img src="${escapeHtml(imgSrc)}" alt="" loading="lazy"
                     onerror="this.onerror=null; this.src='/images/default.png';">

                <div class="card-title">${name}</div>
                <div class="card-sub">${artist}</div>

                <div class="card-bottom">
                    <span class="like-number">${likeCount}</span>
                    <button class="heart-btn ${album.liked ? "liked" : ""} ${disabled ? "disabled" : ""}"
                            type="button" aria-label="좋아요">❤</button>
                </div>
            `;

            const heartBtn = card.querySelector(".heart-btn");
            const likeNumEl = card.querySelector(".like-number");

            heartBtn.addEventListener("click", async (e) => {
                e.stopPropagation();
                if (!hasToken()) {
                    showLoginToast();
                    return;
                }

                const result = await likeToggle(`/api/albums/${album.albumId}/likes`);
                if (!result) return;

                const likedNow = !!result.liked;
                const countNow = result.likeCount ?? 0;

                heartBtn.classList.toggle("liked", likedNow);
                likeNumEl.textContent = String(countNow);
            });

            card.addEventListener("click", () => {
                if (!album.albumId) return;
                location.href = `/albums/${album.albumId}/page`;
            });

            albumList.appendChild(card);
        });
    }

    /* ===============================
       ✅ Artists (원형 + 첫글자 placeholder 통일)
    =============================== */
    function renderArtists(list) {
        artistList.innerHTML = "";

        list.slice(0, 5).forEach((artist) => {
            const card = document.createElement("div");
            card.className = "card artist-card";
            card.dataset.artistId = artist.artistId;

            const nameRaw = artist.artistName ?? "-";
            const likeCount = artist.likeCount ?? 0;

            const disabled = !hasToken();

            // 프로필 영역(원형)
            const profileWrap = document.createElement("div");
            profileWrap.className = "artist-profile";

            const profileUrl = resolveArtistProfileUrl(artist.profileImage);

            if (profileUrl) {
                const img = document.createElement("img");
                img.className = "artist-profile-img";
                img.src = profileUrl;
                img.alt = "artist";
                img.loading = "lazy";
                img.onerror = () => img.replaceWith(createArtistPlaceholder(nameRaw));
                profileWrap.appendChild(img);
            } else {
                profileWrap.appendChild(createArtistPlaceholder(nameRaw));
            }

            // 텍스트
            const titleEl = document.createElement("div");
            titleEl.className = "card-title";
            titleEl.textContent = nameRaw;

            // 하단 좋아요
            const bottom = document.createElement("div");
            bottom.className = "card-bottom";

            const likeNum = document.createElement("span");
            likeNum.className = "like-number";
            likeNum.textContent = String(likeCount);

            const heartBtn = document.createElement("button");
            heartBtn.className = `heart-btn ${artist.liked ? "liked" : ""} ${disabled ? "disabled" : ""}`;
            heartBtn.type = "button";
            heartBtn.setAttribute("aria-label", "좋아요");
            heartBtn.textContent = "❤";

            bottom.appendChild(likeNum);
            bottom.appendChild(heartBtn);

            card.appendChild(profileWrap);
            card.appendChild(titleEl);
            card.appendChild(bottom);

            heartBtn.addEventListener("click", async (e) => {
                e.stopPropagation();
                if (!hasToken()) {
                    showLoginToast();
                    return;
                }

                const result = await likeToggle(`/api/artists/${artist.artistId}/likes`);
                if (!result) return;

                const likedNow = !!result.liked;
                const countNow = result.likeCount ?? 0;

                heartBtn.classList.toggle("liked", likedNow);
                likeNum.textContent = String(countNow);
            });

            card.addEventListener("click", () => {
                if (!artist.artistId) return;
                location.href = `/artists/${artist.artistId}`;
            });

            artistList.appendChild(card);
        });
    }

    function createArtistPlaceholder(name) {
        const div = document.createElement("div");
        div.className = "artist-profile placeholder";
        const firstChar = name ? String(name).charAt(0).toUpperCase() : "?";
        div.textContent = firstChar;
        return div;
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

            // ✅ keyword 정규화: null/undefined/빈문자/공백/"null" => "-"
            const normalizeKeyword = (kw) => {
                if (kw === null || kw === undefined) return "-";
                const s = String(kw).trim();
                if (!s) return "-";
                if (s.toLowerCase() === "null") return "-";
                return s;
            };

            // ✅ 1~10까지 무조건 출력
            for (let i = 0; i < 10; i++) {
                const item = data[i] ?? null;

                const keyword = normalizeKeyword(item?.keyword);
                const rank = i + 1;

                const row = document.createElement("div");
                row.className = "popular-row";

                row.innerHTML = `
                <span class="rank">${rank}.</span>
                <span>${escapeHtml(keyword)}</span>
            `;

                // ✅ "-" 인 경우 클릭 막기
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