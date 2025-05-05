package application.interactor
import adapter.persistence.database.repository.VoteRepository

class VoteInteractor(
    private val voteRepository: VoteRepository
)  {

     fun voteToilet(toiletId: Int, userId: Int, voteValue: Int): Boolean {
        return voteRepository.addOrUpdateVote(toiletId, userId, voteValue)
    }
}
