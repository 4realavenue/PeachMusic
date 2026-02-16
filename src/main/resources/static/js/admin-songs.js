import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/songs";

const els = {
    word: document.getElementById("wordInput"),
    searchBtn: document.getElementById("searchBtn"),
    listBody: document.getElementById("listBody"),
    moreBtn: document.getElementById("moreBtn"),
    emptyBox: document.getElementById("emptyBox"),
};

let state = {
    word: "",
    lastId: null,
    loading: false,
    hasNext: true,
};

/* =========================
   Toast
========================= */
function showToast(message) {
    const toast = document.createElement("div");
    toast.className = "admin-toast";
    toast.textContent = message;

    document.body.appendChild(toast);

    setTimeout(() => toast.classList.add("show"), 10);

    setTimeout(() => {
        toast.classList.remove("show");
        setTimeout(() => toast.remove(), 300);
    }, 2500);
}

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

function setEmpty(isEmpty) {
    els.emptyBox?.classList.toggle("hidden", !isEmpty);
}

function setMoreVisible(visible) {
    els.moreBtn?.classList.toggle("hidden", !visible);
}

function setEmptyMessage(word) {
    if (!els.emptyBox) return;
    els.emptyBox.textContent =
        word && word.trim().length > 0 ? "검색 결과가 없습니다." : "등록된 데이터가 없습니다.";
}

function formatDate(v) {
    if (!v) return "-";
    return String(v); // LocalDate: yyyy-MM-dd 예상
}

function safeNumber(v, fallback = 0) {
    const n = Number(v);
    return Number.isFinite(n) ? n : fallback;
}

/**
 * ✅ SongSearchResponseDto 기준
 * songId, name, artistName, releaseDate, albumImage,
 * likeCount, playCount, isDeleted, progressingStatus
 */
function normalizeSong(s) {
    return {
        songId: s.songId,
        name: s.name ?? "-",
        artistName: s.artistName ?? "-",
        releaseDate: s.releaseDate ?? null,
        albumImage: s.albumImage ?? null,
        likeCount: safeNumber(s.likeCount, 0),
        playCount: safeNumber(s.playCount, 0),
        isDeleted: s.isDeleted === true,
        progressingStatus: s.progressingStatus ?? null,
    };
}

function renderRows(items, append = true) {
    if (!append) els.listBody.innerHTML = "";

    const html = items.map((raw) => {
        const s = normalizeSong(raw);

        const badge = s.isDeleted
            ? `<span class="badge deleted">비활성</span>`
            : `<span class="badge">활성</span>`;

        const statusBadge = s.progressingStatus
            ? `<span class="status-badge">${escapeHtml(s.progressingStatus)}</span>`
            : "";

        // ✅ 곡명 클릭: 활성은 상세 이동 / 비활성은 토스트 안내
        const nameHtml = s.isDeleted
            ? `<span class="song-link disabled" data-disabled="true">${escapeHtml(s.name)}</span>`
            : `<a class="song-link" href="/songs/${s.songId}/page">${escapeHtml(s.name)}</a>`;

        const albumImageHtml = s.albumImage
            ? `<img class="album-cover" src="${escapeHtml(s.albumImage)}" alt="album"/>`
            : `<div class="album-cover-fallback">🎵</div>`;

        const metaLine1 = `${escapeHtml(s.artistName)} · ${escapeHtml(formatDate(s.releaseDate))}`;
        const metaLine2 = `♥ ${escapeHtml(s.likeCount)} · ▶ ${escapeHtml(s.playCount)} ${statusBadge}`;

        const statusButton = s.isDeleted
            ? `<button class="btn primary" data-action="restore" data-id="${s.songId}">복구</button>`
            : `<button class="btn danger" data-action="delete" data-id="${s.songId}">삭제</button>`;

        return `
            <div class="row">
                <div class="col id">${escapeHtml(s.songId)}</div>

                <div class="col name">
                    ${nameHtml}
                </div>

                <div class="col album">
                    <div class="album-stack">
                        <div class="album-cover-wrap">
                            ${albumImageHtml}
                        </div>
                        ${badge}
                    </div>
                </div>

                <div class="col meta">
                    <div class="meta-line">${metaLine1}</div>
                    <div class="meta-line">${metaLine2}</div>
                </div>

                <div class="col manage">
                    <button class="btn" data-action="edit" data-id="${s.songId}">수정</button>
                    ${statusButton}
                </div>
            </div>
        `;
    }).join("");

    els.listBody.insertAdjacentHTML("beforeend", html);
}

