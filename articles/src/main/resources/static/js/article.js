function validateArticle() {
    const article = document.getElementById("article").value;
    const errorMessage = document.getElementById("errorMessage");

    if (article.length > 4000) {
        errorMessage.classList.remove("d-none");
        errorMessage.innerText = "❌ 기사는 최대 4000자까지 입력할 수 있습니다.";
        return false; // 서버로 전송 안 함
    }

    if (article.trim().length === 0) {
        errorMessage.classList.remove("d-none");
        errorMessage.innerText = "❌ 기사를 입력해 주세요.";
        return false;
    }

    errorMessage.classList.add("d-none");
    return true; // 서버로 전송
}
