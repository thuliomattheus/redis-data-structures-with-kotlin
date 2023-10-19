import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import redis.clients.jedis.JedisPool

class ManipulatingLists {

    private val connection = JedisPool("localhost", 6379)

    @Test
    fun addingValuesOnRedisList() {
        connection.getResource().use { redis ->
            assertEquals(11, redis.lpush("Chelsea:2012",
                "Petr Cech",
                "Branislav Ivanovic", "John Terry", "Gary Cahill", "Ashley Cole",
                "John Obi Mikel", "Ramires", "Frank Lampard", "Juan Mata",
                "Florent Malouda", "Didier Drogba",
            ))

            redis.del("Chelsea:2012")
        }
    }

    @Test
    fun updatingValueOnRedisList() {
        connection.getResource().use { redis ->
            redis.rpush("Best Sports", "Football", "VolleyBall")

            redis.lset("Best Sports", 1, "BasketBall")

            assertEquals("BasketBall", redis.lrange("Best Sports", 1, -1)[0])
            redis.del("Best Sports")
        }
    }

    @Test
    fun iteratingOverRedisList() {
        connection.getResource().use { redis ->
            redis.rpush("Chelsea:2021", "Edouard Mendy",
                "Reece James", "César Azpilicueta", "Thiago Silva", "Antonio Rudiger", "Ben Chilwell",
                "Jorginho", "N'Golo Kanté", "Mason Mount",
                "Kai Havertz", "Timo Werner",
            )

            assertEquals(listOf("Reece James"), redis.lrange("Chelsea:2021", 1, 1))
            assertEquals(listOf("Thiago Silva", "Antonio Rudiger"), redis.lrange("Chelsea:2021", 3, 4))
            assertEquals(listOf("Jorginho", "N'Golo Kanté"), redis.lrange("Chelsea:2021", -5, -4))

            redis.del("Chelsea:2021")
        }
    }

    @Test
    fun checkingLengthOfRedisList() {
        connection.getResource().use { redis ->
            redis.lpush("Flamengo:2019", "Diego Alves",
                "Rafinha", "Rodrigo Caio", "Pablo Marí", "Filipe Luís",
                "Willian Arão", "Gérson", "Giorgian De Arrascaeta",
                "Éverton Ribeiro", "Gabriel Barbosa", "Bruno Henrique",
            )
            redis.lpush("Flamengo:2019:reservas", "Diego Ribas", "Vitinho", "Rodinei", "Renê", "Berrío")

            assertEquals(11, redis.llen("Flamengo:2019"))
            assertEquals(5, redis.llen("Flamengo:2019:reservas"))

            redis.del("Flamengo:2019")
            redis.del("Flamengo:2019:reservas")
        }
    }

    @Test
    fun removingEdgeElementsFromRedisList() {
        connection.getResource().use { redis ->
            redis.lpush("Good Programming Languages", "JavaScript", "TypeScript", "Java", "Kotlin", "Python", "C#", "C++", "PHP")

            redis.lpop("Good Programming Languages", 2)
            redis.rpop("Good Programming Languages")

            assertEquals(5, redis.llen("Good Programming Languages"))

            redis.del("Good Programming Languages")
        }
    }

    @Test
    fun removingElementsFromRedisList() {
        connection.getResource().use { redis ->
            redis.rpush("Best Desserts", "Ice Cream", "Sundae", "Nutella", "Cartola", "Chocolate", "Finny", "Cookies")

            redis.ltrim("Best Desserts", 2, 4)

            assertEquals(3, redis.llen("Best Desserts"))
            assertEquals(listOf("Nutella", "Cartola", "Chocolate"), redis.lrange("Best Desserts", 0, -1))

            redis.del("Best Desserts")
        }
    }

    @Test
    fun removingDuplicatedElementsFromRedisList() {
        connection.getResource().use { redis ->
            redis.rpush("Great Places To Visit",
                "Cristo Redentor", "Statue of Liberty", "Tour Eiffel", "Pyramid of Giza", "Cristo Redentor",
                "Taj Mahal", "Cristo Redentor",
            )
            redis.rpush("Best Games To Play",
            "Far Cry 5", "Hollow Knight", "Tomb Raider", "Megaman X6", "Sea of Stars", "GTA San Andreas",
            "Minecraft", "Minecraft", "Minecraft", "Minecraft")

            redis.lrem("Great Places To Visit", 2, "Cristo Redentor")
            redis.lrem("Best Games To Play", 0, "Minecraft") // Remove all occurrences

            assertEquals(5, redis.llen("Great Places To Visit"))
            assertEquals(6, redis.llen("Best Games To Play"))

            redis.del("Great Places To Visit")
            redis.del("Best Games To Play")
        }
    }
}
