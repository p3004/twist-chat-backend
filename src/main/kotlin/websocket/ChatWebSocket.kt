package chat.twist.com.websocket

import chat.twist.com.model.Message
import chat.twist.com.repository.MessageRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.concurrent.ConcurrentHashMap

fun Application.configureChatWebSockets() {
    val connections = ConcurrentHashMap<String, MutableSet<DefaultWebSocketSession>>()
    
    routing {
        val messageRepository: MessageRepository by inject()
        
        webSocket("/ws/chat/{chatId}") {
            val chatId = call.parameters["chatId"] ?: return@webSocket close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Chat ID is required"))
            
            // Add this connection to the chat's connections
            connections.computeIfAbsent(chatId) { mutableSetOf() }.add(this)
            
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        val text = frame.readText()
                        val message = Json.decodeFromString<Message>(text)
                        
                        // Save the message to the database
                        val messageId = messageRepository.sendMessage(message)
                        
                        // Broadcast the message to all connections in this chat
                        val messageWithId = message.copy(messageId = messageId)
                        val messageJson = Json.encodeToString(messageWithId)
                        
                        connections[chatId]?.forEach { session ->
                            if (session != this) { // Don't send back to the sender
                                session.send(messageJson)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error in WebSocket: ${e.message}")
            } finally {
                // Remove this connection when it's closed
                connections[chatId]?.remove(this)
                if (connections[chatId]?.isEmpty() == true) {
                    connections.remove(chatId)
                }
            }
        }
    }
}