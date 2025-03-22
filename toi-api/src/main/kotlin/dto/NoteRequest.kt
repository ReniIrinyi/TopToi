package dto

import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(
    val note: String
)