document.addEventListener("DOMContentLoaded", () => {
    const articleEl = document.getElementById("originalArticle");
    const articleText = articleEl.textContent;

    const wordLoadingBadge = document.getElementById("wordLoadingBadge");
    wordLoadingBadge.style.display = "inline-block";

    fetch("/api/analyze", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ article: articleText })
    })
    .then(res => res.json())
    .then(async data => {

        // 요약 표시
        document.getElementById("loadingBox").style.display = "none";
        const summaryBox = document.getElementById("summaryBox");
        summaryBox.style.display = "block";
        summaryBox.innerText = data.summary;

        // 단어 설명 조회
        let wordInfoMap = {};
        try {
            const res = await fetch("/api/word-info-batch", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ words: data.extractedWords })
            });

            const wordDescriptions = await res.json();
            wordDescriptions.forEach(w => {
                wordInfoMap[w.word] = w.description;
            });
        } catch (e) {
            console.error(e);
        }

        highlightWordsWithTooltip(data.extractedWords, wordInfoMap);
        wordLoadingBadge.style.display = "none";
    })
    .catch(err => {
        console.error(err);
        document.getElementById("loadingBox").style.display = "none";
        document.getElementById("summaryBox").style.display = "block";
        document.getElementById("summaryBox").innerText =
            "❌ 분석 중 오류가 발생했습니다.";
        wordLoadingBadge.style.display = "none";
    });
});

/* =========================
   🔐 HTML Escape (핵심)
========================= */
function escapeHtml(str) {
    return str
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#39;");
}

/* =========================
   하이라이트 (최초 1회)
========================= */
function highlightWordsWithTooltip(words, wordInfoMap) {
    const container = document.getElementById("originalArticle");

    const sortedWords = [...words]
        .filter(Boolean)
        .sort((a, b) => b.length - a.length);

    const highlightedWords = new Set();

    const walker = document.createTreeWalker(
        container,
        NodeFilter.SHOW_TEXT,
        null
    );

    const textNodes = [];
    let node;
    while ((node = walker.nextNode())) {
        textNodes.push(node);
    }

    textNodes.forEach(textNode => {
        const originalText = textNode.nodeValue;
        let replacedText = originalText;

        sortedWords.forEach(word => {
            const key = word.toLowerCase();
            if (highlightedWords.has(key)) return;

            const escapedWord = word.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
            const regex = new RegExp(`(${escapedWord})`);

            if (regex.test(replacedText)) {
                // ✅ title 속성 값만 escape
                const desc = (wordInfoMap[word] || "설명 없음")
                    .replace(/"/g, "&quot;")
                    .replace(/'/g, "&#39;");

                // ✅ mark 태그 HTML은 그대로 넣기
                replacedText = replacedText.replace(
                    regex,
                    `<mark class="highlight-word"
                           data-bs-toggle="tooltip"
                           data-bs-placement="top"
                           title="${desc}">$1</mark>`
                );

                highlightedWords.add(key);
            }
        });

        if (replacedText !== originalText) {
            const span = document.createElement("span");
            span.innerHTML = replacedText;
            textNode.parentNode.replaceChild(span, textNode);
        }
    });

    // Bootstrap Tooltip 활성화
    container.querySelectorAll('[data-bs-toggle="tooltip"]')
        .forEach(el => new bootstrap.Tooltip(el));
}