package routes

import dto.NoteRequest
import dto.ToiletRequest
import dto.VoteRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.Service

fun Route.toiletRoute(service:Service){
    route("/toilets") {
        get {
            call.respond(service.fetchToilets())
        }
        get("{toiletID}") {
            val id = call.parameters["toiletID"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val toilet = service.fetchToilet(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Toilet not found")
            call.respond(toilet)
        }
        post {
            val request = call.receive<ToiletRequest>()
            val newToilet = service.addToilet(request)
            call.respond(HttpStatusCode.Created, newToilet)
        }
        post("{toiletID}/votes/{userID}") {
            val toiletID = call.parameters["toiletID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val userID = call.parameters["userID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userID")
            val voteRequest = call.receive<VoteRequest>()
            val result = service.vote(toiletID, userID, voteRequest.vote)
            call.respondText(result)
        }
        post("{toiletID}/notes/{userID}") {
            val toiletID = call.parameters["toiletID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val userID = call.parameters["userID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userID")
            val noteRequest = call.receive<NoteRequest>()
            val result = service.addNote(toiletID, userID, noteRequest.note)
            call.respondText(result)
        }
        put("{toiletID}/notes/{userID}") {
            val toiletID = call.parameters["toiletID"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val userID = call.parameters["userID"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing userID")
            val noteRequest = call.receive<NoteRequest>()
            val result = service.updateNote(toiletID, userID, noteRequest.note)
            call.respondText(result)
        }
        delete("{toiletID}/notes/{userID}") {
            val toiletID = call.parameters["toiletID"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val userID = call.parameters["userID"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userID")
            val result = service.removeNote(toiletID, userID)
            call.respondText(result)
        }
    }
}