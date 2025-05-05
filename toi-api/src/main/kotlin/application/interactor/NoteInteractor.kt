package application.interactor

import adapter.persistence.database.repository.NoteRepository

class NoteInteractor(
    private val noteRepository: NoteRepository
)  {

     fun addNote(toiletId: Int, userId: Int, noteText: String, imageBytes: ByteArray?): Boolean {
        return noteRepository.addOrUpdateNote(toiletId, userId, noteText, imageBytes)
    }

     fun removeNote(toiletId: Int, userId: Int): Boolean {
        return noteRepository.deleteNote(toiletId, userId)
    }
}
