package simple.articles.cache

import org.springframework.stereotype.Component
import simple.articles.WordDescription



@Component
class CacheWordInfoRepository(
    private val cacheUtils: CacheUtils
) {
    fun getCachedWordDescriptions(
        key: CacheKey,
        words: List<String>
    ): Map<String, WordDescription> =
        words.mapNotNull { word ->
            cacheUtils.get(key, word)?.let { cached ->
                word to WordDescription(
                    word = word,
                    description = cached.substringAfter(":", cached).trim()
                )
            }
        }.toMap()

    fun getCachedRaw(
        key: CacheKey,
        words: List<String>
    ): Map<String, String> =
        words.mapNotNull { word ->
            cacheUtils.get(key, word)?.let { cached ->
                word to cached
            }
        }.toMap()
}