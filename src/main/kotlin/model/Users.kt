package chat.twist.com.model

import chat.twist.com.utils.USER_TABLE_NAME
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table

object UserTable: UUIDTable(USER_TABLE_NAME) {
    val firebaseUid = varchar("firebase_uid", 128)
    val userName = varchar("username", 64)
    val displayName = varchar("display_name", 128)
    val avatarUrl = text("avatar_url")
    val email = varchar("email",255)
    val bio = varchar("bio",255)
}


@Serializable
data class User(
    @SerialName("id")
    val userId: String?,
    @SerialName("firebase_uid")
    val firebaseUid: String,
    @SerialName("username")
    val userName: String,
    @SerialName("display_name")
    val displayName: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    @SerialName("email")
    val email: String,
    @SerialName("bio")
    val bio: String? = null
)
@Serializable
data class Users(
    val users: List<User>
)