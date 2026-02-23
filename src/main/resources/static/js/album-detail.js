import { authFetch, getToken } from "/js/auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

const hasToken = !!getToken();

/* ✅ 플레이리스트 API */
const PLAYLIST_LIST_API = "/api/playlists";
const PLAYLIST_ADD_SONG_API = (playlistId) => `/api/playlists/${playlistId}/songs`;

/* =========================
   Global Player
========================= */
function getGlobalAudioEl() {
    return document.querySelector(".player audio") || document.getElementById("audioPlayer") || null;
}

let currentPlayingSongId = null;
let currentPlayBtn = null;

function isSameTrackPlaying(globalAudio, audioUrl) {
    if (!globalAudio || !globalAudio.src || !audioUrl) return false;
    const currentFile = String(globalAudio.src).split("/").pop();
    const nextFile = String(audioUrl).split("/").pop();
    return currentFile && nextFile && currentFile === nextFile;
}

function setPlayBtnState(btn, isPlaying) {
    if (!btn) return;
    btn.classList.toggle("playing", isPlaying);
    btn.textContent = isPlaying ? "❚❚" : "▶";
    btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
}

/* =========================
   Utils
========================= */
function resolveImageUrl(imagePath) {
    if (!imagePath) return "/images/default.png";
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
    if (imagePath.startsWith("/")) return imagePath;
    return `/${imagePath}`;
}

function decodeHtmlEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

function showLoginPopup() {
    const popup = document.getElementById("loginPopup");
    if (!popup) return;

    popup.classList.remove("hidden");
    popup.classList.add("show");
    setTimeout(() => {
        popup.classList.remove("show");
        popup.classList.add("hidden");
    }, 2000);
}

function formatDuration(seconds) {
    if (seconds == null) return "-";
    const s = Number(seconds);
    const min = Math.floor(s / 60);
    const sec = s % 60;
    return `${min}:${String(sec).padStart(2, "0")}`;
}

function formatDate(iso) {
    if (!iso) return "-";
    return String(iso).replaceAll("-", ".");
}

function renderArtistLinks(artistList, containerEl) {
    if (!containerEl) return;
    containerEl.innerHTML = "";

    const list = Array.isArray(artistList) ? artistList : [];
    if (list.length === 0) {
        containerEl.textContent = "-";
        return;
    }

    list.forEach((a, idx) => {
        const name = decodeHtmlEntities(a?.artistName ?? a?.name ?? "-");
        const id = a?.artistId ?? a?.id ?? a?.artist_id ?? null;

        if (idx > 0) {
            containerEl.appendChild(document.createTextNode(", "));
        }

        if (id != null && String(id).length > 0) {
            const link = document.createElement("a");
            link.className = "album-artist-link";
            link.href = `/artists/${id}`;
            link.textContent = name;
            link.addEventListener("click", (e) => e.stopPropagation());
            containerEl.appendChild(link);
        } else {
            const span = document.createElement("span");
            span.textContent = name;
            containerEl.appendChild(span);
        }
    });
}

/* =========================
   전역 오디오 이벤트 싱크
========================= */
function wireGlobalAudioSync() {
    const globalAudio = getGlobalAudioEl();
    if (!globalAudio) return;

    const sync = () => {
        if (!currentPlayBtn) return;

        const url = currentPlayBtn?.dataset?.audioUrl;
        const same = url ? isSameTrackPlaying(globalAudio, url) : false;
        const isPlaying = same && !globalAudio.paused;

        setPlayBtnState(currentPlayBtn, isPlaying);

        if (!same) {
            currentPlayingSongId = null;
            currentPlayBtn = null;
        }
    };

    globalAudio.addEventListener("play", sync);
    globalAudio.addEventListener("pause", sync);
    globalAudio.addEventListener("ended", () => {
        if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);
        currentPlayingSongId = null;
        currentPlayBtn = null;
    });
}

/* =========================
   ✅ /play 호출 (streamingUrl)
   - 플레이어는 playSongFromPage에서 /play 재호출 금지
   - 페이지에서 "트랙 전환 시"에만 /play 호출
========================= */
async function fetchStreamingUrl(songId) {
    const url = `/api/songs/${songId}/play`;

    const res = getToken()
        ? await authFetch(url, { method: "GET" })
        : await fetch(url, { method: "GET" });

    if (!res) return null;

    const payload = await res.json().catch(() => null);

    if (!res.ok || payload?.success === false) {
        alert(payload?.message || "재생에 실패했습니다.");
        return null;
    }

    return resolveAudioUrl(payload?.data?.streamingUrl ?? null);
}

/* =========================
   ✅ Album songList -> Player Queue tracks
========================= */
function buildAlbumTracks(songList) {
    const list = Array.isArray(songList) ? songList : [];
    return list
        .map((s) => ({
            songId: Number(s?.songId),
            title: decodeHtmlEntities(s?.name ?? "Unknown"),
        }))
        .filter((t) => Number.isFinite(t.songId));
}

