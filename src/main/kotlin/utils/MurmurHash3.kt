package chat.twist.com.utils

object MurmurHash3 {
    fun hash32(data: ByteArray, seed: Int = 0): Int {
        var h1 = seed
        val c1 = -0x3361d2af
        val c2 = 0x1b873593
        val len = data.size
        val roundedEnd = len and 0xfffffffc.toInt()

        for (i in 0 until roundedEnd step 4) {
            var k1 = (data[i].toInt() and 0xff) or
                    ((data[i + 1].toInt() and 0xff) shl 8) or
                    ((data[i + 2].toInt() and 0xff) shl 16) or
                    ((data[i + 3].toInt() and 0xff) shl 24)

            k1 *= c1
            k1 = Integer.rotateLeft(k1, 15)
            k1 *= c2

            h1 = h1 xor k1
            h1 = Integer.rotateLeft(h1, 13)
            h1 = h1 * 5 + 0xe6546b64.toInt()
        }

        var k1 = 0
        when (len and 3) {
            3 -> k1 = (data[roundedEnd + 2].toInt() and 0xff) shl 16
            2 -> k1 = k1 or ((data[roundedEnd + 1].toInt() and 0xff) shl 8)
            1 -> {
                k1 = k1 or (data[roundedEnd].toInt() and 0xff)
                k1 *= c1
                k1 = Integer.rotateLeft(k1, 15)
                k1 *= c2
                h1 = h1 xor k1
            }
        }

        h1 = h1 xor len
        h1 = h1 xor (h1 ushr 16)
        h1 *= -0x7a143595
        h1 = h1 xor (h1 ushr 13)
        h1 *= -0x3d4d51cb
        h1 = h1 xor (h1 ushr 16)
        return h1
    }
}
