package chat.twist.com.configure

import chat.twist.com.model.Auth
import chat.twist.com.model.AuthList
import chat.twist.com.model.AuthTable
import chat.twist.com.routes.userRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        get("/auth/list") {
            val users = transaction {
                AuthTable.selectAll().map {
                    Auth(
                        userName = it[AuthTable.userName],
                        email = it[AuthTable.email],
                        password = it[AuthTable.password]
                    )
                }
            }
            call.respond(AuthList(authList = users))
        }
        userRouting()

    }
}
