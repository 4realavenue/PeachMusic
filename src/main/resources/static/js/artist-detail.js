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

/* 아티스트 좋아요(회색하트) */
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

    songsWrap.innerHTML = songs.map(s => `
    <div class="song-item" data-song-id="${s.songId}">
      <div class="song-left">
        <div class="song-title">${escapeHtml(s.name)}</div>
        <div class="song-meta">
          ❤️ ${s.likeCount ?? 0}
          · ${formatDate(s.albumReleaseDate)}
          ${s.jobStatus ? ` · ${escapeHtml(String(s.jobStatus))}` : ""}
        </div>
      </div>
      <button class="song-play" type="button">▶</button>
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

    // 더보기 링크(페이지 이동)
    if (moreAlbumsLink) moreAlbumsLink.href = `/artists/${artistId}/albums/page`;
    if (moreSongsLink) moreSongsLink.href = `/artists/${artistId}/songs/page`;

    // 아티스트 좋아요(회색하트)
    bindArtistLike(artistId);

    try {
        const res = await fetchPreview(artistId);
        if (!res || !res.ok) throw new Error(`preview failed: ${res?.status}`);

        const payload = await res.json();
        const dto = payload.data;

        renderPreview(dto, albumsWrap, songsWrap);
        bindAlbumClick(albumsWrap);

    } catch (e) {
        console.error(e);
        albumsWrap.innerHTML = `<div style="padding:12px;color:#666;">미리보기를 불러오지 못했어요.</div>`;
        songsWrap.innerHTML = "";
    }
});
