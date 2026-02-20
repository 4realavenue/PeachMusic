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

/* ✅ play API: 재생수 증가 + streamingUrl 반환 */
async function fetchPlayUrl(songId) {
    const res = await authFetch(`/api/songs/${songId}/play`, { method: "GET" });
    if (!res) return null;

    let payload = null;
    try {
        payload = await res.json();
    } catch {
        payload = null;
    }

    if (!res.ok || payload?.success === false) {
        alert(payload?.message || "재생에 실패했습니다.");
        return null;
    }

    return payload?.data?.streamingUrl ?? null;
}

/* 미리보기 렌더링 */
function renderPreview(dto, albumsWrap, songsWrap) {
    const albums = (dto?.albumList ?? []).slice(0, 5);
    const songs = (dto?.songList ?? []).slice(0, 5);

    albumsWrap.innerHTML = albums
        .map(
            (a) => `
    <div class="album-item" data-album-id="${a.albumId}">
      <div class="preview-album-cover"
           style="background-image:url('${a.albumImage ?? ""}');
                  background-size:cover;background-position:center;">
      </div>
      <div class="album-title">${escapeHtml(a.albumName)}</div>
      <div class="album-meta">${formatDate(a.albumReleaseDate)} · ${a.likeCount ?? 0} 💗</div>
      ${a.isLiked ? `<div class="album-liked-badge">✔ 내가 좋아요</div>` : ""}
    </div>
  `
        )
        .join("");

    songsWrap.innerHTML = songs
        .map(
            (s) => `
    <div class="song-item" data-song-id="${s.songId}">
      <div class="song-left">
        <div class="song-title">${escapeHtml(s.name)}</div>
        <div class="song-meta">
          · ${formatDate(s.albumReleaseDate)}
          ${s.jobStatus ? ` · ${escapeHtml(String(s.jobStatus))}` : ""}
        </div>
      </div>

      <div class="song-right">
        <button class="track-play" type="button" aria-label="재생">▶</button>

        <span class="like-group">
          <span class="mini-like-count">${s.likeCount ?? 0}</span>
          <button class="mini-heart-btn ${s.liked ? "liked" : ""} ${!getToken() ? "disabled" : ""}"
                  type="button"
                  aria-label="음원 좋아요">❤</button>
        </span>
      </div>
    </div>
  `
        )
        .join("");
}

function bindAlbumClick(albumsWrap) {
    albumsWrap.querySelectorAll(".album-item").forEach((el) => {
        el.addEventListener("click", () => {
            const albumId = el.dataset.albumId;
            if (albumId) window.location.href = `/albums/${albumId}/page`;
        });
    });
}

/* ✅ 음원 미리보기: 상세 이동 + 전역 재생 + 리스트 하트 */
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

        // ✅ 재생 버튼: 전역 플레이어 사용
        playBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (typeof window.playSongFromPage !== "function") {
                alert("전역 플레이어가 아직 로드되지 않았습니다.");
                return;
            }

            const url = await fetchPlayUrl(songId);
            if (!url) return;

            try {
                const title = row.querySelector(".song-title")?.textContent?.trim() || "Unknown";
                await window.playSongFromPage(url, title, Number(songId));
            } catch (err) {
                console.error(err);
                alert("재생에 실패했습니다.");
            }
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

    try {
        const detailRes = getToken()
            ? await authFetch(`/api/artists/${artistId}`, { method: "GET" })
            : await fetch(`/api/artists/${artistId}`);

        if (detailRes && detailRes.ok) {
            const detailPayload = await detailRes.json();
            if (detailPayload?.success) {
                const { liked, likeCount } = detailPayload.data;

                const likeBtn = document.getElementById("artistLikeBtn");
                const likeCountEl = document.getElementById("artistLikeCount");

                // ✅ 새로고침 시 liked 상태 반영
                likeBtn?.classList.toggle("liked", liked === true);

                // ✅ 좋아요 숫자 반영
                if (likeCountEl) {
                    likeCountEl.textContent = String(likeCount ?? 0);
                }
            }
        }
    } catch (e) {
        console.error("artist detail load failed", e);
    }

    /* ========================= */

    bindArtistLike(artistId);

    try {
        const res = await fetchPreview(artistId);
        if (!res || !res.ok) throw new Error(`preview failed: ${res?.status}`);

        const payload = await res.json();
        const dto = payload.data;

        renderPreview(dto, albumsWrap, songsWrap);
        bindAlbumClick(albumsWrap);
        bindSongInteractions(songsWrap);
    } catch (e) {
        console.error(e);
        albumsWrap.innerHTML = `<div style="padding:12px;color:#666;">미리보기를 불러오지 못했어요.</div>`;
        songsWrap.innerHTML = "";
    }
});