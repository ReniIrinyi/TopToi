package adapter.controller
import application.dto.ToiletDTO
import application.interactor.ToiletInteractor
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Route.toiletRoutes(toiletInteractor: ToiletInteractor) {
    route("/toilets") {
        get {
            val lat = call.request.queryParameters["lat"]?.toDoubleOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val lng = call.request.queryParameters["lng"]?.toDoubleOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            call.respond(toiletInteractor.fetchToilets(lat, lng))
        }

        get("{toiletID}") {
            val toiletId = call.parameters["toiletID"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val toilet = toiletInteractor.fetchToiletById(toiletId)
            if (toilet != null) {
                call.respond(toilet)
            } else {
                call.respond(HttpStatusCode.NotFound, "Toilet not found")
            }
        }

        authenticate("auth-jwt") {
            post {
                val request = call.receive<ToiletDTO>()
                //todo: get the current email
                val userEmail = call.receive<String>()
                val newToilet = toiletInteractor.createToilet(request, userEmail)
                if (newToilet != null) {
                    call.respond(HttpStatusCode.Created, newToilet)
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}
