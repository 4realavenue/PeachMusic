import { setToken } from "./auth.js";

document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const email = form.email.value;
        const password = form.password.value;

        try {
            const res = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password }),
            });

            const payload = await res.json();

            if (!res.ok || payload?.success === false) {
                alert(payload?.message ?? "이메일 또는 비밀번호가 올바르지 않습니다.");
                return;
            }

            // ✅ 서버 응답 구조: payload.data.token
            let token = payload?.data?.token;

            if (!token) {
                alert("토큰이 없습니다. 응답 구조를 확인하세요.");
                console.error("토큰 없음 payload:", payload);
                return;
            }

            // 혹시 Bearer 빠진 경우 대비
            if (!token.startsWith("Bearer ")) token = "Bearer " + token;

            setToken(token);
            location.href = "/";
        } catch (err) {
            console.error(err);
            alert("서버 오류가 발생했습니다.");
        }
    });
});
