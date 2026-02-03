package simple.articles.service.naver

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
@Service
class NaverApiService (
    @Value("\${naver.api.client-id}") private val clientId: String,
    @Value("\${naver.api.client-code}") private val clientCode: String
){

        fun getWordInfoResponse(word : String): String {
            var text: String? = null
            text = try {
                URLEncoder.encode("정치뉴스 ${word} 뜻", "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("검색어 인코딩 실패", e)
            }

            val apiURL = "https://openapi.naver.com/v1/search/webkr.json?query=$text&display=9"
            val requestHeaders: MutableMap<String, String> = HashMap()
            requestHeaders["X-Naver-Client-Id"] = clientId
            requestHeaders["X-Naver-Client-Secret"] = clientCode

            val responseBody: String = get(apiURL, requestHeaders)
            return makeDescription(responseBody, word)
        }

        fun getNewsResponse() {
            var text: String? = null
            text = try {
                URLEncoder.encode("kjk", "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                throw RuntimeException("검색어 인코딩 실패", e)
            }
            val apiURL = "https://openapi.naver.com/v1/search/news?query=$text" // JSON 결과

            val requestHeaders: MutableMap<String, String> = HashMap()
            requestHeaders["X-Naver-Client-Id"] = clientId
            requestHeaders["X-Naver-Client-Secret"] = clientCode

            val responseBody: String = get(apiURL, requestHeaders)
        }

}