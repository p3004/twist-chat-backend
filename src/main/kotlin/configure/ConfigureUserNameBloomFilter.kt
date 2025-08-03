package chat.twist.com.configure

import chat.twist.com.model.UserTable
import chat.twist.com.utils.BloomFilterFactory
import chat.twist.com.utils.SimpleBloomFilter
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ConfigureUserNameBloomFilter {
    private const val FILE_PATH = "username.bloom"
    private const val EXPECTED_INSERTIONS = 100_000
    private const val FALSE_POSITIVE_RATE = 0.001

    private val bloomFilter: SimpleBloomFilter = BloomFilterFactory.createOrLoad(
        FILE_PATH,
        EXPECTED_INSERTIONS,
        FALSE_POSITIVE_RATE
    )

    fun initializeFromDatabase() {
        transaction {
            UserTable.selectAll().forEach {
                val username = it[UserTable.userName]
                println("userName in bloom filter: $username")
                bloomFilter.putAsync(username) // non-blocking insert
            }
        }
    }

    fun saveToDisk() {
        bloomFilter.saveTo(FILE_PATH)
    }

    fun mightContain(username: String): Boolean {
        return bloomFilter.mightContain(username)
    }

    fun addUsername(username: String) {
        bloomFilter.put(username)
    }
}