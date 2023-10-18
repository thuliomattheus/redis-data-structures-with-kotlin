import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool

class ManipulatingHashes {

    private val connection = JedisPool("localhost", 6379)

    @Test
    fun addingValuesUsingIndividualsFields() {
        connection.getResource().use { redis ->
            redis.hset("Chelsea:2023:10", "name", "Mykhaylo Mudryk")
            redis.hset("Chelsea:2023:10", "nationality", "Ukraine")
            redis.hset("Chelsea:2023:10", "position", "left winger")

            assertEquals("Mykhaylo Mudryk", redis.hget("Chelsea:2023:10", "name"))
            assertEquals("Ukraine", redis.hget("Chelsea:2023:10", "nationality"))
            assertEquals("left winger", redis.hget("Chelsea:2023:10", "position"))
        }
    }

    @Test
    fun addingValuesUsingHashMap() {
        connection.getResource().use { redis ->
            val reeceJames = HashMap<String, String>().apply {
                this["name"] = "Reece James"
                this["nationality"] = "England"
                this["position"] = "right wing-back"
                this["captain"] = "true"
            }

            redis.hset("Chelsea:2023:24", reeceJames)

            assertEquals("Reece James", redis.hget("Chelsea:2023:24", "name"))
            assertEquals("England", redis.hget("Chelsea:2023:24", "nationality"))
            assertEquals("right wing-back", redis.hget("Chelsea:2023:24", "position"))
            assertEquals("true", redis.hget("Chelsea:2023:24", "captain"))
        }
    }

    @Test
    fun checkingIfTheseKeysExistInRedisHash() {
        connection.getResource().use { redis ->
            assertTrue(redis.hexists("Chelsea:2023:24", "name"))
            assertTrue(redis.hexists("Chelsea:2023:24", "position"))
            assertTrue(redis.hexists("Chelsea:2023:24", "captain"))
            assertTrue(redis.hexists("Chelsea:2023:10", "name"))
            assertTrue(redis.hexists("Chelsea:2023:10", "position"))
            assertFalse(redis.hexists("Chelsea:2023:10", "captain"))
            assertFalse(redis.hexists("Chelsea:2023:10", "market value"))
        }
    }

    @Test
    fun removingInvalidKeysFromRedisHash() {
        connection.getResource().use { redis ->
            redis.hset("best-cars-to-drive:hb20", "year", "2023")
            redis.hset("best-cars-to-drive:hb20", "color", "black")
            redis.hset("best-cars-to-drive:hb20", "transmission", "automatic")
            redis.hset("best-cars-to-drive:hb20", "ease of selling", "hardest")

            assertTrue(redis.hdel("best-cars-to-drive:hb20", "ease of selling") == 1L)

            assertTrue(redis.hdel("best-cars-to-drive:hb20", "prize of car of the year") == 0L)
        }
    }

    @Test
    fun verifyingKeysFromRedisHash() {
        connection.getResource().use { redis ->
            assertTrue(redis.hkeys("Chelsea:2023:24").containsAll(listOf("name", "position", "nationality", "captain")))
            assertTrue(redis.hkeys("Chelsea:2023:10").containsAll(listOf("name", "position", "nationality")))
        }
    }

    @Test
    fun incrementingValuesForRedisHashes() {
        connection.getResource().use { redis ->
            redis.hset("Chelsea:2023", "trophies", "0")
            redis.hset("Vasco:2023", "trophies", "0.5")

            redis.hincrBy("Chelsea:2023", "trophies", 1)
            redis.hincrByFloat("Vasco:2023", "trophies", -0.5)

            assertEquals("1", redis.hget("Chelsea:2023", "trophies"))
            assertEquals("0", redis.hget("Vasco:2023", "trophies"))
        }
    }
}
