package simple.articles

data class ArticleRequest(val article: String)
data class WordBatchRequest(val words: List<String>)
data class WordDescription(val word: String, val description: String)
