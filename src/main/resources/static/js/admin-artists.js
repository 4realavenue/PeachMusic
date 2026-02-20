import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/artists";

const els = {
    word: document.getElementById("wordInput"),
    searchBtn: document.getElementById("searchBtn"),
    listBody: document.getElementById("listBody"),
    moreBtn: document.getElementById("moreBtn"), // 구조는 유지하되 무한스크롤이라 숨김
    emptyBox: document.getElementById("emptyBox"),
    sentinel: document.getElementById("sentinel"),
    toTopBtn: document.getElementById("toTopBtn"),
};

let state = {
    word: "",
    lastId: null,
    loading: false,
    hasNext: true,
    observer: null,
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

/**
 * ✅ HTML 엔티티 디코딩 (&amp; → &, &quot; → " ...)
 * 글씨 깨짐(엔티티가 그대로 노출) 방지
 */
function decodeHtmlEntities(str) {
    if (str == null) return "";
    const txt = document.createElement("textarea");
    txt.innerHTML = String(str);
    return txt.value;
}

function safeText(str) {
    // ✅ 엔티티를 먼저 풀고, 그 다음 escape 해서 안전 출력
    return escapeHtml(decodeHtmlEntities(str));
}

function resolveImageUrl(imagePath) {
    if (!imagePath) return "/images/default.png";
    const p = String(imagePath);
    if (p.startsWith("http://") || p.startsWith("https://")) return p;
    if (p.startsWith("/")) return p;
    return `/${p}`;
}

function setEmpty(isEmpty) {
    if (!els.emptyBox) return;
    els.emptyBox.classList.toggle("hidden", !isEmpty);
}

function setEmptyMessage(word) {
    if (!els.emptyBox) return;
    const msg = word && word.trim().length > 0 ? "검색 결과가 없습니다." : "등록된 데이터가 없습니다.";
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

            const profileImage =
                a.profileImage ??
                a.profileImageUrl ??
                a.profileImagePath ??
                a.artistProfileImage ??
                null;

            const imgUrl = resolveImageUrl(profileImage);

            // ✅ 글씨 깨짐 방지: 엔티티 디코딩 후 안전 출력
            const artistNameSafe = safeText(a.artistName);

            const nameHtml = isDeleted
                ? `
                    <span class="artist-link disabled"
                          data-disabled-link="true"
                          title="비활성화된 아티스트는 일반 상세로 이동할 수 없습니다.">
                        ${artistNameSafe}
                    </span>
                  `
                : `
                    <a href="/artists/${a.artistId}" class="artist-link">
                        ${artistNameSafe}
                    </a>
                  `;

            const statusButton = isDeleted
                ? `<button class="btn primary" data-action="restore" data-id="${a.artistId}">복구</button>`
                : `<button class="btn danger" data-action="delete" data-id="${a.artistId}">삭제</button>`;

            return `
                <div class="row ${isDeleted ? "is-deleted" : ""}">
                    <div class="col id">${a.artistId}</div>

                    <div class="col profile">
                        <div class="artist-thumb" title="${artistNameSafe}">
                            <img src="${escapeHtml(imgUrl)}"
                                 alt="${artistNameSafe}"
                                 loading="lazy"
                                 onerror="this.onerror=null;this.src='/images/default.png';" />
                        </div>
                    </div>

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
    if (!state.hasNext && !reset) return;

    state.loading = true;

    if (reset) {
        state.lastId = null;
        state.hasNext = true;
        els.listBody.innerHTML = "";
        setEmpty(false);
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
            state.hasNext = false;
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
    } catch (e) {
        console.error(e);
        alert("목록 조회 중 오류가 발생했습니다.");
    } finally {
        state.loading = false;
    }
}

/* ============================
   Infinite Scroll
============================ */
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

/* ============================
   To Top Button (index와 동일 노출 기준 + admin 내부 스크롤 대응)
   - 노출 기준: y > 500
============================ */
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

    // 더보기 버튼은 사용하지 않음
    els.moreBtn?.classList.add("hidden");

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
    if (!els.listBody || !els.emptyBox) {
        console.error("[admin-artists] required elements not found");
        return;
    }

    bindEvents();
    setupInfiniteScroll();
    setupToTop();
    fetchList({ reset: true });
});