package simple.articles.cache
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisCacheStore(
    private val redisTemplate: StringRedisTemplate
) : CacheStore {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun get(key: String): String? =
        try {
            val value = redisTemplate.opsForValue().get(key)

            if (value != null) {
                log.debug("✅ Redis CACHE HIT - key={}", key)
            } else {
                log.debug("❌ Redis CACHE MISS - key={}", key)
            }

            value
        } catch (e: Exception) {
            log.warn("🚨 Redis GET 실패 - key={}", key, e)
            null
        }

    override fun set(key: String, value: String, ttlSeconds: Long) {
        try {
            redisTemplate.opsForValue()
                .set(key, value, Duration.ofSeconds(ttlSeconds))

            log.debug("📝 Redis CACHE SET - key={}, ttl={}s", key, ttlSeconds)
        } catch (e: Exception) {
            log.warn("🚨 Redis SET 실패 - key={}", key, e)
        }
    }
}

enum class CacheKey(val prefix: String) {
    NAVER_WORD("naver:word"),
    GPT_WORD("gpt:word")
}

object CacheTTL {
    const val NAVER_WORD_INFO = 60 * 60 * 24L      // 1일
    const val GPT_WORD_INFO   = 60 * 60 * 24 * 7L  // 7일
}