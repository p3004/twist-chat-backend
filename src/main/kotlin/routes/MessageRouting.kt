package chat.twist.com.routes

import chat.twist.com.model.*
import chat.twist.com.repository.MessageRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.messageRouting() {
    val messageRepository: MessageRepository by inject()
    
    route("/message") {
        // Send a new message
        post("/send") {
            try {
                val message = call.receive<Message>()
                val messageId = messageRepository.sendMessage(message)
                call.respondText(messageId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Get messages for a chat
        get("/chat/{chatId}") {
            try {
                val chatId = call.parameters["chatId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing chat ID"
                )
                
                // Get pagination parameters
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0
                
                val messages = messageRepository.getChatMessages(chatId, limit, offset)
                call.respond(MessageList(messages = messages))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Delete a message (soft delete)
        delete("/{messageId}") {
            try {
                val messageId = call.parameters["messageId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, "Missing message ID"
                )
                
                val success = messageRepository.deleteMessage(messageId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Message not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}