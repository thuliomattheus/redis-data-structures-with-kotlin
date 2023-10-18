import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.SetParams

class ManipulatingStrings {

    private val connection = JedisPool("localhost", 6379)

    @Test
    fun addingValues() {
        connection.getResource().use { redis ->
            assertEquals("OK", redis.set("best-team-in-premier-league", "Chelsea FC"))
            assertEquals("OK", redis.set("best-team-in-brasileirao", "CR Flamengo"))
            assertEquals("OK", redis.set("greatest-player-of-all-time", "Didier Drogba"))
        }
    }

    @Test
    fun retrievingValues() {
        connection.getResource().use { redis ->
            redis.set("best-team-in-premier-league", "Chelsea FC")
            redis.set("best-team-in-brasileirao", "CR Flamengo")
            redis.set("greatest-player-of-all-time", "Didier Drogba")

            assertEquals("Chelsea FC", redis.get("best-team-in-premier-league"))
            assertEquals("CR Flamengo", redis.get("best-team-in-brasileirao"))
            assertEquals("Didier Drogba", redis.get("greatest-player-of-all-time"))
        }
    }

    @Test
    fun checkingIfTheseKeysExistInRedis() {
        connection.getResource().use { redis ->
            assertTrue(redis.exists("best-team-in-premier-league"))
            assertTrue(redis.exists("best-team-in-brasileirao"))
            assertTrue(redis.exists("greatest-player-of-all-time"))
            assertFalse(redis.exists("best-team-in-la-liga"))
            assertFalse(redis.exists("best-team-in-ligue-1"))
        }
    }

    @Test
    fun removingInvalidKeysFromRedis() {
        connection.getResource().use { redis ->
            redis.set("best-team-in-the-premier-league", "Leicester")
            redis.set("best-team-in-the-brasileirao", "Internacional")
            redis.set("best-team-in-the-la-liga", "Valencia")

            assertTrue(redis.del("best-team-in-the-premier-league") == 1L)
            assertTrue(redis.del("best-team-in-the-brasileirao") == 1L)
            assertTrue(redis.del("best-team-in-the-la-liga") == 1L)

            assertTrue(redis.del("best-team-in-the-premier-league") == 0L)
            assertTrue(redis.del("best-team-in-the-brasileirao") == 0L)
            assertTrue(redis.del("best-team-in-the-la-liga") == 0L)
        }
    }

    @Test
    fun definingTTLForKeys() {
        connection.getResource().use { redis ->
            redis.set("key-with-2s-of-duration", "test", SetParams().ex(2))
            redis.set("key-with-2ms-of-duration", "test", SetParams().px(2))

            Thread.sleep(5)

            assertTrue(redis.exists("key-with-2s-of-duration"))
            assertFalse(redis.exists("key-with-2ms-of-duration"))
        }
    }

    @Test
    fun verifyingTTLForKeys() {
        connection.getResource().use { redis ->
            redis.set("key-with-5s-of-duration", "test", SetParams().ex(5))
            redis.set("key-with-5000ms-of-duration", "test", SetParams().px(5000))

            assertTrue(redis.ttl("key-with-5s-of-duration") > 0)
            assertTrue(redis.ttl("key-with-5000ms-of-duration") > 0)
        }
    }

    @Test
    fun definingInfiniteTTLForKeys() {
        connection.getResource().use { redis ->
            redis.set("key-with-5s-of-duration", "test", SetParams().ex(5))

            assertTrue(redis.ttl("key-with-5s-of-duration") > 0)

            redis.persist("key-with-5s-of-duration")

            assertEquals(-1L, redis.ttl("key-with-5s-of-duration"))
        }
    }

    @Test
    fun incrementingValuesForKeys() {
        connection.getResource().use { redis ->
            redis.set("titles-of-champions-league-of-Chelsea", "2")
            redis.set("titles-of-champions-league-of-PSG", "1")

            redis.incr("titles-of-champions-league-of-Chelsea")
            redis.incrBy("titles-of-champions-league-of-PSG", -1)

            assertEquals("3", redis.get("titles-of-champions-league-of-Chelsea"))
            assertEquals("0", redis.get("titles-of-champions-league-of-PSG"))
        }
    }
}
