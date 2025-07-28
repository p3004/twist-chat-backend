package chat.twist.com.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

object AuthTable : Table("public.auth") {
    val userName = varchar("user_name", 255)
    val email = varchar("email", 255)
    val password = varchar("password", 255)
    override val primaryKey = PrimaryKey(userName)
}

@Serializable
data class Auth(
    @SerialName("user_name")
    val userName: String,
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)
@Serializable
data class AuthList(
    val authList: List<Auth>
)