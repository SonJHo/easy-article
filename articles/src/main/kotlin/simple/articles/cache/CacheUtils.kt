package simple.articles.cache

import org.springframework.stereotype.Component

@Component
 class CacheUtils(
    private val cache: CacheStore,
) {

    fun get(type: CacheKey, key: String): String? =
        cache.get("${type.prefix}:$key")

    fun set(
        type: CacheKey,
        key: String,
        value: String,
        ttlSeconds: Long,
    ) {
        cache.set(
            key = "${type.prefix}:$key",
            value = value,
            ttlSeconds = ttlSeconds
        )
    }
}
