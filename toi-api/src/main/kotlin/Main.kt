import database.DatabaseConfig
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import routes.toiletRoute
import service.Service

fun main() {
    embeddedServer(Netty, port = 8080) {
        install(ContentNegotiation) {
            json()
        }
        DatabaseConfig.init(this)
        val toiletService = Service()
        routing {
            toiletRoute(toiletService)
        }
    }.start(wait = true)
}