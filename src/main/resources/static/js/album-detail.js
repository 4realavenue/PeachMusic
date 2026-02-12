import { authFetch, getToken } from "/js/auth.js";

const hasToken = !!getToken();

function resolveImageUrl(imagePath) {
    if (!imagePath) return "/images/default.png";
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
    if (imagePath.startsWith("/")) return imagePath;
    return `/${imagePath}`;
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
    // LocalDate면 "2025-01-21" 형태로 올 가능성 높음
    return String(iso).replaceAll("-", ".");
}

document.addEventListener("DOMContentLoaded", loadAlbum);

async function loadAlbum() {
    if (!albumId) return;

    const res = await fetch(`/api/albums/${albumId}`);
    const payload = await res.json();

    if (!payload?.success) return;

    const album = payload.data;

    // 기본 정보
    document.getElementById("albumImage").src = resolveImageUrl(album.albumImage);
    document.getElementById("albumName").textContent = album.albumName ?? "-";
    document.getElementById("albumReleaseDate").textContent = formatDate(album.albumReleaseDate);

    const artistNames = (album.artistList ?? []).map(a => a.artistName).join(", ");
    document.getElementById("albumArtists").textContent = artistNames || "-";

    // 좋아요
    const heartBtn = document.getElementById("heartBtn");
    const likeCountEl = document.getElementById("likeCount");

    likeCountEl.textContent = String(album.likeCount ?? 0);
    heartBtn.classList.toggle("liked", album.isLiked === true);

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

    (album.songList ?? []).forEach(song => {
        const li = document.createElement("li");
        li.className = "track";

        li.innerHTML = `
      <div class="track-no">${song.position ?? "-"}</div>
      <div class="track-name" title="${song.name ?? ""}">${song.name ?? "-"}</div>
      <div class="track-duration">${formatDuration(song.duration)}</div>
      <div class="track-like"><span class="mini-heart">❤</span>${song.likeCount ?? 0}</div>
    `;

        ul.appendChild(li);
    });
}
