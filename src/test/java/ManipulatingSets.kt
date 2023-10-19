import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool

class ManipulatingSets {

    private val connection = JedisPool("localhost", 6379)

    @Test
    fun addingValuesOnRedisSet() {
        connection.getResource().use { redis ->
            assertEquals(4, redis.sadd("Chelsea:Legends",
                "Petr Cech", "John Terry", "Frank Lampard", "Didier Drogba",
            ))

            redis.del("Chelsea:Legends")
        }
    }

    @Test
    fun iteratingOverRedisSet() {
        connection.getResource().use { redis ->
            redis.sadd("marine-animals", "fish", "dolphin", "shark", "octopus", "jellyfish")

            assertEquals(5, redis.smembers("marine-animals").size)

            redis.del("marine-animals")
        }
    }

    @Test
    fun checkingLengthOfRedisSet() {
        connection.getResource().use { redis ->
            redis.sadd("farm-animals", "horse", "chicken", "pig", "cow", "rabbit")

            assertEquals(5, redis.scard("farm-animals"))

            redis.del("farm-animals")
        }
    }

    @Test
    fun verifyingIfAnElementAreWithinARedisSet() {
        connection.getResource().use { redis ->
            redis.sadd("domestic-animals", "cat", "dog", "bird", "hamster")

            assertTrue(redis.sismember("domestic-animals", "cat"))
            assertFalse(redis.sismember("domestic-animals", "tiger"))

            redis.del("domestic-animals")
        }
    }

    @Test
    fun removingElementsFromRedisSet() {
        connection.getResource().use { redis ->
            redis.sadd("flying-animals", "eagle", "falcon", "crow", "swan", "pigeon")

            redis.srem("flying-animals", "swan")

            assertEquals(4, redis.scard("flying-animals"))

            redis.del("flying-animals")
        }
    }

    @Test
    fun removingRandomElementsFromRedisSet() {
        connection.getResource().use { redis ->
            redis.sadd("wild-animals", "lion", "tiger", "zebra", "giraffe", "elephant")

            redis.spop("wild-animals", 2)

            assertEquals(3, redis.scard("wild-animals"))

            redis.del("wild-animals")
        }
    }

    @Test
    fun retrievingUnionBetweenSets() {
        connection.getResource().use { redis ->
            redis.sadd("primary-colors", "blue", "red", "yellow")
            redis.sadd("secondary-colors", "orange", "green", "violet")



            assertEquals(6, redis.sunion("primary-colors", "secondary-colors").size)

            redis.del("primary-colors")
            redis.del("secondary-colors")
        }
    }

    @Test
    fun retrievingIntersectionBetweenSets() {
        connection.getResource().use { redis ->
            redis.sadd("colors", "blue", "red", "yellow", "orange")
            redis.sadd("fruits", "melon", "apple", "orange")

            assertEquals(setOf("orange"), redis.sinter("colors", "fruits"))

            redis.del("colors")
            redis.del("fruits")
        }
    }

    @Test
    fun retrievingDifferenceBetweenSets() {
        connection.getResource().use { redis ->
            redis.sadd("mammal-animals", "dog", "cat", "bull", "dolphin", "whale", "bat")
            redis.sadd("flying-animals", "hawk", "bat", "eagle", "owl")

            assertEquals(
                setOf("dog", "cat", "bull", "dolphin", "whale"),
                redis.sdiff("mammal-animals", "flying-animals"),
            )
            assertEquals(
                setOf("hawk", "eagle", "owl"),
                redis.sdiff("flying-animals", "mammal-animals"),
            )

            redis.del("mammal-animals")
            redis.del("flying-animals")
        }
    }
}
