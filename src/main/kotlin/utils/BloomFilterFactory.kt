package chat.twist.com.utils

import java.io.File

object BloomFilterFactory {
    fun createOrLoad(path: String, expectedInsertions: Int, fpp: Double): SimpleBloomFilter {
        return if (File(path).exists()) {
            SimpleBloomFilter.loadFrom(path)
        } else {
            SimpleBloomFilter(expectedInsertions, fpp)
        }
    }
}