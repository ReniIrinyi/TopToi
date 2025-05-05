package adapter.controller

import application.dto.NoteDTO
import application.interactor.NoteInteractor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Route.noteRoutes(noteInteractor: NoteInteractor) {
    authenticate("auth-jwt") {
        post("/toilets/{toiletID}/notes/{userID}") {
            val toiletId = call.parameters["toiletID"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val userId = call.parameters["userID"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val request = call.receive<NoteDTO>()
            val imageBytes = request.img?.let { Base64.getDecoder().decode(it) }

            val success = noteInteractor.addNote(toiletId, userId, request.note, imageBytes)
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }

        delete("/toilets/{toiletID}/notes/{userID}") {
            val toiletId = call.parameters["toiletID"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)
            val userId = call.parameters["userID"]?.toIntOrNull() ?: return@delete call.respond(HttpStatusCode.BadRequest)

            val success = noteInteractor.removeNote(toiletId, userId)
            if (success) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}
