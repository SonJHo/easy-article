package simple.articles.controller

import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import simple.articles.service.AnalysisResult
import simple.articles.service.ArticleAnalysisService

@Controller
class ArticleController {

    // index 페이지
    @GetMapping("/")
    fun index(): String = "index"

    // result 페이지 렌더링
    @PostMapping("/result")
    fun showResult(@RequestParam article: String, model: Model): String {
        if (article.length > 4000) {
            model.addAttribute("error", "기사가 너무 깁니다.")
            return "index"
        }

        model.addAttribute("originalArticle", article)
        return "result" // result.html 렌더링
    }


}

// JSON 요청 객체