/* =========================
   Playlist Modal
========================= */
function wirePlaylistModalClose() {
    const modal = document.getElementById("playlistModal");
    const closeBtn = document.getElementById("playlistModalClose");
    const cancelBtn = document.getElementById("playlistModalCancel");

    if (!modal) return;

    modal.addEventListener("click", (e) => {
        if (e.target?.dataset?.close === "true") closePlaylistModal();
    });

    closeBtn?.addEventListener("click", closePlaylistModal);
    cancelBtn?.addEventListener("click", closePlaylistModal);

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !modal.classList.contains("hidden")) {
            closePlaylistModal();
        }
    });
}

function closePlaylistModal() {
    const modal = document.getElementById("playlistModal");
    if (!modal) return;
    modal.classList.add("hidden");
    modal.setAttribute("aria-hidden", "true");
}

async function openPlaylistModalAndAdd(songId) {
    const modal = document.getElementById("playlistModal");
    const listEl = document.getElementById("playlistList");
    if (!modal || !listEl) return;

    modal.classList.remove("hidden");
    modal.setAttribute("aria-hidden", "false");

    listEl.innerHTML = `<div class="pl-empty">불러오는 중...</div>`;

    const res = await authFetch(PLAYLIST_LIST_API, { method: "GET" });
    if (!res) return;

    let payload = null;
    try {
        payload = await res.json();
    } catch {
        payload = null;
    }

    if (!res.ok || !payload?.success) {
        listEl.innerHTML = `<div class="pl-empty">플레이리스트 목록 조회 실패</div>`;
        return;
    }

    const playlists = payload.data ?? [];
    if (playlists.length === 0) {
        listEl.innerHTML = `<div class="pl-empty">플레이리스트가 없습니다. 먼저 생성해주세요.</div>`;
        return;
    }

    listEl.innerHTML = "";

    playlists.forEach((pl) => {
        const btn = document.createElement("button");
        btn.type = "button";
        btn.className = "pl-item";

        const imgPath = pl.playlistImage ? resolveImageUrl(pl.playlistImage) : null;
        const imgHtml = imgPath ? `<img src="${imgPath}" alt="">` : `<div class="pl-thumb-ph">♪</div>`;

        btn.innerHTML = `
      <div class="pl-thumb">${imgHtml}</div>
      <div class="pl-meta">
        <div class="pl-name">${pl.playlistName ?? "-"}</div>
        <div class="pl-sub">클릭하면 추가</div>
      </div>
    `;

        btn.addEventListener("click", async () => {
            await addSongToPlaylist(pl.playlistId, songId);
        });

        listEl.appendChild(btn);
    });
}

async function addSongToPlaylist(playlistId, songId) {
    try {
        const body = { songIdSet: [Number(songId)] };

        const res = await authFetch(PLAYLIST_ADD_SONG_API(playlistId), {
            method: "POST",
            body: JSON.stringify(body),
        });

        if (!res) return;

        let payload = null;
        try {
            payload = await res.json();
        } catch {
            payload = null;
        }

        if (!res.ok) {
            const msg = payload?.message;

            if (res.status === 400) return alert(msg || "플레이리스트에 담을 음원을 입력해 주세요.");
            if (res.status === 403) return alert(msg || "접근 권한이 없습니다.");
            if (res.status === 404) return alert(msg || "플레이리스트가 존재하지 않습니다.");
            if (res.status === 409) return alert(msg || "동일한 곡이 플레이리스트에 있습니다.");

            return alert(msg || "플레이리스트 추가에 실패했습니다.");
        }

        if (!payload?.success) {
            alert(payload?.message || "플레이리스트 추가에 실패했습니다.");
            return;
        }

        alert("플레이리스트에 추가했습니다.");
        closePlaylistModal();
    } catch (e) {
        console.error(e);
        alert("서버 오류가 발생했습니다.");
    }
}

/* =========================
   Main
========================= */
document.addEventListener("DOMContentLoaded", () => {
    wirePlaylistModalClose();
    wireGlobalAudioSync();
    loadAlbum();
});

