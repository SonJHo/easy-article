package simple.articles.service.naver

import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL


fun get(apiUrl: String, requestHeaders: Map<String, String>): String {
    val con: HttpURLConnection = connect(apiUrl)
    return try {
        con.setRequestMethod("GET")
        for ((key, value) in requestHeaders) {
            con.setRequestProperty(key, value)
        }
        val responseCode: Int = con.getResponseCode()
        if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
            readBody(con.inputStream)
        } else { // 오류 발생
            readBody(con.errorStream)
        }
    } catch (e: IOException) {
        throw java.lang.RuntimeException("API 요청과 응답 실패", e)
    } finally {
        con.disconnect()
    }
}


private fun connect(apiUrl: String): HttpURLConnection {
    return try {
        val url = URL(apiUrl)
        url.openConnection() as HttpURLConnection
    } catch (e: MalformedURLException) {
        throw java.lang.RuntimeException("API URL이 잘못되었습니다. : $apiUrl", e)
    } catch (e: IOException) {
        throw java.lang.RuntimeException("연결이 실패했습니다. : $apiUrl", e)
    }
}


private fun readBody(body: InputStream): String {
    val streamReader = InputStreamReader(body)
    try {
        BufferedReader(streamReader).use { lineReader ->
            val responseBody = StringBuilder()
            var line: String?
            while (lineReader.readLine().also { line = it } != null) {
                responseBody.append(line)
            }
            return responseBody.toString()
        }
    } catch (e: IOException) {
        throw java.lang.RuntimeException("API 응답을 읽는 데 실패했습니다.", e)
    }
}

fun makeDescription(
    json: String,
    word: String,
    maxDescLength: Int = 200
): String {
    val root = JSONObject(json)
    val items = root.optJSONArray("items") ?: return ""

    val sb = StringBuilder("targetWord: $word\n")

    for (i in 0..<items.length()) {
        val item = items.getJSONObject(i)

        val title = item.optString("title")
            .cleanText()
            .truncate(80)

        val description = item.optString("description")
            .cleanText()
            .truncate(maxDescLength)

        sb.append("• ")
        sb.append(title)
        sb.append(" : ")
        sb.append(description)
        sb.append("\n")
    }
    return sb.toString()
}

fun String.truncate(maxLength: Int): String {
    return if (this.length <= maxLength) this
    else this.substring(0, maxLength) + "..."
}

fun String.cleanText(): String {
    return this
        .replace(Regex("<[^>]*>"), "")                 // HTML 태그 제거
        .replace(Regex("[一-龥]"), "")                  // 한자 제거
        .replace(Regex("[^가-힣a-zA-Z0-9\\s.,]"), "")   // 특수문자 제거
        .replace(Regex("\\s+"), " ")                   // 공백 정리
        .trim()
}



