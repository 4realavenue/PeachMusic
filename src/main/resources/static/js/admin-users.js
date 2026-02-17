import { authFetch } from "/js/auth.js";

const API_URL = "/api/admin/users";

const listBody = document.getElementById("listBody");
const sentinel = document.getElementById("sentinel");
const loadingEl = document.getElementById("loading");
const endMessage = document.getElementById("endMessage");
const searchBtn = document.getElementById("searchBtn");
const wordInput = document.getElementById("wordInput");

let lastId = null;
let hasNext = true;
let isLoading = false;
let observer = null;
let currentWord = "";

document.addEventListener("DOMContentLoaded", () => {
    bindSearch();
    init();
});

async function init(){
    await load(true);
    setupInfiniteScroll();
}

/* =========================
   목록 조회
========================= */
async function load(reset = false){

    if(reset){
        lastId = null;
        hasNext = true;
        listBody.innerHTML = "";
        endMessage.classList.add("hidden");
        observer?.disconnect();
    }

    if(!hasNext || isLoading) return;

    isLoading = true;
    loadingEl.classList.remove("hidden");

    let url = API_URL;
    const params = new URLSearchParams();

    if(currentWord) params.set("word", currentWord);
    if(lastId !== null) params.set("lastId", lastId);

    if(params.toString()){
        url += `?${params.toString()}`;
    }

    try{
        const res = await authFetch(url);
        const result = await res.json();

        if(!result.success){
            alert(result.message);
            return;
        }

        const page = result.data;
        const content = page.content || [];

        if(!content.length && reset){
            listBody.innerHTML =
                `<div class="loading">검색 결과가 없습니다.</div>`;
            return;
        }

        render(content);

        hasNext = page.hasNext;
        lastId = page.nextCursor?.lastId ?? lastId;

        if(!hasNext){
            endMessage.classList.remove("hidden");
        }

    }catch(e){
        console.error("회원 조회 실패", e);
    }finally{
        isLoading = false;
        loadingEl.classList.add("hidden");
        if(reset) setupInfiniteScroll();
    }
}

/* =========================
   렌더링
========================= */
function render(list){

    list.forEach(u => {

        const row = document.createElement("div");
        row.className = "row";

        const isDeleted = u.deleted === true;

        row.innerHTML = `
            <div class="col id">${u.userId}</div>
            <div class="col email">${u.email}</div>
            <div class="col nickname">${u.nickname}</div>

            <div class="col role">
                <select class="role-select" data-id="${u.userId}">
                    <option value="USER"  ${u.role === "USER" ? "selected" : ""}>USER</option>
                    <option value="ADMIN" ${u.role === "ADMIN" ? "selected" : ""}>ADMIN</option>
                </select>
            </div>

            <div class="col status">
                <span class="badge ${isDeleted ? "inactive" : "active"}">
                    ${isDeleted ? "비활성" : "활성"}
                </span>
            </div>

            <div class="col manage">
                <div class="action-group">
                    <button class="btn active-btn"
                        data-action="activate"
                        data-id="${u.userId}"
                        ${!isDeleted ? "disabled" : ""}>
                        활성
                    </button>

                    <button class="btn inactive-btn"
                        data-action="deactivate"
                        data-id="${u.userId}"
                        ${isDeleted ? "disabled" : ""}>
                        비활성
                    </button>
                </div>
            </div>
        `;

        listBody.appendChild(row);
    });
}

/* =========================
   무한 스크롤
========================= */
function setupInfiniteScroll(){

    observer = new IntersectionObserver(async (entries)=>{
        if(entries[0].isIntersecting){
            await load(false);
        }
    }, { rootMargin:"400px" });

    observer.observe(sentinel);
}

/* =========================
   검색
========================= */
function bindSearch(){

    searchBtn.addEventListener("click", async ()=>{
        currentWord = wordInput.value.trim();
        await load(true);
    });

    wordInput.addEventListener("keydown", async (e)=>{
        if(e.key === "Enter"){
            currentWord = wordInput.value.trim();
            await load(true);
        }
    });
}

/* =========================
   활성 / 비활성
========================= */
listBody.addEventListener("click", async (e)=>{

    const btn = e.target.closest("button[data-action]");
    if(!btn) return;

    const id = btn.dataset.id;
    const action = btn.dataset.action;

    if(action === "deactivate"){

        if(!confirm("회원 비활성화 하시겠습니까?")) return;

        const res = await authFetch(
            `${API_URL}/${id}/delete`,
            { method:"DELETE" }
        );

        const json = await res.json();
        alert(json.message);
        await load(true);
    }

    if(action === "activate"){

        if(!confirm("회원 활성화 하시겠습니까?")) return;

        const res = await authFetch(
            `${API_URL}/${id}/restore`,
            { method:"PATCH" }
        );

        const json = await res.json();
        alert(json.message);
        await load(true);
    }
});

/* =========================
   권한 변경
========================= */
listBody.addEventListener("change", async (e)=>{

    const select = e.target.closest(".role-select");
    if(!select) return;

    const id = select.dataset.id;
    const role = select.value;

    if(!confirm(`권한을 ${role}로 변경하시겠습니까?`)){
        await load(true);
        return;
    }

    const res = await authFetch(
        `${API_URL}/${id}/role`,
        {
            method:"PATCH",
            headers:{ "Content-Type":"application/json" },
            body:JSON.stringify({ role })
        }
    );

    const json = await res.json();
    alert(json.message);
});
