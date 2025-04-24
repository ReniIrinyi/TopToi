package database.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteModel(
    val id: Int,
    val toiletId: Int,
    val userId: Int,
    val note: String,
    val addDate: String,
    val image: ByteArray?
)