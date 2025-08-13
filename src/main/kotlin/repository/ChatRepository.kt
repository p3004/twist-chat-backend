package chat.twist.com.repository

import chat.twist.com.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*

class ChatRepository {
    private val userRepository = UserRepository()
    
    // Create a new chat
    fun createChat(chat: Chat, memberIds: List<String>): String = transaction {
        // Insert the chat
        val chatId = ChatTable.insertAndGetId {
            it[isGroup] = chat.isGroup
            it[name] = chat.name
            it[createdBy] = UUID.fromString(chat.createdBy)
            it[mode] = chat.mode
        }.value.toString()
        
        // Add members to the chat
        memberIds.forEach { memberId ->
            ChatMemberTable.insert {
                it[ChatMemberTable.chatId] = UUID.fromString(chatId)
                it[userId] = UUID.fromString(memberId)
            }
        }
        
        chatId
    }
    
    // Get chat by ID with members
    fun getChatById(chatId: String): ChatWithMembers? = transaction {
        val chat = ChatTable.selectAll().where { ChatTable.id eq UUID.fromString(chatId) }
            .map { rowToChat(it) }
            .singleOrNull() ?: return@transaction null
        
        val members = ChatMemberTable.join(UserTable, JoinType.INNER, 
            additionalConstraint = { ChatMemberTable.userId eq UserTable.id })
            .selectAll().where { ChatMemberTable.chatId eq UUID.fromString(chatId) }
            .map { userRepository.rowToUser(it) }
        
        ChatWithMembers(chat, members)
    }
    
    // Get all chats for a user
    fun getUserChats(userId: String): List<ChatWithMembers> = transaction {
        // Find all chats where the user is a member
        val chatIds = ChatMemberTable.selectAll()
            .where { ChatMemberTable.userId eq UUID.fromString(userId) }
            .map { it[ChatMemberTable.chatId].value.toString() }
        
        // For each chat, get the chat details and members
        chatIds.mapNotNull { getChatById(it) }
    }
    
    // Add a user to a chat
    fun addUserToChat(chatId: String, userId: String): Boolean = transaction {
        try {
            ChatMemberTable.insert {
                it[ChatMemberTable.chatId] = UUID.fromString(chatId)
                it[ChatMemberTable.userId] = UUID.fromString(userId)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // Remove a user from a chat
    fun removeUserFromChat(chatId: String, userId: String): Boolean = transaction {
        ChatMemberTable.deleteWhere { 
            (ChatMemberTable.chatId eq UUID.fromString(chatId)) and
            (ChatMemberTable.userId eq UUID.fromString(userId))
        } > 0
    }
    
    // Check if a chat exists between two users
    fun getDirectChatBetweenUsers(user1Id: String, user2Id: String): String? = transaction {
        // Find all chats where user1 is a member
        val user1Chats = ChatMemberTable.selectAll()
            .where { ChatMemberTable.userId eq UUID.fromString(user1Id) }
            .map { it[ChatMemberTable.chatId] }
        
        // Find all chats where user2 is a member
        val user2Chats = ChatMemberTable.selectAll()
            .where { ChatMemberTable.userId eq UUID.fromString(user2Id) }
            .map { it[ChatMemberTable.chatId] }
        
        // Find the intersection (chats where both users are members)
        val commonChats = user1Chats.intersect(user2Chats.toSet())
        
        // Check if any of these common chats are direct (non-group) chats
        val directChat = ChatTable.selectAll()
            .where { (ChatTable.id inList commonChats) and (ChatTable.isGroup eq false) }
            .map { it[ChatTable.id].value.toString() }
            .firstOrNull()
        
        directChat
    }
    
    // Helper function to convert ResultRow to Chat
    private fun rowToChat(row: ResultRow): Chat {
        return Chat(
            chatId = row[ChatTable.id].value.toString(),
            isGroup = row[ChatTable.isGroup],
            name = row[ChatTable.name],
            createdBy = row[ChatTable.createdBy].value.toString(),
            createdAt = row[ChatTable.createdAt].toString(),
            mode = row[ChatTable.mode]
        )
    }
}