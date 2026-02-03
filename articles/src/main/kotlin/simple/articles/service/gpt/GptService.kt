package simple.articles.service.gpt

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.models.responses.Response
import com.openai.models.responses.ResponseCreateParams
import org.springframework.stereotype.Service

@Service
open class GptService(
) {
    fun getExtractedQuestionResponse(text: String): List<String> {
        val client = OpenAIOkHttpClient.fromEnv()

        val modelName = "gpt-5-mini"
        val params = ResponseCreateParams.builder()
            .input(EXTRACTION_PO + text)
            .model(modelName)
            .build()

        val response: Response = client.responses().create(params)

        val result = response.output()
            .asSequence()
            .mapNotNull { it.message().orElse(null) }   // Optional 처리 (reasoning 제거)
            .flatMap { it.content().asSequence() }
            .map { it.outputText() }
            .map { it.get().text() }
            .joinToString("\n")

        return result.split("\n")
    }


    fun getWordInfoBatchResponse(textList: List<String>): List<String> {
        val client = OpenAIOkHttpClient.fromEnv()
        val prompt = buildString {
            append(WORD_INFO_SUMMARY_PO)
            append("\n")
            textList.forEach { word ->
                append("- ").append(word).append("\n")
            }
        }

        val params = ResponseCreateParams.builder()
            .model("gpt-5-mini")
            .input(prompt)
            .build()

        val response: Response = client.responses().create(params)

        val result = response.output()
            .asSequence()
            .mapNotNull { it.message().orElse(null) }      // message만
            .flatMap { it.content().asSequence() }         // content 펼치기
            .mapNotNull { it.outputText().orElse(null) }   // output_text만
            .map { it.text() }                             // 실제 텍스트
            .joinToString("\n")

        return result.split("\n").filter { it.isNotBlank() }
    }

//        fun getContext(list: List<NaverNewsItem>): String { // 반환 타입을 String으로 지정
//            val client = OpenAIOkHttpClient.fromEnv()
//
//            val prompt = buildString {
//                append(CONTEXT_ANAL_PO)
//                append("\n\n[분석할 뉴스 리스트]\n")
//
//                list.forEachIndexed { index, item ->
//                    append("${index + 1}. ")
//                    append("제목: ${item.title}\n")
//                    append("설명: ${item.description}\n")
//                    append("날짜: ${item.pubDate}\n\n")
//                }
//            }
//
//            val params = ResponseCreateParams.builder()
//                .model("gpt-5-mini") // gpt-5-mini가 없는 경우 gpt-4o 또는 gpt-4o-mini 사용 권장
//                .input(prompt)
//                .build()
//
//            val response: Response = client.responses().create(params)
//
//            // 결과 텍스트 추출 및 반환
//            return response.output()
//                .asSequence()
//                .mapNotNull { it.message().orElse(null) }
//                .flatMap { it.content().asSequence() }
//                .mapNotNull { it.outputText().orElse(null) }
//                .map { it.text() }
//                .joinToString("\n")
//        }

}