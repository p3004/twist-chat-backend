package chat.twist.com

import chat.twist.com.configure.*
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    configureKoin()
    configureHTTP()
    configureSecurity()
    configureSerialization()
    configureMonitoring()
    configureDatabase()
    ConfigureUserNameBloomFilter.initializeFromDatabase()
    configureRouting()
    configureSockets()

    environment.monitor.subscribe(ApplicationStopped) {
        ConfigureUserNameBloomFilter.saveToDisk()
    }
}
