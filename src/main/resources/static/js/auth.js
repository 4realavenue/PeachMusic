// auth.js

/* ============================
   Token 관리
============================ */

export function getToken() {
    return localStorage.getItem("accessToken");
}

export function setToken(token) {
    localStorage.setItem("accessToken", token);
}

export function removeToken() {
    localStorage.removeItem("accessToken");
}


/* ============================
   인증 fetch 래퍼
============================ */

export async function authFetch(url, options = {}) {

    const token = getToken();

    const headers = {
        "Content-Type": "application/json",
        ...options.headers
    };

    if (token) {
        headers["Authorization"] = token;
    }

    const res = await fetch(url, {
        ...options,
        headers
    });

    /* 토큰 만료 처리 */
    if (res.status === 401) {
        removeToken();
        location.href = "/login";
        return;
    }

    return res;
}
