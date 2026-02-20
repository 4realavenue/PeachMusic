import { authFetch, getToken } from "/js/auth.js";

const hasToken = !!getToken();

/* ✅ 플레이리스트 API */
const PLAYLIST_LIST_API = "/api/playlists";
const PLAYLIST_ADD_SONG_API = (playlistId) => `/api/playlists/${playlistId}/songs`;

/* =========================
   Global Player (전역 재생바)
========================= */
function getGlobalAudioEl() {
    return document.querySelector(".player audio") || document.getElementById("audioPlayer") || null;
}

let currentPlayingSongId = null; // 이 페이지에서 마지막으로 재생 시도한 곡
let currentPlayBtn = null;

function isSameTrackPlaying(globalAudio, audioUrl) {
    if (!globalAudio || !globalAudio.src || !audioUrl) return false;
    const currentFile = String(globalAudio.src).split("/").pop();
    const nextFile = String(audioUrl).split("/").pop();
    return currentFile && nextFile && currentFile === nextFile;
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

function setPlayBtnState(btn, isPlaying) {
    if (!btn) return;
    btn.classList.toggle("playing", isPlaying);
    btn.textContent = isPlaying ? "❚❚" : "▶";
    btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
}

/**
 * ✅ audio 경로 정규화 ("storage/..." -> "/storage/...")
 * - 전역 player-hls에 의존하지 않기 위해 페이지에서 최소한만 처리
 */
function normalizeAudioUrl(audioPath) {
    if (!audioPath) return null;
    const s = String(audioPath);
    if (s.startsWith("http://") || s.startsWith("https://")) return s;
    if (s.startsWith("/")) return s;
    return `/${s}`;
}

/* =========================
   전역 플레이어 이벤트로 버튼 상태 싱크
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

        // 다른 곡이 전역에서 재생되면 현재 버튼 상태 해제
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
   Song audio 가져오기
========================= */
async function getSongAudioUrl(song) {
    // 1) album API songList에 audio가 포함되어 있으면 바로 사용
    if (song.audio) return song.audio;

    // 2) 없으면 단건조회로 audio 가져오기
    const res = await fetch(`/api/songs/${song.songId}`);
    const payload = await res.json();
    if (!payload?.success) return null;
    return payload.data?.audio ?? null;
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
    try { payload = await res.json(); } catch { payload = null; }

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
        const imgHtml = imgPath
            ? `<img src="${imgPath}" alt="">`
            : `<div class="pl-thumb-ph">♪</div>`;

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
        try { payload = await res.json(); } catch { payload = null; }

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


    // 기본 정보
    document.getElementById("albumImage").src = resolveImageUrl(album.albumImage);
    document.getElementById("albumName").textContent = decodeHtmlEntities(album.albumName ?? "-");
    document.getElementById("albumReleaseDate").textContent = formatDate(album.albumReleaseDate);

    const artistNames = (album.artistList ?? [])
        .map(a => decodeHtmlEntities(a.artistName))
        .join(", ");

    document.getElementById("albumArtists").textContent = artistNames || "-";

    // ✅ 앨범 좋아요(상단): 숫자 → 하트
    const heartBtn = document.getElementById("heartBtn");
    const likeCountEl = document.getElementById("likeCount");

    likeCountEl.classList?.add("like-count");

    likeCountEl.textContent = String(album.likeCount ?? 0);
    heartBtn.classList.toggle("liked", album.liked === true);

    heartBtn.addEventListener("click", async (e) => {
        e.stopPropagation();

        if (!hasToken) {
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

    // 수록곡
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
              <button class="mini-heart-btn ${(song.liked ?? song.liked) ? "liked" : ""}"
                      type="button"
                      aria-label="음원 좋아요">❤</button>
            </span>

            <button class="add-pl-btn ${!hasToken ? "disabled" : ""}"
                    type="button"
                    aria-label="플레이리스트에 추가"
                    data-song-id="${song.songId}">＋</button>
          </div>
        `;

        // 트랙 클릭 = 음원 상세 이동 (재생/하트/+ 클릭은 이동 방지)
        li.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".mini-heart-btn")) return;
            if (e.target.closest(".add-pl-btn")) return;
            location.href = `/songs/${song.songId}/page`;
        });

        // ✅ 재생 버튼: 전역 플레이어로 재생
        const playBtn = li.querySelector(".track-play");
        playBtn.addEventListener("click", async (e) => {
            e.stopPropagation();

            const rawAudio = await getSongAudioUrl(song);
            const audioUrl = normalizeAudioUrl(rawAudio);

            if (!audioUrl) {
                alert("재생 가능한 음원 주소가 없습니다.");
                return;
            }

            playBtn.dataset.audioUrl = audioUrl;

            if (currentPlayBtn && currentPlayBtn !== playBtn) {
                setPlayBtnState(currentPlayBtn, false);
            }

            currentPlayingSongId = song.songId;
            currentPlayBtn = playBtn;

            if (typeof window.playSongFromPage !== "function") {
                alert("전역 플레이어가 아직 로드되지 않았습니다.");
                return;
            }

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

            if (!hasToken) {
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

            if (!hasToken) {
                showLoginPopup();
                return;
            }

            const sid = addBtn.dataset.songId;
            await openPlaylistModalAndAdd(sid);
        });

        ul.appendChild(li);
    });
}