async function loadAlbum() {
    if (!albumId) return;

    // 🔥 핵심 수정: 로그인 시 authFetch 사용
    const res = hasToken
        ? await authFetch(`/api/albums/${albumId}`, { method: "GET" })
        : await fetch(`/api/albums/${albumId}`);

    if (!res) return;

    const payload = await res.json();
    if (!payload?.success) return;

    const album = payload.data;

    // ✅ 렌더 시점 토큰 상태(버튼 disabled 클래스용)
    const tokenNow = !!getToken();

    // 기본 정보
    document.getElementById("albumImage").src = resolveImageUrl(album.albumImage);
    document.getElementById("albumName").textContent = decodeHtmlEntities(album.albumName ?? "-");
    document.getElementById("albumReleaseDate").textContent = formatDate(album.albumReleaseDate);

    // ✅ 아티스트 이름 클릭 → 아티스트 상세로
    renderArtistLinks(album.artistList, document.getElementById("albumArtists"));

    // ✅ 앨범 좋아요
    const heartBtn = document.getElementById("heartBtn");
    const likeCountEl = document.getElementById("likeCount");

    likeCountEl.textContent = String(album.likeCount ?? 0);
    heartBtn.classList.toggle("liked", album.liked === true);

    heartBtn.addEventListener("click", async (e) => {
        e.stopPropagation();

        // ✅ 고정 hasToken 대신 매번 현재 토큰 체크
        if (!getToken()) {
            showLoginPopup();
            return;
        }

        const likeRes = await authFetch(`/api/albums/${albumId}/likes`, { method: "POST" });
        if (!likeRes) return;

        const likePayload = await likeRes.json();
        if (!likePayload?.success) return;

        const { liked, likeCount } = likePayload.data;
        heartBtn.classList.toggle("liked", liked === true);
        likeCountEl.textContent = String(likeCount ?? 0);
    });

    // ✅ 컨텍스트 큐(수록곡 리스트) 생성
    const tracks = buildAlbumTracks(album.songList);

    // 수록곡 렌더
    const ul = document.getElementById("songList");
    ul.innerHTML = "";

    (album.songList ?? []).forEach((song) => {
        const li = document.createElement("li");
        li.className = "track";
        li.dataset.id = String(song.songId);

        const songName = decodeHtmlEntities(song.name ?? "-");
        const songTitle = decodeHtmlEntities(song.name ?? "");

        li.innerHTML = `
      <div class="track-no">${song.position ?? "-"}</div>

      <button class="track-play" type="button" aria-label="재생">▶</button>

      <div class="track-name" title="${songTitle}">${songName}</div>
      <div class="track-duration">${formatDuration(song.duration)}</div>

      <div class="track-like">
        <span class="like-group">
          <span class="mini-like-count">${song.likeCount ?? 0}</span>
          <button class="mini-heart-btn ${(song.liked ?? song.liked) ? "liked" : ""} ${!tokenNow ? "disabled" : ""}"
                  type="button"
                  aria-label="음원 좋아요">❤</button>
        </span>

        <button class="add-pl-btn ${!tokenNow ? "disabled" : ""}"
                type="button"
                aria-label="플레이리스트에 추가"
                data-song-id="${song.songId}">＋</button>
      </div>
    `;

        // row 클릭 → 상세 이동(버튼 클릭 제외)
        li.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".mini-heart-btn")) return;
            if (e.target.closest(".add-pl-btn")) return;
            location.href = `/songs/${song.songId}/page`;
        });

        // ✅ 재생 버튼: setPlayerQueue + /play + playSongFromPage
        const playBtn = li.querySelector(".track-play");
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (typeof window.setPlayerQueue !== "function" || typeof window.playSongFromPage !== "function") {
                alert("전역 플레이어가 아직 로드되지 않았습니다.");
                return;
            }

            window.setPlayerQueue(tracks, song.songId, { loop: true, contextKey: `album:${albumId}` });

            const audioUrl = await fetchStreamingUrl(song.songId);
            if (!audioUrl) return;

            playBtn.dataset.audioUrl = audioUrl;

            if (currentPlayBtn && currentPlayBtn !== playBtn) {
                setPlayBtnState(currentPlayBtn, false);
            }
            currentPlayingSongId = song.songId;
            currentPlayBtn = playBtn;

            try {
                await window.playSongFromPage(audioUrl, songName, song.songId);

                const globalAudio = getGlobalAudioEl();
                const same = isSameTrackPlaying(globalAudio, audioUrl);
                if (!same) setPlayBtnState(playBtn, true);
            } catch (err) {
                console.error(err);
                alert("재생에 실패했습니다.");
                setPlayBtnState(playBtn, false);
                currentPlayingSongId = null;
                currentPlayBtn = null;
            }
        });

        // ✅ 리스트 하트(좋아요)
        const miniHeartBtn = li.querySelector(".mini-heart-btn");
        const miniLikeCountEl = li.querySelector(".mini-like-count");

        miniHeartBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            // ✅ 고정 hasToken 대신 매번 현재 토큰 체크
            if (!getToken()) {
                showLoginPopup();
                return;
            }

            const likeRes = await authFetch(`/api/songs/${song.songId}/likes`, { method: "POST" });
            if (!likeRes) return;

            const likePayload = await likeRes.json();
            if (!likePayload?.success) return;

            const { liked, likeCount } = likePayload.data;

            miniHeartBtn.classList.toggle("liked", liked === true);
            miniLikeCountEl.textContent = String(likeCount ?? 0);
        });

        // ✅ 플레이리스트 추가(+)
        const addBtn = li.querySelector(".add-pl-btn");
        addBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            // ✅ 고정 hasToken 대신 매번 현재 토큰 체크
            if (!getToken()) {
                showLoginPopup();
                return;
            }

            const sid = addBtn.dataset.songId;
            await openPlaylistModalAndAdd(sid);
        });

        ul.appendChild(li);
    });
}