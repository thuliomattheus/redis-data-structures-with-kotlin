import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool
import redis.clients.jedis.params.ZParams

class ManipulatingSortedSets {

    private val connection = JedisPool("localhost", 6379)

    private val foods = HashMap<String, Double>().apply {
        this["pizza"] = 8.2
        this["hot dog"] = 4.5
        this["cake"] = 4.0
        this["barbecue"] = 8.0
    }

    @BeforeEach
    fun setup() {
        connection.getResource().use { redis ->
            redis.zadd("best-foods", foods)
        }
    }

    @AfterEach
    fun tearDown() {
        connection.getResource().use { redis ->
            redis.del("best-foods")
        }
    }

    @Test
    fun iteratingOverRedisSortedSet() {
        connection.getResource().use { redis ->
            assertEquals(listOf("cake"), redis.zrange("best-foods", 0, 0))
            assertEquals(listOf("pizza"), redis.zrevrange("best-foods", 0, 0))
            assertEquals(2, redis.zrangeByScore("best-foods", 5.0, 10.0).size)
        }
    }

    @Test
    fun checkingLengthOfRedisSortedSet() {
        connection.getResource().use { redis ->
            assertEquals(4, redis.zcard("best-foods"))
        }
    }

    @Test
    fun checkingHowManyElementsOwnGoodScoresWithinRedisSortedSet() {
        connection.getResource().use { redis ->
            assertEquals(2, redis.zcount("best-foods", 7.0, 10.0))
        }
    }

    @Test
    fun verifyingRankOfElementWithinARedisSortedSet() {
        connection.getResource().use { redis ->
            System.err.println(redis.zrangeWithScores("best-foods", 0, -1))
            assertEquals(0, redis.zrevrank("best-foods", "pizza"))
            assertEquals(1, redis.zrank("best-foods", "hot dog"))
        }
    }

    @Test
    fun verifyingScoreOfElementWithinARedisSortedSet() {
        connection.getResource().use { redis ->
            assertEquals(foods["hot dog"]!!, redis.zscore("best-foods", "hot dog"))
            assertEquals(foods["pizza"]!!, redis.zscore("best-foods", "pizza"))
        }
    }

    @Test
    fun incrementingScoreOfElementsWithinRedisSortedSet() {
        connection.getResource().use { redis ->
            val pizzaIncrement = 1.0
            val cakeDecrement = -0.5

            redis.zincrby("best-foods", pizzaIncrement, "pizza")
            redis.zincrby("best-foods", cakeDecrement, "cake")

            assertEquals(foods["pizza"]!! + pizzaIncrement, redis.zscore("best-foods", "pizza"))
            assertEquals(foods["cake"]!! + cakeDecrement, redis.zscore("best-foods", "cake"))
        }
    }

    @Test
    fun removingElementsFromRedisSortedSet() {
        connection.getResource().use { redis ->
            assertEquals(2, redis.zremrangeByScore("best-foods", 0.0, 5.0))
            assertEquals(1, redis.zremrangeByRank("best-foods", -1, -1))
            assertEquals(1, redis.zrem("best-foods", "barbecue"))
            assertEquals(0, redis.zrem("best-foods", "pie"))
        }
    }

    @Test
    fun retrievingUnionBetweenSets() {
        connection.getResource().use { redis ->
            val snacks = HashMap<String, Double>().apply {
                this["mini pizza"] = 6.0
                this["pastry"] = 7.5
                this["cheese ball"] = 8.0
                this["mini pie"] = 9.5
            }
            val sweets = HashMap<String, Double>().apply {
                this["truffle"] = 9.0
                this["cupcake"] = 3.5
                this["lollipop"] = 3.0
                this["candy"] = 7.0
            }
            redis.zadd("sweets", sweets)
            redis.zadd("snacks", snacks)
            redis.zunionstore("light-foods", "sweets", "snacks")

            assertEquals(
                8,
                redis.zunion(ZParams().aggregate(ZParams.Aggregate.SUM), "light-foods", "sweets", "snacks").size
            )
            assertEquals(0, redis.zrevrank("light-foods", "mini pie"))

            redis.del("sweets")
            redis.del("snacks")
            redis.del("light-foods")
        }
    }

    @Test
    fun retrievingIntersectionBetweenSets() {
        connection.getResource().use { redis ->
            val worldCups = HashMap<String, Double>().apply {
                this["brazil"] = 5.0
                this["germany"] = 4.0
                this["italy"] = 4.0
                this["argentina"] = 3.0
            }
            val southAmericanCups = HashMap<String, Double>().apply {
                this["argentina"] = 15.0
                this["uruguay"] = 15.0
                this["brazil"] = 9.0
                this["chile"] = 2.0
            }
            redis.zadd("world-cups", worldCups)
            redis.zadd("south-american-cups", southAmericanCups)
            redis.zunionstore("titles", "world-cups", "south-american-cups")

            assertEquals(
                2,
                redis.zinter(
                    ZParams().aggregate(ZParams.Aggregate.SUM), "titles", "world-cups", "south-american-cups"
                ).size
            )
            assertEquals(
                southAmericanCups["argentina"]!! + worldCups["argentina"]!!,
                redis.zscore("titles", "argentina")
            )

            redis.del("world-cups")
            redis.del("south-american-cups")
            redis.del("titles")
        }
    }

    @Test
    fun retrievingDifferenceBetweenSets() {
        connection.getResource().use { redis ->
            val worldCups = HashMap<String, Double>().apply {
                this["england"] = 1.0
                this["france"] = 2.0
                this["spain"] = 1.0
            }
            val euroCups = HashMap<String, Double>().apply {
                this["netherlands"] = 1.0
                this["france"] = 2.0
                this["portugal"] = 3.0
                this["spain"] = 3.0
            }
            redis.zadd("world-cups", worldCups)
            redis.zadd("euro-cups", euroCups)
            redis.zdiffStore("titles", "world-cups", "euro-cups")

            assertEquals(1, redis.zdiff("world-cups", "euro-cups").size)
            assertEquals(2, redis.zdiff("euro-cups", "world-cups").size)
            assertEquals(worldCups["england"]!!, redis.zscore("titles", "england"))

            redis.del("world-cups")
            redis.del("euro-cups")
            redis.del("titles")
        }
    }
}
