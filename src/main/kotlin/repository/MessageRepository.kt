package chat.twist.com.repository

import chat.twist.com.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class MessageRepository {
    private val userRepository = UserRepository()
    
    // Send a new message
    fun sendMessage(message: Message): String = transaction {
        MessageTable.insertAndGetId {
            it[chatId] = UUID.fromString(message.chatId)
            it[senderId] = UUID.fromString(message.senderId)
            it[content] = message.content
            it[messageType] = message.messageType
            it[isDeleted] = message.isDeleted
            // Handle nullable fields
            message.expiresAt?.let { expiresAt -> it[MessageTable.expiresAt] = java.time.Instant.parse(expiresAt) }
            message.extra?.let { extra -> it[MessageTable.extra] = extra }
        }.value.toString()
    }
    
    // Get messages for a chat
    fun getChatMessages(chatId: String, limit: Int = 50, offset: Int = 0): List<Message> = transaction {
        MessageTable.join(UserTable, JoinType.INNER, 
            additionalConstraint = { MessageTable.senderId eq UserTable.id })
            .selectAll()
            .where { MessageTable.chatId eq UUID.fromString(chatId) }
            .orderBy(MessageTable.createdAt, SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map { 
                val message = rowToMessage(it)
                message.copy(sender = userRepository.rowToUser(it))
            }
    }
    
    // Delete a message (soft delete)
    fun deleteMessage(messageId: String): Boolean = transaction {
        MessageTable.update({ MessageTable.id eq UUID.fromString(messageId) }) {
            it[isDeleted] = true
        } > 0
    }
    
    // Helper function to convert ResultRow to Message
    private fun rowToMessage(row: ResultRow): Message {
        return Message(
            messageId = row[MessageTable.id].value.toString(),
            chatId = row[MessageTable.chatId].value.toString(),
            senderId = row[MessageTable.senderId].value.toString(),
            content = row[MessageTable.content],
            messageType = row[MessageTable.messageType],
            isDeleted = row[MessageTable.isDeleted],
            expiresAt = row[MessageTable.expiresAt]?.toString(),
            extra = row[MessageTable.extra]
        )
    }
}