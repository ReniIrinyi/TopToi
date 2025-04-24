package request

import kotlinx.serialization.Serializable

@Serializable
data class NoteRequest(
    val note: String,
    val image: String? = null
)