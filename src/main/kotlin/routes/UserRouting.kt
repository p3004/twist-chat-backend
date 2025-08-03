package chat.twist.com.routes

import chat.twist.com.configure.ConfigureUserNameBloomFilter
import chat.twist.com.model.*
import chat.twist.com.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.userRouting() {
    val userRepository: UserRepository by inject()

    route("/user") {

        post("/upsert") {
            try {
                val user = call.receive<User>()
                val upsertUserId = userRepository.upsertUser(user)
                ConfigureUserNameBloomFilter.addUsername(user.userName)
                call.respondText(upsertUserId)
            } catch (e: BadRequestException) {
                println(" [31m$e [0m")
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        get("/{id}") {
            try {
                val userId = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing id parameter")

                val user = userRepository.getUserById(userId)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }

        get("/username/available/{user_name}") {
            try {
                val userName = call.parameters["user_name"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing user name")
                val isUserNameUnique = userRepository.usernameExists(userName).not()
                call.respond(isUserNameUnique)
            }catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }

    }


}