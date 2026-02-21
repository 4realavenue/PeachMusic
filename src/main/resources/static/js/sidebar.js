document.addEventListener("DOMContentLoaded", () => {

    const token = localStorage.getItem("accessToken");

    const loginBtn = document.getElementById("loginBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const mypageBtn = document.getElementById("mypageBtn");

    /* =========================
       로그인 상태 UI
    ========================= */
    if (token) {
        loginBtn?.classList.add("hidden");
        logoutBtn?.classList.remove("hidden");
        mypageBtn?.classList.remove("hidden");
    } else {
        loginBtn?.classList.remove("hidden");
        logoutBtn?.classList.add("hidden");
        mypageBtn?.classList.add("hidden");
    }

    /* =========================
       좋아요 드롭다운
    ========================= */
    const likeBtn = document.getElementById("likeBtn");
    const likeMenu = document.getElementById("likeMenu");
    const likeArrow = document.getElementById("likeArrow");

    if (likeBtn && likeMenu && likeArrow) {
        likeBtn.addEventListener("click", (e) => {
            // ✅ likeBtn이 <a>면 이동을 막아야 토글이 보임
            e.preventDefault();
            e.stopPropagation();

            likeMenu.classList.toggle("open");
            likeArrow.textContent = likeMenu.classList.contains("open") ? "▴" : "▾";
        });
    }

    /* =========================
       🔥 추천 버튼 로그인 체크
    ========================= */
    const recommendLink = document.querySelector('a[href="/recommend"]');

    recommendLink?.addEventListener("click", (e) => {
        const token = localStorage.getItem("accessToken");
        if (!token) {
            e.preventDefault();
            const goLogin = confirm(
                "추천 기능은 로그인한 사용자만 이용 가능합니다.\n로그인 페이지로 이동하시겠습니까?"
            );
            if (goLogin) location.href = "/login";
        }
    });

    /* =========================
       로그아웃
    ========================= */
    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.removeItem("accessToken");
            location.href = "/";
        });
    }
});

/* =========================
   관리자 메뉴 표시 (JWT decode)
========================= */
const adminBtn = document.getElementById("adminBtn");

function parseJwt(token) {
    try {
        const payload = token.split(".")[1];

        // base64url -> base64
        let s = payload.replace(/-/g, "+").replace(/_/g, "/");
        const pad = s.length % 4;
        if (pad === 2) s += "==";
        else if (pad === 3) s += "=";

        return JSON.parse(atob(s));
    } catch (e) {
        return null;
    }
}

function applyAdminMenuFromToken() {
    const token = localStorage.getItem("accessToken");
    if (!token || !adminBtn) return;

    const decoded = parseJwt(token);
    if (!decoded) return;

    const role = decoded.role || decoded.userRole || decoded.auth || decoded.authorities;
    const roleStr = Array.isArray(role) ? role.join(",") : String(role ?? "");

    if (roleStr.includes("ADMIN")) {
        adminBtn.classList.remove("hidden");
    }
}

applyAdminMenuFromToken();