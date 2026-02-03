package simple.articles.controller

import simple.articles.service.WordInfoBatchService
import org.springframework.web.bind.annotation.*
import simple.articles.ArticleRequest
import simple.articles.WordBatchRequest
import simple.articles.WordDescription
import simple.articles.service.AnalysisResult
import simple.articles.service.ArticleAnalysisService

@RestController
@RequestMapping("/api")
class ArticleApiController(
    private val articleAnalysisService: ArticleAnalysisService,
    private val wordInfoBatchService: WordInfoBatchService
) {

    @PostMapping("/analyze")
    suspend fun analyzeAjax(@RequestBody request: ArticleRequest): AnalysisResult {
        var article = request.article.trim()
        require(article.length <= 4000) { "기사가 너무 깁니다." }
        return articleAnalysisService.analyze(article)
    }


    @PostMapping("/word-info-batch")
    suspend fun getWordInfoBatch(
        @RequestBody request: WordBatchRequest
    ): List<WordDescription> {
        println("-------------------")
        return wordInfoBatchService.execute(request.words)
    }

}

