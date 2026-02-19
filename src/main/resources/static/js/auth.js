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
   - JSON 바디면 Content-Type 자동 추가
   - FormData(멀티파트)면 Content-Type 절대 설정하지 않음 (boundary는 브라우저가 자동)
============================ */

export async function authFetch(url, options = {}) {
    const token = getToken();

    // ✅ 호출부에서 준 헤더를 먼저 복사
    const headers = {
        ...(options.headers || {})
    };

    // ✅ FormData 여부 판별
    const isFormData =
        typeof FormData !== "undefined" &&
        options.body instanceof FormData;

    // ✅ JSON 요청일 때만 기본 Content-Type 부여
    // (사용자가 직접 Content-Type을 넣었다면 그대로 존중)
    if (!isFormData && !("Content-Type" in headers)) {
        headers["Content-Type"] = "application/json";
    }

    // ✅ Authorization 추가
    if (token) {
        headers["Authorization"] = token;
    }

    const res = await fetch(url, {
        ...options,
        headers
    });

    // ✅ 토큰 만료 처리
    if (res.status === 401) {
        removeToken();
        location.href = "/login";
        return;
    }

    return res;
}