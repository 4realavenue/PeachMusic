import { authFetch, getToken } from "/js/auth.js";
import { resolveAudioUrl } from "/js/player-hls.js";

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
    return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, "0")}.${String(
        d.getDate()
    ).padStart(2, "0")}`;
}

/* ✅ 이미지 경로 안전 처리(외부/내부 둘 다) */
function resolveImageUrl(path) {
    if (!path) return "";
    const s = String(path);
    if (s.startsWith("http://") || s.startsWith("https://")) return s;
    if (s.startsWith("/")) return s;
    return `/${s}`;
}

/* ✅ 아티스트 상단 아바타: 원형 + 첫 글자 */
function setArtistAvatar(avatarEl, imagePath, artistName) {
    if (!avatarEl) return;

    const name = String(artistName ?? "").trim();
    const firstChar = name ? name.charAt(0).toUpperCase() : "?";
    const url = resolveImageUrl(imagePath);

    // 초기화
    avatarEl.classList.remove("has-image");
    avatarEl.style.backgroundImage = "";
    avatarEl.textContent = firstChar;

    // 이미지가 있으면 background-image로 덮기
    if (url) {
        avatarEl.style.backgroundImage = `url('${url}')`;
        avatarEl.classList.add("has-image");
        avatarEl.textContent = firstChar; // 색은 투명 처리되니 값 유지해도 OK
    }
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

            const payload = await res.json().catch(() => null);
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

/* ✅ Context Queue (미리보기 5곡) */
function buildPreviewTracks(dto) {
    const songs = (dto?.songList ?? []).slice(0, 5);
    return songs
        .map((s) => ({
            songId: Number(s?.songId),
            title: String(s?.name ?? "Unknown"),
        }))
        .filter((t) => Number.isFinite(t.songId));
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

      <div class="album-meta-row">
        <span class="album-date">${formatDate(a.albumReleaseDate)}</span>

        <span class="like-group">
          <span class="mini-like-count">${a.likeCount ?? 0}</span>
          <button class="mini-heart-btn ${a.liked ? "liked" : ""} ${!getToken() ? "disabled" : ""}"
                  type="button"
                  aria-label="앨범 좋아요">❤</button>
        </span>
      </div>
    </div>
  `
        )
        .join("");

    songsWrap.innerHTML = songs
        .map((s) => {
            const albumName = escapeHtml(s.albumName ?? "");
            const date = formatDate(s.albumReleaseDate);
            const meta = `${albumName}${albumName ? " · " : ""}${date}`;

            return `
    <div class="song-item" data-song-id="${s.songId}">
      <div class="song-left">
        <div class="song-title">${escapeHtml(s.name)}</div>
        <div class="song-meta">${meta}</div>
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
  `;
        })
        .join("");
}

function bindAlbumInteractions(albumsWrap) {
    albumsWrap.querySelectorAll(".album-item").forEach((row) => {
        const albumId = row.dataset.albumId;
        const heartBtn = row.querySelector(".mini-heart-btn");
        const likeCountEl = row.querySelector(".mini-like-count");

        row.addEventListener("click", (e) => {
            if (e.target.closest(".mini-heart-btn")) return;
            if (albumId) window.location.href = `/albums/${albumId}/page`;
        });

        heartBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (!getToken()) {
                showLoginPopup();
                return;
            }

            try {
                const res = await authFetch(`/api/albums/${albumId}/likes`, { method: "POST" });
                if (!res) return;

                const payload = await res.json().catch(() => null);
                if (!payload?.success) return;

                const { liked, likeCount } = payload.data;
                heartBtn.classList.toggle("liked", liked === true);
                likeCountEl.textContent = String(likeCount ?? 0);
            } catch (err) {
                console.error(err);
            }
        });
    });
}

