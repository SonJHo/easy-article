package simple.articles.service

import org.apache.commons.text.StringEscapeUtils

open class PreprocessService {

    companion object {

        fun decodeHtml(text: String): String {
            return StringEscapeUtils.unescapeHtml4(text)
        }

        fun preprocessArticle(text: String): String {
            return text
                .replace(Regex("^\\(.*?\\)\\s*.*?기자\\s*="), "")
                .replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}"), "")
                .replace(Regex("([가-힣]{2,4}\\s*기자).*$"), "")
                .replace(Regex("<저작권자.*?재배포 금지>"), "")
                .replace(Regex("저작권자\\(c\\).*?무단전재-재배포 금지"), "")
                .replace(Regex("ⓒ.*?Copyrights"), "")
                .replace(Regex("\\[.*?\\]"), "")
                .replace(Regex("Pause|Mute|\\d{2}:\\d{2}"), "")
                .replace(Regex("\\n{2,}"), "\n")
                .trim()
        }

        fun normalizeText(text: String): String {
            return StringEscapeUtils.unescapeHtml4(text)   // &quot; → "
                .replace(Regex("<[^>]*>"), "")            // <b>, </b> 제거
                .replace(Regex("\\s+"), " ")               // 공백 정리
                .trim()
        }
    }
}