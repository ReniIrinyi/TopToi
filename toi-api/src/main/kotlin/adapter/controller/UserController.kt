package adapter.controller

import application.dto.UserDTO
import application.interactor.UserInteractor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.userRoutes(userInteractor: UserInteractor) {
    route("/user") {
        post("/register") {
            val success = userInteractor.createLocalUser(call.receive<UserDTO>())
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            }
        }

        route("/auth") {
            post("/google") {
                val (token, user) = userInteractor.authenticateGoogleUser(call.receive<UserDTO>())
                call.respond(mapOf("token" to token, "data" to user))
            }

            post("/local") {
                val success = userInteractor.authenticateLocalUser(call.receive<UserDTO>())
                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            }
        }
    }

}
