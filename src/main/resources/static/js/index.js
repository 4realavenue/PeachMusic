import { authFetch, getToken } from "/js/auth.js";

const recommendGrid = document.getElementById("recommendGrid");
const rankingList = document.getElementById("rankingList");

const token = getToken();

/* =========================
   추천 카드 렌더
========================= */

function createRecommendCard(song) {
    const div = document.createElement("div");
    div.className = "music-card";

    div.innerHTML = `
        <div class="card-img">
            <img src="${song.albumImage || '/images/default.png'}">
        </div>
        <div class="card-title">${song.songName}</div>
        <div class="card-sub">${song.artistName}</div>
    `;

    div.addEventListener("click", () => {
        location.href = `/songs/${song.songId}/page`;
    });

    return div;
}

/* =========================
   차트 행 렌더
========================= */

function createRankRow(song, rank) {
    const div = document.createElement("div");
    div.className = "rank-item";

    div.innerHTML = `
        <div class="rank-number">${rank}</div>
        <div class="rank-thumb">
            <img src="/images/default.png">
        </div>
        <div>
            <div class="rank-title">${song.title}</div>
            <div class="rank-artist">-</div>
        </div>
    `;

    div.addEventListener("click", () => {
        location.href = `/songs/${song.id}/page`;
    });

    return div;
}

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

        list.slice(0, 5).forEach(song => {
            recommendGrid.appendChild(createRecommendCard(song));
        });

    } catch (e) {
        console.error("추천 로딩 실패:", e);
    }
}

/* =========================
   차트 로드
========================= */

async function loadRanking() {
    try {
        const res = await fetch("/api/songs/ranking/Top100");
        const json = await res.json();

        if (!res.ok || !json.success) return;

        const list = json.data || [];

        list.slice(0, 5).forEach((song, index) => {
            rankingList.appendChild(
                createRankRow(song, index + 1)
            );
        });

    } catch (e) {
        console.error("차트 로딩 실패:", e);
    }
}

/* ========================= */

document.addEventListener("DOMContentLoaded", () => {
    loadRecommend();
    loadRanking();
});
