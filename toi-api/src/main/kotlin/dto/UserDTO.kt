package dto

import database.table.AuthProvider
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val email: String,
    val name: String,
    val authProvider: String,
    val imgUrl: String?,
    val imgBase64: String? = null,
    val notes: List<NoteDTO> = emptyList(),
    val votes: List<VoteDTO> = emptyList()
)