function bindSongInteractions(songsWrap, tracks, artistId) {
    songsWrap.querySelectorAll(".song-item").forEach((row) => {
        const songId = row.dataset.songId;
        const playBtn = row.querySelector(".track-play");
        const heartBtn = row.querySelector(".mini-heart-btn");
        const likeCountEl = row.querySelector(".mini-like-count");

        row.addEventListener("click", (e) => {
            if (e.target.closest(".track-play")) return;
            if (e.target.closest(".mini-heart-btn")) return;
            if (songId) window.location.href = `/songs/${songId}/page`;
        });

        playBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (typeof window.setPlayerQueue !== "function" || typeof window.playSongFromPage !== "function") {
                alert("전역 플레이어가 아직 로드되지 않았습니다.");
                return;
            }

            const sid = Number(songId);
            if (!sid) return;

            window.setPlayerQueue(tracks, sid, { loop: true, contextKey: `artist-preview:${artistId}` });

            const url = await fetchPlayUrl(sid);
            if (!url) return;

            try {
                const title = row.querySelector(".song-title")?.textContent?.trim() || "Unknown";
                await window.playSongFromPage(url, title, sid);
            } catch (err) {
                console.error(err);
                alert("재생에 실패했습니다.");
            }
        });

        heartBtn?.addEventListener("click", async (e) => {
            e.stopPropagation();

            if (!getToken()) {
                showLoginPopup();
                return;
            }

            try {
                const res = await authFetch(`/api/songs/${songId}/likes`, { method: "POST" });
                if (!res) return;

                const payload = await res.json().catch(() => null);
                if (!payload?.success) return;

                const { liked, likeCount } = payload.data;
                heartBtn.classList.toggle("liked", liked === true);
                likeCountEl.textContent = String(likeCount ?? 0);
            } catch (err) {
                console.error(err);
            }
        });
    });
}

document.addEventListener("DOMContentLoaded", async () => {
    const meta = document.getElementById("artistMeta");
    const artistId = meta?.dataset?.artistId;
    const artistName = meta?.dataset?.artistName || "아티스트";

    // ✅ 상단 원형 아바타 요소(HTML에 클래스만 있으면 됨)
    const avatarEl = document.querySelector(".artist-hero-avatar");

    // 1) 먼저 “이름 첫 글자”로 기본 표시 (이미지 못 받아도 UI는 정상)
    //    meta에 profileImage가 있으면 이 단계에서 바로 이미지도 적용됨
    const metaImage = meta?.dataset?.profileImage || meta?.dataset?.artistImage || meta?.dataset?.image || "";
    setArtistAvatar(avatarEl, metaImage, artistName);

    const previewArtistName = document.getElementById("previewArtistName");
    const albumsWrap = document.getElementById("albumsPreview");
    const songsWrap = document.getElementById("songsPreview");

    const moreAlbumsLink = document.getElementById("moreAlbumsLink");
    const moreSongsLink = document.getElementById("moreSongsLink");

    if (previewArtistName) previewArtistName.textContent = artistName;
    if (!artistId) return;

    if (moreAlbumsLink) moreAlbumsLink.href = `/artists/${artistId}/albums/page`;
    if (moreSongsLink) moreSongsLink.href = `/artists/${artistId}/songs/page`;

    // ✅ detail에서 liked/likeCount + profileImage(가능하면)까지 반영
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

                likeBtn?.classList.toggle("liked", liked === true);
                if (likeCountEl) likeCountEl.textContent = String(likeCount ?? 0);

                // ✅ 프로필 이미지 키 후보들(백엔드 DTO 이름이 뭐든 대응)
                const profileImage =
                    detailPayload.data?.profileImage ||
                    detailPayload.data?.artistImage ||
                    detailPayload.data?.image ||
                    detailPayload.data?.imageUrl ||
                    "";

                // detail에서 이미지가 오면 여기서 최종 덮어쓰기
                setArtistAvatar(avatarEl, profileImage, artistName);
            }
        }
    } catch (e) {
        console.error("artist detail load failed", e);
    }

    bindArtistLike(artistId);

    try {
        const res = await fetchPreview(artistId);
        if (!res || !res.ok) throw new Error(`preview failed: ${res?.status}`);

        const payload = await res.json();
        const dto = payload.data;

        renderPreview(dto, albumsWrap, songsWrap);

        const tracks = buildPreviewTracks(dto);
        bindSongInteractions(songsWrap, tracks, artistId);
        bindAlbumInteractions(albumsWrap);
    } catch (e) {
        console.error(e);
        albumsWrap.innerHTML = `<div style="padding:12px;color:#666;">미리보기를 불러오지 못했어요.</div>`;
        songsWrap.innerHTML = "";
    }
});