package chat.twist.com.model


import chat.twist.com.utils.MEDIA_TABLE_NAME
import chat.twist.com.utils.MESSAGES_TABLE_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.json.jsonb
import java.time.Instant

object MessageTable: UUIDTable(MESSAGES_TABLE_NAME) {
    val chatId = reference("chat_id", ChatTable)
    val senderId = reference("sender_id", UserTable)
    val content = text("content")
    val messageType = varchar("message_type", 32).default("text")
    val isDeleted = bool("is_deleted").default(false)
    val expiresAt = timestamp("expires_at").nullable()
    val createdAt = timestamp("created_at").default(Instant.now())
    val extra = jsonb(
        "extra",
        serialize = { it },       // storing String as-is
        deserialize = { it }      // reading String as-is
    ).nullable()
}

object MediaTable: UUIDTable(MEDIA_TABLE_NAME) {
    val messageId = reference("message_id", MessageTable)
    val url = text("url")
    val mediaType = varchar("media_type", 32).nullable()
   }

@Serializable
data class Message(
    @SerialName("id")
    val messageId: String?,
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("sender_id")
    val senderId: String,
    @SerialName("content")
    val content: String,
    @SerialName("message_type")
    val messageType: String = "text",
    @SerialName("is_deleted")
    val isDeleted: Boolean = false,
    @SerialName("expires_at")
    val expiresAt: String? = null,
    @SerialName("extra")
    val extra: String? = null,
    @SerialName("sender")
    val sender: User? = null
)

@Serializable
data class Media(
    @SerialName("id")
    val mediaId: String?,
    @SerialName("message_id")
    val messageId: String,
    @SerialName("url")
    val url: String,
    @SerialName("media_type")
    val mediaType: String? = null,
)

@Serializable
data class MessageList(
    val messages: List<Message>
)