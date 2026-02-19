import { authFetch, getToken } from "/js/auth.js";

const recommendGrid = document.getElementById("recommendGrid");
const rankingList = document.getElementById("rankingList");
const audio = document.getElementById("previewAudio");

const token = getToken();

let currentSongId = null; // 현재 재생 중인 곡 id

/* =========================
   추천 카드 렌더
========================= */

function createRecommendCard(song) {
    const div = document.createElement("div");
    div.className = "music-card";

    const img = song.albumImage ?? null;

    div.innerHTML = `
      <div class="card-img">
          ${img ? `<img src="${img}" alt="album">` : ``}
      </div>
      <div class="card-title"></div>
      <div class="card-sub"></div>
  `;

    div.querySelector(".card-title").textContent = decodeEntities(song.songName ?? "-");
    div.querySelector(".card-sub").textContent = decodeEntities(song.artistName ?? "-");

    div.addEventListener("click", () => {
        location.href = `/songs/${song.songId}/page`;
    });

    return div;
}

/* =========================
   인기 차트 행 렌더 (✅ 곡 제목 + 재생버튼)
========================= */

function createRankRow(song, rank) {
    const div = document.createElement("div");
    div.className = "rank-item";
    div.dataset.songId = String(song.songId ?? song.id);

    const songId = song.songId ?? song.id;
    const title = decodeEntities(song.songName ?? song.title ?? "-");

    // ✅ albumImage 보강된 값 우선
    const albumImage =
        song.albumImage ??
        song.image ??
        song.imageUrl ??
        song.albumCover ??
        null;

    const thumbHtml = albumImage
        ? `<div class="rank-thumb"><img src="${albumImage}" alt="album"></div>`
        : `<div class="rank-thumb placeholder" aria-label="no album image"></div>`;

    div.innerHTML = `
      <div class="rank-number">${rank}</div>
      ${thumbHtml}
      <div>
          <div class="rank-title"></div>
      </div>
      <button class="rank-play" type="button" aria-label="재생">▶</button>
  `;

    div.querySelector(".rank-title").textContent = title;

    // ✅ 행 클릭 = 상세 페이지 이동
    div.addEventListener("click", (e) => {
        if (e.target.closest(".rank-play")) return; // 재생 버튼 클릭은 이동 막기
        location.href = `/songs/${songId}/page`;
    });

    // ✅ 재생 버튼 클릭 = 재생/일시정지
    const playBtn = div.querySelector(".rank-play");
    playBtn.addEventListener("click", async (e) => {
        e.stopPropagation();
        await togglePlay(songId);
    });

    return div;
}

/* =========================
   ✅ 재생 토글 (곡별로 play API 호출)
========================= */

async function togglePlay(songId) {
    // 같은 곡이면 pause/play 토글
    if (currentSongId === songId) {
        if (audio.paused) await audio.play();
        else audio.pause();
        syncPlayButtons();
        return;
    }

    // 다른 곡이면 play API로 streamingUrl 받아서 재생
    try {
        const res = await fetch(`/api/songs/${songId}/play`);
        const json = await res.json();

        const url = json?.data?.streamingUrl;
        if (!res.ok || json?.success === false || !url) {
            alert(json?.message || "재생 가능한 음원이 없습니다.");
            return;
        }

        currentSongId = songId;
        audio.src = url;

        await audio.play();
        syncPlayButtons();
    } catch (e) {
        console.error("재생 실패:", e);
        alert("재생에 실패했습니다.");
    }
}

function syncPlayButtons() {
    document.querySelectorAll(".rank-item").forEach((row) => {
        const songId = Number(row.dataset.songId);
        const btn = row.querySelector(".rank-play");

        const isCurrent = currentSongId === songId;
        const isPlaying = isCurrent && !audio.paused;

        btn.classList.toggle("playing", isPlaying);
        btn.textContent = isPlaying ? "❚❚" : "▶";
        btn.setAttribute("aria-label", isPlaying ? "일시정지" : "재생");
    });
}

/* audio 이벤트 */
audio.addEventListener("ended", () => {
    currentSongId = null;
    syncPlayButtons();
});
audio.addEventListener("pause", () => syncPlayButtons());
audio.addEventListener("play", () => syncPlayButtons());

/* =========================
   추천 로드
========================= */

async function loadRecommend() {
    if (!token) return;

    try {
        const res = await authFetch("/api/songs/recommendation");
        if (!res) return;

        const json = await res.json();
        if (!json.success) return;

        const list = json.data || [];
        recommendGrid.innerHTML = "";

        list.slice(0, 5).forEach((song) => {
            recommendGrid.appendChild(createRecommendCard(song));
        });
    } catch (e) {
        console.error("추천 로딩 실패:", e);
    }
}

/* =========================
   ✅ Top100 + (5개만) 단건조회로 이미지 보강
========================= */

async function loadRanking() {
    try {
        const res = await fetch("/api/songs/ranking/Top100");
        const json = await res.json();

        if (!res.ok || !json.success) return;

        const top5 = (json.data || []).slice(0, 5);

        const enriched = await Promise.all(
            top5.map(async (s) => {
                const songId = s.songId ?? s.id;

                const hasImage = !!(s.albumImage ?? s.image ?? s.imageUrl ?? s.albumCover);
                if (hasImage) return s;

                const detail = await fetchSongDetail(songId);
                if (!detail) return s;

                return { ...s, ...detail };
            })
        );

        rankingList.innerHTML = "";
        enriched.forEach((song, idx) => {
            rankingList.appendChild(createRankRow(song, idx + 1));
        });

        syncPlayButtons();
    } catch (e) {
        console.error("차트 로딩 실패:", e);
    }
}

async function fetchSongDetail(songId) {
    try {
        const res = getToken()
            ? await authFetch(`/api/songs/${songId}`, { method: "GET" })
            : await fetch(`/api/songs/${songId}`);

        if (!res) return null;

        const json = await res.json();
        if (!json.success) return null;

        return json.data;
    } catch (e) {
        console.error("곡 단건조회 실패:", e);
        return null;
    }
}

/* ========================= */

document.addEventListener("DOMContentLoaded", () => {
    loadRecommend();
    loadRanking();
});

/* =========================
   HTML Entity 디코딩 (&quot; 등)
========================= */
function decodeEntities(str) {
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}
