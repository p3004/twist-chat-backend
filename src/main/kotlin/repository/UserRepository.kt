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
        UserTable.selectAll().where { UserTable.id eq UUID.fromString(userId) }
            .map { rowToUser(it) }
            .singleOrNull()
    }
    
    // Delete user
    fun deleteUser(userId: String): Boolean = transaction {
        UserTable.deleteWhere { UserTable.id eq UUID.fromString(userId) } > 0
    }
    
    // Check if username exists
    fun usernameExists(username: String): Boolean = transaction {
        ConfigureUserNameBloomFilter.mightContain(username)
    }
    
    // Search users by multiple criteria (username, display name, or email)
    fun searchUsers(query: String, limit: Int = 20, offset: Int = 0): List<User> = transaction {
        // Create a query with OR conditions for all searchable fields
        val searchQuery = Op.build {
            (UserTable.userName like "%$query%") or
            (UserTable.displayName like "%$query%") or
            (UserTable.email like "%$query%")
        }
        
        UserTable.selectAll().where(searchQuery)
            .orderBy(UserTable.displayName)
            .limit(limit, offset.toLong())
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