import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/songs";

/* =========================
   Elements
========================= */
const els = {
    word: document.getElementById("wordInput"),
    searchBtn: document.getElementById("searchBtn"),
    listBody: document.getElementById("listBody"),
    moreBtn: document.getElementById("moreBtn"),
    emptyBox: document.getElementById("emptyBox"),

    // ✅ 추가
    sentinel: document.getElementById("sentinel"),
    toTopBtn: document.getElementById("toTopBtn"),
};

const retryDownloadBtn = document.getElementById("retryDownloadBtn");
const retryTranscodeBtn = document.getElementById("retryTranscodeBtn");
const selectAll = document.getElementById("selectAll");

/* =========================
   State
========================= */
let selected = new Set();

let state = {
    word: "",
    lastId: null,
    loading: false,
    hasNext: true,
    observer: null, // ✅ 무한 스크롤 observer
};

/* =========================
   Utils
========================= */
function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function safeNumber(v, fallback = 0) {
    const n = Number(v);
    return Number.isFinite(n) ? n : fallback;
}

function formatDate(v) {
    if (!v) return "-";
    return String(v);
}

async function safeJson(res) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}

function setEmpty(isEmpty) {
    els.emptyBox?.classList.toggle("hidden", !isEmpty);
}

function setMoreVisible(visible) {
    // ✅ 더보기 버튼은 구조만 남기고 실제 사용은 안 함 (hidden 유지)
    // 필요하면 visible에 따라 토글 가능하지만, 여기선 무한스크롤이라 항상 숨김 유지
    els.moreBtn?.classList.add("hidden");
}

/* =========================
   Normalize
========================= */
function normalizeSong(s) {
    return {
        songId: s.songId,
        name: s.name ?? "-",
        artistName: s.artistName ?? "-",
        releaseDate: s.releaseDate ?? null,
        albumImage: s.albumImage ?? null,
        likeCount: safeNumber(s.likeCount, 0),
        playCount: safeNumber(s.playCount, 0),
        isDeleted: (s.deleted ?? s.isDeleted) === true,
        progressingStatus: s.progressingStatus ?? null,
    };
}

/* =========================
   Render
========================= */
function renderRows(items, append = true) {
    if (!append) els.listBody.innerHTML = "";

    const html = items.map(raw => {
        const s = normalizeSong(raw);

        const badge = s.isDeleted
            ? `<span class="badge deleted">비활성</span>`
            : `<span class="badge">활성</span>`;

        const statusBadge = s.progressingStatus
            ? `<span class="status-badge">${escapeHtml(s.progressingStatus)}</span>`
            : "";

        const nameHtml = s.isDeleted
            ? `<span class="song-link disabled" data-disabled="true">${escapeHtml(s.name)}</span>`
            : `<a class="song-link" href="/songs/${s.songId}/page">${escapeHtml(s.name)}</a>`;

        const albumImageHtml = s.albumImage
            ? `<img class="album-cover" src="${escapeHtml(s.albumImage)}" onerror="this.onerror=null;this.style.display='none';"/>`
            : `<div class="album-cover-fallback">🎵</div>`;

        return `
        <div class="row">
            <div class="col check">
                <input type="checkbox" class="row-checkbox" value="${s.songId}">
            </div>

            <div class="col id">${s.songId}</div>

            <div class="col name">${nameHtml}</div>

            <div class="col album">
                <div class="album-stack">
                    ${albumImageHtml}
                    ${badge}
                </div>
            </div>

            <div class="col meta">
                <div>${escapeHtml(s.artistName)} · ${escapeHtml(formatDate(s.releaseDate))}</div>
                <div>♥ ${s.likeCount} · ▶ ${s.playCount} ${statusBadge}</div>
            </div>

            <div class="col manage">
                <button class="btn" data-action="edit" data-id="${s.songId}">수정</button>
                ${
            s.isDeleted
                ? `<button class="btn primary" data-action="restore" data-id="${s.songId}">복구</button>`
                : `<button class="btn danger" data-action="delete" data-id="${s.songId}">삭제</button>`
        }
            </div>
        </div>
        `;
    }).join("");

    els.listBody.insertAdjacentHTML("beforeend", html);
}

/* =========================
   Fetch List
========================= */
async function fetchList({ reset = false } = {}) {
    if (state.loading) return;
    if (!state.hasNext && !reset) return;

    state.loading = true;

    if (reset) {
        state.lastId = null;
        state.hasNext = true;
        els.listBody.innerHTML = "";
        setEmpty(false);

        selected.clear();
        retryDownloadBtn?.setAttribute("disabled", true);
        retryTranscodeBtn?.setAttribute("disabled", true);
        if (selectAll) selectAll.checked = false;
    }

    try {
        const params = new URLSearchParams();
        if (state.word) params.set("word", state.word);
        if (state.lastId) params.set("lastId", state.lastId);

        const res = await authFetch(`${API_URL}?${params}`, { method: "GET" });
        if (!res) return;

        const json = await safeJson(res);
        const data = json?.data || {};
        const content = data.content || [];

        if (content.length === 0 && els.listBody.children.length === 0) {
            setEmpty(true);
            state.hasNext = false;
            setMoreVisible(false);
            return;
        }

        renderRows(content, true);

        state.hasNext = data.hasNext ?? false;
        state.lastId = data.nextCursor?.lastId ?? content.at(-1)?.songId ?? null;

        setMoreVisible(state.hasNext);

    } catch (e) {
        console.error(e);
        alert("목록 조회 중 오류");
    } finally {
        state.loading = false;
    }
}

