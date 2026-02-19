import { authFetch, getToken } from "/js/auth.js";

/* 로그인 팝업 */
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

/* 유틸 */
function escapeHtml(str) {
    return String(str ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function formatDate(dateStr) {
    if (!dateStr) return "";
    const d = new Date(dateStr);
    if (isNaN(d)) return String(dateStr);
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(d.getDate()).padStart(2, "0")}`;
}

/* preview API */
async function fetchPreview(artistId) {
    const url = `/api/artists/${artistId}/preview`;
    if (getToken()) return authFetch(url, { method: "GET" });
    return fetch(url, { method: "GET" });
}

/* 아티스트 좋아요 */
function bindArtistLike(artistId) {
    const likeBtn = document.getElementById("artistLikeBtn");
    const likeCountEl = document.getElementById("artistLikeCount");
    if (!likeBtn || !likeCountEl) return;

    likeBtn.addEventListener("click", async () => {
        if (!getToken()) {
            showLoginPopup();
            return;
        }

        try {
            const res = await authFetch(`/api/artists/${artistId}/likes`, { method: "POST" });
            if (!res) return;

            const payload = await res.json();
            if (!payload?.success) return;

            const { liked, likeCount } = payload.data;
            likeBtn.classList.toggle("liked", !!liked);
            likeCountEl.textContent = String(likeCount ?? 0);

        } catch (e) {
            console.error(e);
        }
    });
}

/* ✅ 페이지 내 프리뷰 재생(한 곡만) */
const previewAudio = new Audio();
previewAudio.preload = "metadata";

let currentPlayingSongId = null;
let currentPlayBtn = null;

function setPlayBtnState(btn, isPlaying) {
    if (!btn) return;
    btn.classList.toggle("playing", isPlaying);
    btn.textContent = isPlaying ? "❚❚" : "▶";
}

previewAudio.addEventListener("ended", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);
    currentPlayingSongId = null;
    currentPlayBtn = null;
});

previewAudio.addEventListener("pause", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);
});

previewAudio.addEventListener("play", () => {
    if (currentPlayBtn) setPlayBtnState(currentPlayBtn, true);
});

async function getSongAudioUrl(songId) {
    const res = await authFetch(`/api/songs/${songId}/play`, { method: "GET" });
    if (!res) return null;
    const payload = await res.json();
    if (!res.ok || payload?.success === false) return null;
    return payload.data?.streamingUrl ?? null;
}

/* 미리보기 렌더링 */
function renderPreview(dto, albumsWrap, songsWrap) {
    const albums = (dto?.albumList ?? []).slice(0, 5);
    const songs = (dto?.songList ?? []).slice(0, 5);

    albumsWrap.innerHTML = albums.map(a => `
    <div class="album-item" data-album-id="${a.albumId}">
      <div class="preview-album-cover"
           style="background-image:url('${a.albumImage ?? ""}');
                  background-size:cover;background-position:center;">
      </div>
      <div class="album-title">${escapeHtml(a.albumName)}</div>
      <div class="album-meta">${formatDate(a.albumReleaseDate)} · ❤️ ${a.likeCount ?? 0}</div>
      ${a.isLiked ? `<div class="album-liked-badge">✔ 내가 좋아요</div>` : ""}
    </div>
  `).join("");

    // ✅ song-item: 클릭=상세 이동 / 버튼=재생/좋아요
    songsWrap.innerHTML = songs.map(s => `
    <div class="song-item" data-song-id="${s.songId}">
      <div class="song-left">
        <div class="song-title">${escapeHtml(s.name)}</div>
        <div class="song-meta">
          · ${formatDate(s.albumReleaseDate)}
          ${s.jobStatus ? ` · ${escapeHtml(String(s.jobStatus))}` : ""}
        </div>
      </div>

      <div class="song-right">
        <button class="mini-heart-btn ${s.liked ? "liked" : ""} ${!getToken() ? "disabled" : ""}"
                type="button"
                aria-label="음원 좋아요">❤</button>
        <span class="mini-like-count">${s.likeCount ?? 0}</span>

        <button class="track-play" type="button" aria-label="재생">▶</button>
      </div>
    </div>
  `).join("");
}

function bindAlbumClick(albumsWrap) {
    albumsWrap.querySelectorAll(".album-item").forEach((el) => {
        el.addEventListener("click", () => {
            const albumId = el.dataset.albumId;
            if (albumId) window.location.href = `/albums/${albumId}/page`;
        });
    });
}

/* ✅ 음원 미리보기: 상세 이동 + 재생 + 리스트 하트 */
function bindSongInteractions(songsWrap) {
    songsWrap.querySelectorAll(".song-item").forEach((row) => {
        const songId = row.dataset.songId;
        const playBtn = row.querySelector(".track-play");
        const heartBtn = row.querySelector(".mini-heart-btn");
        const likeCountEl = row.querySelector(".mini-like-count");

        // row 클릭 = 상세 이동 (버튼 클릭은 제외)
        row.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".mini-heart-btn")) return;
            if (songId) window.location.href = `/songs/${songId}/page`;
        });

        // 재생 버튼
        playBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            const audioUrl = await getSongAudioUrl(songId);
            if (!audioUrl) {
                alert("재생 가능한 음원 주소가 없습니다.");
                return;
            }

            // 같은 곡이면 토글
            if (currentPlayingSongId === songId) {
                if (previewAudio.paused) await previewAudio.play();
                else previewAudio.pause();
                return;
            }

            // 다른 곡 재생: 이전 버튼 원복
            if (currentPlayBtn) setPlayBtnState(currentPlayBtn, false);

            currentPlayingSongId = songId;
            currentPlayBtn = playBtn;

            previewAudio.src = audioUrl;
            await previewAudio.play();
        });

        // 리스트 하트
        heartBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (!getToken()) {
                showLoginPopup();
                return;
            }

            const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
            if (!res) return;

            const payload = await res.json();
            if (!payload?.success) return;

            const { liked, likeCount } = payload.data;
            heartBtn.classList.toggle("liked", liked === true);
            likeCountEl.textContent = String(likeCount ?? 0);
        });
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    const meta = document.getElementById("artistMeta");
    const artistId = meta?.dataset?.artistId;
    const artistName = meta?.dataset?.artistName || "아티스트";

    const previewArtistName = document.getElementById("previewArtistName");
    const albumsWrap = document.getElementById("albumsPreview");
    const songsWrap = document.getElementById("songsPreview");

    const moreAlbumsLink = document.getElementById("moreAlbumsLink");
    const moreSongsLink = document.getElementById("moreSongsLink");

    if (previewArtistName) previewArtistName.textContent = artistName;
    if (!artistId) return;

    if (moreAlbumsLink) moreAlbumsLink.href = `/artists/${artistId}/albums/page`;
    if (moreSongsLink) moreSongsLink.href = `/artists/${artistId}/songs/page`;

    bindArtistLike(artistId);

    try {
        const res = await fetchPreview(artistId);
        if (!res || !res.ok) throw new Error(`preview failed: ${res?.status}`);

        const payload = await res.json();
        const dto = payload.data;

        renderPreview(dto, albumsWrap, songsWrap);
        bindAlbumClick(albumsWrap);
        bindSongInteractions(songsWrap); // ✅ 추가

    } catch (e) {
        console.error(e);
        albumsWrap.innerHTML = `<div style="padding:12px;color:#666;">미리보기를 불러오지 못했어요.</div>`;
        songsWrap.innerHTML = "";
    }
});