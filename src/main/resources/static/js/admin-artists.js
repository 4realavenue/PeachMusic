import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/artists";

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

/* ============================
   Utils
============================ */

function escapeHtml(s) {
    return String(s ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function setEmpty(isEmpty) {
    if (!els.emptyBox) return;
    els.emptyBox.classList.toggle("hidden", !isEmpty);
}

function setMoreVisible(visible) {
    if (!els.moreBtn) return;
    els.moreBtn.classList.toggle("hidden", !visible);
}

function setEmptyMessage(word) {
    if (!els.emptyBox) return;
    const msg = word && word.trim().length > 0
        ? "검색 결과가 없습니다."
        : "등록된 데이터가 없습니다.";
    els.emptyBox.textContent = msg;
}

/* ============================
   Render
============================ */

function renderRows(items, append = true) {
    if (!append) els.listBody.innerHTML = "";

    const html = items
        .map((a) => {
            const isDeleted = (a.isDeleted ?? a.deleted) === true;

            const badge = isDeleted
                ? `<span class="badge deleted">비활성</span>`
                : `<span class="badge">활성</span>`;

            const nameHtml = isDeleted
                ? `
                    <span class="artist-link disabled"
                          data-disabled-link="true"
                          title="비활성화된 아티스트는 일반 상세로 이동할 수 없습니다.">
                        ${escapeHtml(a.artistName)}
                    </span>
                  `
                : `
                    <a href="/artists/${a.artistId}" class="artist-link">
                        ${escapeHtml(a.artistName)}
                    </a>
                  `;

            const statusButton = isDeleted
                ? `<button class="btn primary" data-action="restore" data-id="${a.artistId}">복구</button>`
                : `<button class="btn danger" data-action="delete" data-id="${a.artistId}">삭제</button>`;

            return `
                <div class="row">
                    <div class="col id">${a.artistId}</div>
                    <div class="col name">
                        ${nameHtml}
                        ${badge}
                    </div>
                    <div class="col like">${a.likeCount ?? 0}</div>
                    <div class="col manage">
                        <button class="btn" data-action="edit" data-id="${a.artistId}">수정</button>
                        ${statusButton}
                    </div>
                </div>
            `;
        })
        .join("");

    els.listBody.insertAdjacentHTML("beforeend", html);
}

/* ============================
   Fetch List (Keyset)
============================ */

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

        const res = await authFetch(`${API_URL}?${params.toString()}`, { method: "GET" });
        if (!res) return;

        const json = await res.json();

        if (!res.ok || json?.success === false) {
            alert(json?.message || "아티스트 목록 조회 실패");
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
        const last = content[content.length - 1];

        state.lastId =
            nextCursor?.lastId ??
            nextCursor?.id ??
            last?.artistId ??
            state.lastId;

        setMoreVisible(state.hasNext);
    } catch (e) {
        console.error(e);
        alert("목록 조회 중 오류가 발생했습니다.");
    } finally {
        state.loading = false;
    }
}

/* ============================
   Events
============================ */

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

    // 비활성 이름 클릭 안내
    els.listBody.addEventListener("click", (e) => {
        const disabled = e.target.closest("[data-disabled-link='true']");
        if (!disabled) return;
        alert("비활성화된 아티스트는 일반 상세 페이지로 이동할 수 없습니다.\n복구 후 다시 시도해주세요.");
    });

    // 관리 버튼
    els.listBody.addEventListener("click", async (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;

        const action = btn.dataset.action;
        const id = btn.dataset.id;

        if (action === "edit") {
            location.href = `/admin/artists/${id}/update`;
            return;
        }

        if (action === "delete") {
            const confirmMessage =
                "아티스트를 비활성화(삭제) 하시겠습니까?\n\n" +
                "해당 아티스트의 앨범 및 음원도 함께 비활성화됩니다.";

            if (!confirm(confirmMessage)) return;

            const res = await authFetch(`/api/admin/artists/${id}`, { method: "DELETE" });
            if (!res) return;

            const json = await res.json();

            if (!res.ok || json?.success === false) {
                alert(json?.message || "비활성화 실패");
                return;
            }

            alert(json.message || "아티스트가 비활성화 되었습니다.");
            fetchList({ reset: true });
            return;
        }

        if (action === "restore") {
            if (!confirm("아티스트를 복구(활성화) 하시겠습니까?")) return;

            const res = await authFetch(`/api/admin/artists/${id}/restore`, { method: "PATCH" });
            if (!res) return;

            const json = await res.json();

            if (!res.ok || json?.success === false) {
                alert(json?.message || "복구 실패");
                return;
            }

            alert(json.message || "아티스트가 활성화 되었습니다.");
            fetchList({ reset: true });
        }
    });
}

/* ============================
   Init
============================ */

document.addEventListener("DOMContentLoaded", () => {
    if (!els.listBody || !els.moreBtn || !els.emptyBox) {
        console.error("[admin-artists] required elements not found");
        return;
    }

    bindEvents();
    fetchList({ reset: true });
});
