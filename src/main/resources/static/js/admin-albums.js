import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/albums";

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
    hasNext: true,
    loading: false,
};

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function showEmpty(show) {
    if (!els.emptyBox) return;
    els.emptyBox.classList.toggle("hidden", !show);
}

function showMore(show) {
    if (!els.moreBtn) return;
    els.moreBtn.classList.toggle("hidden", !show);
}

function setEmptyMessage(word) {
    if (!els.emptyBox) return;
    const msg = word && word.trim().length > 0
        ? "검색 결과가 없습니다."
        : "등록된 데이터가 없습니다.";
    els.emptyBox.textContent = msg;
}

function getReleaseDate(a) {
    return a.albumReleaseDate ?? a.releaseDate ?? "-";
}

function getDeleted(a) {
    return (a.isDeleted ?? a.deleted) === true;
}

function renderRows(items, append = true) {
    if (!append) els.listBody.innerHTML = "";

    const html = items.map((a) => {
        const isDeleted = getDeleted(a);

        const badge = isDeleted
            ? `<span class="badge deleted">비활성</span>`
            : `<span class="badge">활성</span>`;

        const nameHtml = isDeleted
            ? `
                <span class="album-link disabled" data-disabled="true"
                      title="비활성화된 앨범은 일반 상세로 이동할 수 없습니다.">
                    ${escapeHtml(a.albumName)}
                </span>
              `
            : `
                <a class="album-link" href="/albums/${a.albumId}/page">
                    ${escapeHtml(a.albumName)}
                </a>
              `;

        const statusBtn = isDeleted
            ? `<button class="btn primary" data-action="restore" data-id="${a.albumId}">복구</button>`
            : `<button class="btn danger" data-action="delete" data-id="${a.albumId}">삭제</button>`;

        return `
            <div class="row">
                <div class="col id">${a.albumId}</div>
                <div class="col name">${nameHtml} ${badge}</div>
                <div class="col date">${escapeHtml(getReleaseDate(a))}</div>
                <div class="col like">${a.likeCount ?? 0}</div>
                <div class="col manage">
                    <button class="btn" data-action="edit" data-id="${a.albumId}">수정</button>
                    ${statusBtn}
                </div>
            </div>
        `;
    }).join("");

    els.listBody.insertAdjacentHTML("beforeend", html);
}

async function fetchList({ reset = false } = {}) {
    if (state.loading) return;
    state.loading = true;

    if (reset) {
        state.lastId = null;
        state.hasNext = true;
        els.listBody.innerHTML = "";
        showEmpty(false);
        showMore(false);
    }

    try {
        const params = new URLSearchParams();
        if (state.word) params.set("word", state.word);
        if (state.lastId != null) params.set("lastId", String(state.lastId));

        const res = await authFetch(`${API_URL}?${params.toString()}`, { method: "GET" });
        if (!res) return;

        const json = await res.json();
        if (!res.ok || json?.success === false) {
            alert(json?.message || "앨범 목록 조회 실패");
            return;
        }

        const data = json.data || {};
        const content = data.content || [];

        if (content.length === 0 && els.listBody.children.length === 0) {
            setEmptyMessage(state.word);
            showEmpty(true);
            showMore(false);
            return;
        }

        renderRows(content, true);

        state.hasNext = typeof data.hasNext === "boolean" ? data.hasNext : false;
        state.lastId = data.nextCursor?.lastId ?? (content.at(-1)?.albumId ?? state.lastId);

        showMore(state.hasNext);
    } catch (e) {
        console.error(e);
        alert("목록 조회 중 오류가 발생했습니다.");
    } finally {
        state.loading = false;
    }
}

function bindEvents() {
    els.searchBtn.addEventListener("click", () => {
        state.word = (els.word.value || "").trim();
        fetchList({ reset: true });
    });

    els.word.addEventListener("keydown", (e) => {
        if (e.key === "Enter") {
            state.word = (els.word.value || "").trim();
            fetchList({ reset: true });
        }
    });

    els.moreBtn.addEventListener("click", () => {
        if (!state.hasNext) return;
        fetchList({ reset: false });
    });

    // 비활성 앨범명 클릭 안내
    els.listBody.addEventListener("click", (e) => {
        const disabled = e.target.closest("[data-disabled='true']");
        if (!disabled) return;
        alert("비활성화된 앨범은 일반 상세 페이지로 이동할 수 없습니다.\n복구 후 다시 시도해주세요.");
    });

    // 버튼 액션
    els.listBody.addEventListener("click", async (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const action = btn.dataset.action;
        const id = btn.dataset.id;

        if (action === "edit") {
            location.href = `/admin/albums/${id}/update`;
            return;
        }

        if (action === "delete") {
            const msg =
                "앨범을 비활성화(삭제) 하시겠습니까?\n\n" +
                "해당 앨범의 음원도 함께 비활성화됩니다.";

            if (!confirm(msg)) return;

            const res = await authFetch(`/api/admin/albums/${id}`, { method: "DELETE" });
            if (!res) return;

            const json = await res.json();
            if (!res.ok || json?.success === false) {
                alert(json?.message || "비활성화 실패");
                return;
            }

            alert(json.message || "앨범이 비활성화 되었습니다.");
            fetchList({ reset: true });
            return;
        }

        if (action === "restore") {
            if (!confirm("앨범을 복구(활성화) 하시겠습니까?")) return;

            const res = await authFetch(`/api/admin/albums/${id}/restore`, { method: "PATCH" });
            if (!res) return;

            const json = await res.json();
            if (!res.ok || json?.success === false) {
                alert(json?.message || "복구 실패");
                return;
            }

            alert(json.message || "앨범이 활성화 되었습니다.");
            fetchList({ reset: true });
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    if (!els.listBody || !els.moreBtn || !els.emptyBox) {
        console.error("[admin-albums] required elements not found");
        return;
    }
    bindEvents();
    fetchList({ reset: true });
});