/* =========================
   Fetch List (Keyset)
========================= */
async function fetchList({ reset = false } = {}) {
    if (state.loading) return;
    state.loading = true;

    if (reset) {
        state.lastId = null;
        state.hasNext = true;
        els.listBody.innerHTML = "";
        setEmpty(false);
        setMoreVisible(false);
    }

    try {
        const params = new URLSearchParams();

        if (state.word) params.set("word", state.word);
        if (state.lastId != null) params.set("lastId", String(state.lastId));

        const url = `${API_URL}?${params.toString()}`;

        const res = await authFetch(url, { method: "GET" });
        if (!res) return;

        const json = await res.json();

        if (!res.ok || json?.success === false) {
            alert(json?.message || "음원 목록 조회 실패");
            return;
        }

        const data = json.data || {};
        const content = data.content || [];

        if (content.length === 0 && els.listBody.children.length === 0) {
            setEmptyMessage(state.word);
            setEmpty(true);
            setMoreVisible(false);
            return;
        }

        renderRows(content, true);

        state.hasNext = typeof data.hasNext === "boolean" ? data.hasNext : content.length > 0;

        const nextCursor = data.nextCursor;
        const lastItem = content[content.length - 1];
        const lastNorm = lastItem ? normalizeSong(lastItem) : null;

        state.lastId =
            nextCursor?.lastId ??
            lastNorm?.songId ??
            state.lastId;

        setMoreVisible(state.hasNext);
    } catch (e) {
        console.error(e);
        alert("목록 조회 중 오류가 발생했습니다.");
    } finally {
        state.loading = false;
    }
}

/* =========================
   Events
========================= */
function bindEvents() {
    // 검색
    els.searchBtn?.addEventListener("click", () => {
        state.word = (els.word?.value || "").trim();
        fetchList({ reset: true });
    });

    els.word?.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            state.word = (els.word?.value || "").trim();
            fetchList({ reset: true });
        }
    });

    // 더보기
    els.moreBtn?.addEventListener("click", () => {
        if (!state.hasNext) return;
        fetchList({ reset: false });
    });

    // ✅ 비활성 곡 클릭 안내(토스트)
    els.listBody.addEventListener("click", (e) => {
        const disabled = e.target.closest(".song-link[data-disabled='true']");
        if (!disabled) return;
        showToast("비활성화된 음원은 일반 상세로 이동할 수 없습니다.");
    });

    // 관리 버튼
    els.listBody.addEventListener("click", async (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const action = btn.dataset.action;
        const id = btn.dataset.id;

        if (!id) return;

        if (action === "edit") {
            location.href = `/admin/songs/${id}/update`;
            return;
        }

        if (action === "delete") {
            if (!confirm("음원을 비활성화(삭제) 하시겠습니까?")) return;

            const res = await authFetch(`/api/admin/songs/${id}`, { method: "DELETE" });
            if (!res) return;

            const json = await res.json();

            if (!res.ok || json?.success === false) {
                alert(json?.message || "비활성화 실패");
                return;
            }

            alert(json.message || "음원이 비활성화 되었습니다.");
            fetchList({ reset: true });
            return;
        }

        if (action === "restore") {
            if (!confirm("음원을 복구(활성화) 하시겠습니까?")) return;

            const res = await authFetch(`/api/admin/songs/${id}/restore`, { method: "PATCH" });
            if (!res) return;

            const json = await res.json();

            if (!res.ok || json?.success === false) {
                alert(json?.message || "복구 실패");
                return;
            }

            alert(json.message || "음원이 활성화 되었습니다.");
            fetchList({ reset: true });
        }
    });
}

/* =========================
   Init
========================= */
document.addEventListener("DOMContentLoaded", () => {
    if (!els.listBody || !els.moreBtn || !els.emptyBox) {
        console.error("[admin-songs] required elements not found");
        return;
    }

    bindEvents();
    fetchList({ reset: true });
});
