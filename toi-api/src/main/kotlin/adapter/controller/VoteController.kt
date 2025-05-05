package adapter.controller
import application.dto.VoteDTO
import application.interactor.VoteInteractor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.voteRoutes(voteInteractor: VoteInteractor) {
    authenticate("auth-jwt") {
        post("/toilets/{toiletID}/votes/{userID}") {
            val toiletId = call.parameters["toiletID"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val userId = call.parameters["userID"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<VoteDTO>()

            val success = voteInteractor.voteToilet(toiletId, userId, request.value)
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
