package simple.articles.cache

interface CacheStore {
    fun get(key: String): String?
    fun set(key: String, value: String, ttlSeconds: Long)
}
