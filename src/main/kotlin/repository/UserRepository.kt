package chat.twist.com.repository

import chat.twist.com.configure.ConfigureUserNameBloomFilter
import chat.twist.com.model.User
import chat.twist.com.model.UserTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class UserRepository {
    
    // Create a new user
    fun upsertUser(user: User): String = transaction {
        if (user.userId.isNullOrBlank()) {
            UserTable.insertAndGetId {
                it[firebaseUid] = user.firebaseUid
                it[userName] = user.userName
                it[email] = user.email
                it[displayName] = user.displayName
                it[avatarUrl] = user.avatarUrl
            }.value.toString()
        } else {
            UserTable.update({ UserTable.id eq UUID.fromString(user.userId) }) {
                it[firebaseUid] = user.firebaseUid
                it[userName] = user.userName
                it[email] = user.email
                it[displayName] = user.displayName
                it[avatarUrl] = user.avatarUrl
            }
            user.userId
        }
    }
    
    // Get user by ID
    fun getUserById(userId: String): User? = transaction {
        UserTable.select { UserTable.id eq UUID.fromString(userId) }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    // Get user by Firebase UID
    fun getUserByFirebaseUid(firebaseUid: String): User? = transaction {
        UserTable.select { UserTable.firebaseUid eq firebaseUid }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    // Get user by username
    fun getUserByUsername(username: String): User? = transaction {
        UserTable.select { UserTable.userName eq username }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    // Get user by email
    fun getUserByEmail(email: String): User? = transaction {
        UserTable.select { UserTable.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    // Get all users
    fun getAllUsers(): List<User> = transaction {
        UserTable.selectAll()
            .map { rowToUser(it) }
    }
    
    // Delete user
    fun deleteUser(userId: String): Boolean = transaction {
        UserTable.deleteWhere { UserTable.id eq UUID.fromString(userId) } > 0
    }
    
    // Check if username exists
    fun usernameExists(username: String): Boolean = transaction {
        ConfigureUserNameBloomFilter.mightContain(username)
    }

    fun isUsernameUnique(username: String): Boolean {
        return if (!usernameExists(username)) {
            true
        } else {
            // Might be a false positive, verify with DB
            transaction {
                UserTable.selectAll()
                    .where { UserTable.userName eq username }
                    .empty()
            }
        }
    }
    
    // Check if email exists
    fun emailExists(email: String): Boolean = transaction {
        UserTable.select { UserTable.email eq email }.count() > 0
    }
    
    // Check if Firebase UID exists
    fun firebaseUidExists(firebaseUid: String): Boolean = transaction {
        UserTable.select { UserTable.firebaseUid eq firebaseUid }.count() > 0
    }
    
    // Search users by display name
    fun searchUsersByDisplayName(query: String): List<User> = transaction {
        UserTable.select { UserTable.displayName like "%$query%" }
            .map { rowToUser(it) }
    }
    
    // Helper function to convert ResultRow to User
    private fun rowToUser(row: ResultRow): User {
        return User(
            userId = row[UserTable.id].toString(),
            firebaseUid = row[UserTable.firebaseUid],
            userName = row[UserTable.userName],
            displayName = row[UserTable.displayName],
            avatarUrl = row[UserTable.avatarUrl],
            email = row[UserTable.email]
        )
    }
}