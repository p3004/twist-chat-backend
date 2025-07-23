package chat.twist.com

import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database

fun Application.initDatabase() {
    val config = environment.config
    val dbUrl = config.property("database.url").getString()
    val dbUser = config.property("database.user").getString()
    val dbPassword = config.property("database.password").getString()

    Database.connect(
        url = dbUrl,
        user = dbUser,
        password = dbPassword
    )
}