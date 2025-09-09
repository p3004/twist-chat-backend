package chat.twist.com.configure

import chat.twist.com.routes.chatRouting
import chat.twist.com.routes.messageRouting
import chat.twist.com.routes.userRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        userRouting()
        chatRouting()
        messageRouting()
    }
}
