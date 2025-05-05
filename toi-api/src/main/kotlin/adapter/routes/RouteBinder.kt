package adapter.routes

import adapter.controller.*
import application.interactor.NoteInteractor
import application.interactor.ToiletInteractor
import application.interactor.UserInteractor
import application.interactor.VoteInteractor
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    userInteractor: UserInteractor,
    toiletInteractor: ToiletInteractor,
    noteInteractor: NoteInteractor,
    voteInteractor: VoteInteractor
) {
    routing {
        userRoutes(userInteractor)
        toiletRoutes(toiletInteractor)
        noteRoutes(noteInteractor)
        voteRoutes(voteInteractor)
    }
}
