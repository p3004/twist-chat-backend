package chat.twist.com

import chat.twist.com.model.Auth
import chat.twist.com.model.AuthList
import chat.twist.com.model.AuthTable
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
        get("/") {
            call.respondText("Hello, World!")
        }
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
        post("/auth/upsert") {
            try {
                val auth = call.receive<Auth>()
                transaction {
                    val updatedRows = AuthTable.update({ AuthTable.userName eq auth.userName }) {
                        it[email] = auth.email
                        it[password] = auth.password
                    }
                    if (updatedRows == 0) {
                        AuthTable.insert {
                            it[userName] = auth.userName
                            it[email] = auth.email
                            it[password] = auth.password
                        }
                    }
                }
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                println(" [31m$e [0m")
                call.respond(HttpStatusCode.BadRequest)
            }
        }
        
    }
}
