package database.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteModel(
    val userId: String,
    val addDate: String,
    val text: String
)