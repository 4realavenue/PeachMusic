import { authFetch } from "../auth.js";

/* ============================
   관리자 공통 fetch
============================ */

export async function adminFetch(url, options = {}) {

    const res = await authFetch(url, options);

    if (!res) return;

    const data = await res.json();

    if (!res.ok) {
        alert(data.message || "요청 실패");
        return;
    }

    return data;
}
