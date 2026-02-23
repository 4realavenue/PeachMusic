document.addEventListener("DOMContentLoaded", () => {

    const form = document.getElementById("signupForm");
    const emailSendBtn = document.getElementById("emailSendBtn");
    const emailVerifyBtn = document.getElementById("emailVerifyBtn");

    let emailVerified = false;

    // 1️⃣ 이메일 코드 발송
    emailSendBtn.addEventListener("click", async () => {

        const email = form.email.value;

        if (!email) {
            alert("이메일을 입력하세요.");
            return;
        }

        const res = await fetch("/api/auth/email/code-send", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email })
        });

        const result = await res.json();

        if (!res.ok) {
            alert(result.message || "이메일 발송 실패");
            return;
        }

        alert("인증 코드가 발송되었습니다.");
    });

    // 2️⃣ 인증 코드 확인
    emailVerifyBtn.addEventListener("click", async () => {

        const email = form.email.value;
        const code = form.authCode.value;

        if (!code) {
            alert("인증번호를 입력하세요.");
            return;
        }

        const res = await fetch("/api/auth/email/verify-code", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code })
        });

        const result = await res.json();

        if (!res.ok) {
            alert(result.message || "인증 실패");
            return;
        }

        alert("이메일 인증 완료");
        emailVerified = true;
    });

    // 3️⃣ 회원가입
    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        if (!emailVerified) {
            alert("이메일 인증을 완료해주세요.");
            return;
        }

        if (form.password.value !== form.passwordCheck.value) {
            alert("비밀번호가 일치하지 않습니다.");
            return;
        }

        const data = {
            name: form.name.value,
            nickname: form.nickname.value,
            email: form.email.value,
            password: form.password.value
        };

        const res = await fetch("/api/auth/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(data)
        });

        const result = await res.json();

        if (!res.ok) {
            alert(result.message || "회원가입 실패");
            return;
        }

        alert("회원가입 완료");
        location.href = "/login";
    });

});
