package me.amryousef

import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.serialization.gson.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.launch
import java.time.Duration
import java.util.*

// ----------------------------
// 1. This is the main() method JVM looks for
// ----------------------------
fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0") {
        module()
    }.start(wait = true)
}

// ----------------------------
// 2. Your existing Ktor module
// ----------------------------
fun Application.module() {

    install(DefaultHeaders)
    install(CallLogging)

    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(30)
        timeout = Duration.ofSeconds(30)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        gson()
    }

    val connections = Collections.synchronizedMap(
        mutableMapOf<String, WebSocketServerSession>()
    )

    routing {
        webSocket("/connect") {
            val id = UUID.randomUUID().toString()
            connections[id] = this
            application.log.info("Client connected: $id, total = ${connections.size}")

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val message = frame.readText()

                        // Broadcast to all except sender
                        connections.filterKeys { it != id }
                            .values
                            .forEach { session ->
                                launch { session.send(Frame.Text(message)) }
                            }
                    }
                }
            } catch (e: Exception) {
                application.log.error("WebSocket error: $e")
            } finally {
                connections.remove(id)
                application.log.info("Client disconnected: $id")
            }
        }
    }
}
