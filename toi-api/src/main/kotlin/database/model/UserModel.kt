package database.model

import database.table.AuthProvider
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val authProvider:AuthProvider,
    val name:String,
    val imgUrl:String?,
    val img: ByteArray?,
    val notes: List<NoteModel> = emptyList(),
    val votes: List<VoteModel> = emptyList()
)