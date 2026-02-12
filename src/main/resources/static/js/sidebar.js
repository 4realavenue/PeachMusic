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
        likeBtn.addEventListener("click", () => {

            likeMenu.classList.toggle("open");

            likeArrow.textContent =
                likeMenu.classList.contains("open") ? "▴" : "▾";
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

            if (goLogin) {
                location.href = "/login";
            }
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
