package chat.twist.com.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.math.ceil
import kotlin.math.ln
import kotlin.math.pow

class SimpleBloomFilter private constructor(
    private val bitSize: Int,
    private val hashFunctionsCount: Int,
    private val bitSet: BitSet
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    constructor(expectedInsertions: Int, falsePositiveRate: Double = 0.01) : this(
        bitSize = ceil(-(expectedInsertions * ln(falsePositiveRate)) / ln(2.0).pow(2.0)).toInt(),
        hashFunctionsCount = ceil((-(expectedInsertions * ln(falsePositiveRate)) / ln(2.0).pow(2.0)) / expectedInsertions.toDouble() * ln(2.0)).toInt(),
        bitSet = BitSet(
            ceil(-(expectedInsertions * ln(falsePositiveRate)) / ln(2.0).pow(2.0)).toInt()
        )
    )

    @Synchronized
    fun put(item: String) {
        val hashes = getHashes(item)
        for (hash in hashes) {
            bitSet.set(hash)
        }
    }

    fun putAsync(item: String) {
        scope.launch {
            put(item)
        }
    }

    @Synchronized
    fun mightContain(item: String): Boolean {
        val hashes = getHashes(item)
        return hashes.all { bitSet[it] }
    }

    private fun getHashes(item: String): List<Int> {
        val bytes = item.toByteArray(StandardCharsets.UTF_8)
        val hash1 = MurmurHash3.hash32(bytes, 0)
        val hash2 = MurmurHash3.hash32(bytes, 157)
        return List(hashFunctionsCount) { i ->
            val combined = (hash1 + i * hash2) % bitSize
            Math.floorMod(combined, bitSize)
        }
    }

    fun saveTo(filePath: String) {
        synchronized(this) {
            ObjectOutputStream(File(filePath).outputStream()).use { out ->
                out.writeInt(bitSize)
                out.writeInt(hashFunctionsCount)
                out.writeObject(bitSet)
            }
        }
    }

    companion object {
        fun loadFrom(filePath: String): SimpleBloomFilter {
            ObjectInputStream(File(filePath).inputStream()).use { input ->
                val size = input.readInt()
                val hashes = input.readInt()
                val bits = input.readObject() as BitSet
                return SimpleBloomFilter(size, hashes, bits)
            }
        }
    }
}
