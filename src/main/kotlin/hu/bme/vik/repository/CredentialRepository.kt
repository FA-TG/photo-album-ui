package hu.bme.vik.repository

import com.mongodb.client.model.IndexOptions
import hu.bme.vik.model.User
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.mindrot.jbcrypt.BCrypt

class CredentialRepository(
    database: CoroutineDatabase,
    collectionName: String
) {
    private val collection = database.getCollection<User>(collectionName)

    init {
        runBlocking {
            collection.ensureIndex(User::username, indexOptions = IndexOptions().unique(true))
        }
    }

    suspend fun createUser(username: String, password: String) {
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = User(username, hashedPassword)
        collection.insertOne(user)
    }

    suspend fun findUserByUsername(username: String): User? {
        return collection.findOne(User::username eq username)
    }

    fun verifyPassword(plainTextPassword: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(plainTextPassword, hashedPassword)
    }
}