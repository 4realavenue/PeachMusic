document.addEventListener("DOMContentLoaded", () => {

    const token = localStorage.getItem("accessToken");

    const loginBtn = document.getElementById("loginBtn");
    const logoutBtn = document.getElementById("logoutBtn");
    const mypageBtn = document.getElementById("mypageBtn");

    /* 로그인 상태 UI */

    if (token) {
        loginBtn?.classList.add("hidden");
        logoutBtn?.classList.remove("hidden");
        mypageBtn?.classList.remove("hidden");
    } else {
        loginBtn?.classList.remove("hidden");
        logoutBtn?.classList.add("hidden");
        mypageBtn?.classList.add("hidden");
    }

    /* 좋아요 */

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

    /* 로그아웃 */

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {

            localStorage.removeItem("accessToken");
            location.href = "/";
        });
    }

});
