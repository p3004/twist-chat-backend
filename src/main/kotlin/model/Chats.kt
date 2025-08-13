package chat.twist.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.time
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant

object ChatTable: UUIDTable("public.chats") {
    val isGroup = bool("is_group")
    val name = varchar("name", 128).nullable()
    val createdBy = reference("created_by", UserTable)
    val createdAt = timestamp("created_at")
    val mode = varchar("mode", 32).default("normal")
}

object ChatMemberTable: Table("public.chat_members") {
    val chatId = reference("chat_id", ChatTable)
    val userId = reference("user_id", UserTable)

    override val primaryKey = PrimaryKey(chatId, userId, name = "PK")
}

@Serializable
data class Chat(
    @SerialName("id")
    val chatId: String?,
    @SerialName("is_group")
    val isGroup: Boolean,
    @SerialName("name")
    val name: String?,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("mode")
    val mode: String = "normal"
)

@Serializable
data class ChatMember(
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("joined_at")
    val joinedAt: String? = null
)

@Serializable
data class ChatWithMembers(
    val chat: Chat,
    val members: List<User>
)

@Serializable
data class ChatList(
    val chats: List<ChatWithMembers>
)