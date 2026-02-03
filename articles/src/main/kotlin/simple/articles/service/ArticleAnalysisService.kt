package simple.articles.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import simple.articles.service.gpt.GptService


data class AnalysisResult(
    var summary :String,
    var keywords : String,
    var extractedWords: List<String>
)
@Service
class ArticleAnalysisService (
    private val gptService: GptService
){

    suspend fun analyze(article: String): AnalysisResult =
        withContext(Dispatchers.IO) {

            val normalizeText = PreprocessService.normalizeText(
                PreprocessService.preprocessArticle(article)
            )
            val responseList =
                gptService.getExtractedQuestionResponse(normalizeText)

            val summary = responseList[0]
            val keywords = responseList[4]
            val extractedWords = responseList[2]
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinct()
            println(summary)
            println(extractedWords)
            println(keywords)
            AnalysisResult(
                summary = summary,
                keywords = keywords,
                extractedWords = extractedWords
            )

        }
}