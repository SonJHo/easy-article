package simple.articles.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.stereotype.Service
import simple.articles.WordDescription
import simple.articles.cache.CacheKey
import simple.articles.cache.CacheTTL
import simple.articles.cache.CacheUtils
import simple.articles.cache.CacheWordInfoRepository
import simple.articles.service.gpt.GptService
import simple.articles.service.naver.NaverApiService

@Service
class WordInfoBatchService(
    private val cacheUtils: CacheUtils,
    private val cacheWordInfoRepository: CacheWordInfoRepository,
    private val naverApiService: NaverApiService,
    private val gptService: GptService
) {

    suspend fun execute(words: List<String>): List<WordDescription> = coroutineScope {
        val wordInfoMap = loadNaverWordInfo(words)

        // 2GPT 캐시 조회
        val cachedGptResults =
            cacheWordInfoRepository.getCachedWordDescriptions(CacheKey.GPT_WORD, words)

        val gptTargetWords =
            words.filterNot { cachedGptResults.containsKey(it) }

        // GPT 배치 호출
        val fetchedResults =
            fetchGptBatchResults(gptTargetWords, wordInfoMap)

        // 결과 병합
        mergeResults(cachedGptResults.toMutableMap(), fetchedResults)
    }

    /* =========================
       1️⃣ 네이버 캐시 + API
       ========================= */
    private suspend fun loadNaverWordInfo(
        words: List<String>
    ): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (word in words) {
            val cached =
                cacheUtils.get(CacheKey.NAVER_WORD, word)
            val description = cached ?: run {
                val fetched = try {
                    naverApiService.getWordInfoResponse(word)
                } catch (e: Exception) {
                    "❌ 네이버 조회 실패"
                }

                cacheUtils.set(
                    CacheKey.NAVER_WORD,
                    word,
                    fetched,
                    CacheTTL.NAVER_WORD_INFO
                )
                fetched
            }
            result[word] = description
        }

        return result
    }

    /* =========================
       2️⃣ GPT 배치 처리
       ========================= */
    private suspend fun fetchGptBatchResults(
        gptTargetWords: List<String>,
        wordInfoMap: Map<String, String>
    ): List<WordDescription> = coroutineScope {

        val semaphore = Semaphore(5)
        val batchSize = 5

        gptTargetWords.chunked(batchSize).map { batch ->
            async(Dispatchers.IO) {
                semaphore.withPermit {

                    val batchTexts =
                        batch.mapNotNull { wordInfoMap[it] }

                    if (batchTexts.isEmpty())
                        return@withPermit emptyList()

                    runCatching {
                        gptService.getWordInfoBatchResponse(batchTexts)
                    }.getOrElse {
                        List(batch.size) { "GPT fail" }
                    }.zip(batch).map { (text, word) ->

                        cacheUtils.set(
                            CacheKey.GPT_WORD,
                            word,
                            text,
                            CacheTTL.GPT_WORD_INFO
                        )

                        WordDescription(
                            word = word,
                            description = text.substringAfter(":", text).trim()
                        )
                    }
                }
            }
        }.awaitAll().flatten()
    }

    /* =========================
       3️⃣ 결과 병합
       ========================= */
    private fun mergeResults(
        cached: MutableMap<String, WordDescription>,
        fetched: List<WordDescription>
    ): List<WordDescription> {

        fetched.forEach { cached[it.word] = it }
        return cached.values.toList()
    }
}
