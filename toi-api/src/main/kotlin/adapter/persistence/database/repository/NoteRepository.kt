package adapter.persistence.database.repository

import adapter.persistence.database.table.Note
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import java.time.Instant

class NoteRepository {

     fun addOrUpdateNote(toiletId: Int, userId: Int, noteText: String, imageBytes: ByteArray?): Boolean {
        return transaction {
            val existing = Note.select {
                (Note.toiletId eq toiletId) and (Note.userId eq userId)
            }.singleOrNull()

            if (existing != null) {
                Note.update({ (Note.toiletId eq toiletId) and (Note.userId eq userId) }) {
                    it[note] = noteText
                    it[addDate] = Instant.now()
                    it[img] = imageBytes?.let { ExposedBlob(it) }
                }
            } else {
                Note.insert {
                    it[Note.toiletId] = toiletId
                    it[Note.userId] = userId
                    it[note] = noteText
                    it[addDate] = Instant.now()
                    it[img] = imageBytes?.let { ExposedBlob(it) }
                }
            }
            true
        }
    }

     fun deleteNote(toiletId: Int, userId: Int): Boolean {
        return transaction {
            val deleted = Note.deleteWhere {
                (Note.toiletId eq toiletId) and (Note.userId eq userId)
            }
            deleted > 0
        }
    }
}