/* =========================
   ✅ Infinite Scroll
========================= */
function setupInfiniteScroll() {
    if (!els.sentinel) return;

    if (state.observer) {
        try { state.observer.disconnect(); } catch (_) {}
        state.observer = null;
    }

    state.observer = new IntersectionObserver(
        (entries) => {
            const entry = entries[0];
            if (!entry?.isIntersecting) return;
            if (state.hasNext && !state.loading) fetchList({ reset: false });
        },
        { root: null, rootMargin: "300px", threshold: 0 }
    );

    state.observer.observe(els.sentinel);
}

/* =========================
   Selection
========================= */
function syncSelectAll() {
    const boxes = document.querySelectorAll(".row-checkbox");
    if (!selectAll) return;
    selectAll.checked = boxes.length > 0 &&
        Array.from(boxes).every(cb => cb.checked);
}

/* =========================
   ✅ To Top Button (admin 공통: 내부 스크롤 대응)
========================= */
function getScrollableAncestor(el) {
    let cur = el;
    while (cur && cur !== document.body && cur !== document.documentElement) {
        const style = window.getComputedStyle(cur);
        const oy = style.overflowY;
        const canScroll = (oy === "auto" || oy === "scroll") && cur.scrollHeight > cur.clientHeight + 5;
        if (canScroll) return cur;
        cur = cur.parentElement;
    }
    return null;
}

function guessScroller() {
    if (els.listBody) {
        const anc = getScrollableAncestor(els.listBody);
        if (anc) return anc;
    }
    return document.scrollingElement || document.documentElement;
}

function setupToTop() {
    if (!els.toTopBtn) return;

    // 레이아웃 overflow/transform 이슈 회피: body로 이동
    if (els.toTopBtn.parentElement !== document.body) {
        document.body.appendChild(els.toTopBtn);
    }

    const scroller = guessScroller();

    const getTop = () => {
        if (scroller === document.documentElement || scroller === document.body || scroller === document.scrollingElement) {
            return window.scrollY || document.documentElement.scrollTop || 0;
        }
        return scroller.scrollTop || 0;
    };

    let ticking = false;
    const updateTopBtn = () => {
        if (ticking) return;
        ticking = true;
        requestAnimationFrame(() => {
            const y = getTop();
            // ✅ admin 공통 기준 (스크롤 좀 하면 등장)
            els.toTopBtn.classList.toggle("hidden", !(y > 500));
            ticking = false;
        });
    };

    window.addEventListener("scroll", updateTopBtn, { passive: true, capture: true });
    if (scroller && scroller !== window) {
        scroller.addEventListener("scroll", updateTopBtn, { passive: true, capture: true });
    }

    updateTopBtn();

    els.toTopBtn.addEventListener("click", () => {
        if (scroller === document.documentElement || scroller === document.body || scroller === document.scrollingElement) {
            window.scrollTo({ top: 0, behavior: "smooth" });
        } else {
            scroller.scrollTo({ top: 0, behavior: "smooth" });
        }
    });
}

/* =========================
   Events
========================= */
function bindEvents() {

    // 검색
    els.searchBtn?.addEventListener("click", () => {
        state.word = els.word?.value.trim();
        fetchList({ reset: true });
    });

    // 더보기 (기존 코드 유지: 혹시 숨김 풀어 쓰고 싶으면)
    els.moreBtn?.addEventListener("click", () => {
        if (state.hasNext) fetchList();
    });

    // 개별 체크
    els.listBody.addEventListener("change", (e) => {
        const cb = e.target.closest(".row-checkbox");
        if (!cb) return;

        cb.checked ? selected.add(cb.value) : selected.delete(cb.value);

        const has = selected.size > 0;
        retryDownloadBtn?.toggleAttribute("disabled", !has);
        retryTranscodeBtn?.toggleAttribute("disabled", !has);

        syncSelectAll();
    });

    // 전체 선택
    selectAll?.addEventListener("change", () => {
        const boxes = document.querySelectorAll(".row-checkbox");
        selected.clear();

        boxes.forEach(cb => {
            cb.checked = selectAll.checked;
            if (selectAll.checked) selected.add(cb.value);
        });

        const has = selected.size > 0;
        retryDownloadBtn?.toggleAttribute("disabled", !has);
        retryTranscodeBtn?.toggleAttribute("disabled", !has);
    });

    // 다운로드 재시도
    retryDownloadBtn?.addEventListener("click", async () => {
        const ids = Array.from(selected).map(Number);
        if (!ids.length) return;

        if (!confirm(`${ids.length}개 다운로드 재시도 하시겠습니까?`)) return;

        await authFetch("/api/admin/songs/download-request", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ songIdList: ids })
        });

        fetchList({ reset: true });
    });

    // 형변환 재시도
    retryTranscodeBtn?.addEventListener("click", async () => {
        const ids = Array.from(selected).map(Number);
        if (!ids.length) return;

        if (!confirm(`${ids.length}개 형변환 재시도 하시겠습니까?`)) return;

        await authFetch("/api/admin/songs/transcode-request", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ songIdList: ids })
        });

        fetchList({ reset: true });
    });

    // 관리 버튼
    els.listBody.addEventListener("click", async (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const { action, id } = btn.dataset;

        if (action === "edit") {
            location.href = `/admin/songs/${id}/update`;
        }

        if (action === "delete") {
            await authFetch(`/api/admin/songs/${id}`, { method: "DELETE" });
            fetchList({ reset: true });
        }

        if (action === "restore") {
            await authFetch(`/api/admin/songs/${id}/restore`, { method: "PATCH" });
            fetchList({ reset: true });
        }
    });
}

/* =========================
   Init
========================= */
document.addEventListener("DOMContentLoaded", () => {
    bindEvents();
    setupInfiniteScroll();
    setupToTop();
    fetchList({ reset: true });
});