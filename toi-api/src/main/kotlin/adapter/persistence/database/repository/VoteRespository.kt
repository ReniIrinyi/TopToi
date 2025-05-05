package adapter.persistence.database.repository

import adapter.persistence.database.table.Vote
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class VoteRepository  {

     fun addOrUpdateVote(toiletId: Int, userId: Int, voteValue: Int): Boolean {
        return transaction {
            val updated = Vote.update({ (Vote.toiletId eq toiletId) and (Vote.userId eq userId) }) {
                it[value] = voteValue
            }
            if (updated == 0) {
                Vote.insert {
                    it[Vote.toiletId] = toiletId
                    it[Vote.userId] = userId
                    it[value] = voteValue
                }
            }
            true
        }
    }
}
