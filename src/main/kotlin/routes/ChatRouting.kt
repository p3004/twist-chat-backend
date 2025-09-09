package chat.twist.com.routes

import chat.twist.com.model.*
import chat.twist.com.repository.ChatRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.util.*

fun Route.chatRouting() {
    val chatRepository: ChatRepository by inject()
    
    route("/chat") {
        // Create a new chat
        post("/create") {
            try {
                val request = call.receive<ChatCreate>()
                val chat = request.chat
                val memberIds = request.memberIds
                
                val chatId = chatRepository.createChat(chat, memberIds)
                call.respondText(chatId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Get chat by ID
        get("/{id}") {
            try {
                val chatId = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing chat ID"
                )
                
                val chat = chatRepository.getChatById(chatId)
                if (chat != null) {
                    call.respond(chat)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Chat not found")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Get all chats for a user
        get("/user/{userId}") {
            try {
                val userId = call.parameters["userId"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest, "Missing user ID"
                )
                
                val chats = chatRepository.getUserChats(userId)
                call.respond(ChatList(chats = chats))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Add a user to a chat
        post("/{chatId}/add/{userId}") {
            try {
                val chatId = call.parameters["chatId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing chat ID"
                )
                val userId = call.parameters["userId"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing user ID"
                )
                
                val success = chatRepository.addUserToChat(chatId, userId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to add user to chat")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Remove a user from a chat
        delete("/{chatId}/remove/{userId}") {
            try {
                val chatId = call.parameters["chatId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, "Missing chat ID"
                )
                val userId = call.parameters["userId"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest, "Missing user ID"
                )
                
                val success = chatRepository.removeUserFromChat(chatId, userId)
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found in chat")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
        
        // Check or create direct chat between two users
        post("/direct") {
            try {
                val request = call.receive<DirectChatMembers>()
                
                // Check if a direct chat already exists
                var chatId = chatRepository.getDirectChatBetweenUsers(
                    user1Id = request.chatCreatorUserId,
                    user2Id = request.chatReceiverUserId
                )
                
                // If not, create a new direct chat
                if (chatId == null) {
                    val chat = Chat(
                        chatId = null,
                        isGroup = false,
                        name = null,  // Direct chats don't need names
                        createdBy = request.chatCreatorUserId,
                        mode = "normal"
                    )
                    chatId = chatRepository.createChat(
                        chat = chat,
                        memberIds = listOf(request.chatCreatorUserId,request.chatReceiverUserId)
                    )
                }
                
                call.respondText(chatId)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.message}")
            }
        }
    }
}