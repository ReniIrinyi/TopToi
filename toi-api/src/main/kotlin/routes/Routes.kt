package routes

import UserRequest
import dto.GoogleAuthRequest
import dto.NoteRequest
import dto.ToiletRequest
import dto.VoteRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import service.Service


fun ApplicationCall.getCurrentUserEmail(): String {
    val principal = this.principal<JWTPrincipal>()
    return principal?.getClaim("email", String::class)
        ?: throw IllegalStateException("No JWT principal or email found")
}

fun Route.toiletRoute(service:Service){
    route("/toilets") {
        get {
        service.debugLog("here");
            val lat = call.request.queryParameters["lat"]?.toDoubleOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val lng = call.request.queryParameters["lng"]?.toDoubleOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(service.fetchToilets(lat,lng))
        }
        get("{toiletID}") {
            val id = call.parameters["toiletID"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing toiletID")
            val toilet = service.fetchToilet(id) ?: return@get call.respond(HttpStatusCode.NotFound, "Toilet not found")
            call.respond(toilet)
        }
        authenticate("auth-jwt") {
            post {
                val request = call.receive<ToiletRequest>()
                val email = call.getCurrentUserEmail()
                val newToilet = service.addToilet(request, email)
                call.respond(HttpStatusCode.Created, newToilet)
            }
        }
        authenticate("auth-jwt") {
            post("{toiletID}/votes/{userID}") {
                val toiletID = call.parameters["toiletID"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing toiletID"
                )
                val userID =
                    call.parameters["userID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userID")
                val voteRequest = call.receive<VoteRequest>()
                val result = service.vote(toiletID, userID, voteRequest.vote)
                call.respondText(result)
            }
        }
        authenticate("auth-jwt") {
            post("{toiletID}/notes/{userID}") {
                val toiletID = call.parameters["toiletID"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing toiletID"
                )
                val userID =
                    call.parameters["userID"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing userID")
                val noteRequest = call.receive<NoteRequest>()
                val result = service.addNote(
                    toiletID, userID, noteRequest.note,
                    imageBytes = TODO()
                )
                call.respondText(result)
            }
        }
        authenticate("auth-jwt") {
            put("{toiletID}/notes/{userID}") {
                val toiletID = call.parameters["toiletID"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing toiletID"
                )
                val userID =
                    call.parameters["userID"] ?: return@put call.respond(HttpStatusCode.BadRequest, "Missing userID")
                val noteRequest = call.receive<NoteRequest>()
                val result = service.updateNote(
                    toiletID, userID, noteRequest.note,
                    imageBytes = TODO()
                )
                call.respondText(result)
            }
        }
        authenticate("auth-jwt") {
            delete("{toiletID}/notes/{userID}") {
                val toiletID = call.parameters["toiletID"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    "Missing toiletID"
                )
                val userID =
                    call.parameters["userID"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing userID")
                val result = service.removeNote(toiletID, userID)
                call.respondText(result)
            }
        }
    }

    route("/user") {
        service.debugLog("here")
        post("/register") {
            val request = call.receive<UserRequest>()
            val success = service.createLocalUser(request)
            if (success) {
                val token = service.generateJwt(request.email)
                val user = service.getUserByEmail(request.email)
                println(user)

                call.respond(HttpStatusCode.Created, mapOf("token" to token, "data" to user))
            } else {
                call.respond(HttpStatusCode.Conflict, "User already exists")
            }
        }

        post("/login") {
            val request = call.receive<UserRequest>()
            val authenticated = service.authenticateUser(request)
            service.debugLog(authenticated)
            if (authenticated) {
                val token = service.generateJwt(request.email)
                val user = service.getUserByEmail(request.email)
                println(user)

                call.respond(HttpStatusCode.OK, mapOf("token" to token, "data" to user))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }
    }

    route("/auth") {
        post("/google") {
            val authRequest = call.receive<GoogleAuthRequest>()
            val token = service.verifyGoogleIdToken(authRequest.idToken)
            val payload= token?.payload
            if (payload?.email != null) {
                val email = payload.email
                val name = payload["name"] as? String
                val picture = payload["picture"] as? String

                val exists = service.isUserExist(email)
                service.debugLog(exists)

                if (!exists) {
                    if (name != null) {
                        if (picture != null) {
                            service.createGoogleUser(email, name, picture)
                        }
                    }
                }

                val jwtToken = service.generateJwt(email)

                val user = service.getUserByEmail(email)
                service.debugLog("here kommt userobject line 160")
                service.debugLog(user.toString())
                call.respond(HttpStatusCode.OK, mapOf("token" to jwtToken, "data" to user.toString() ))
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid Google ID Token")
            }
        }

    }

}

