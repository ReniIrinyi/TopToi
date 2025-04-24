package database.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val email: String,
    val authProvider: String,
    val name:String,
    val imgUrl:String?="",
    val img: ByteArray?=null,
    val notes: List<NoteModel> = emptyList(),
    val votes: List<VoteModel> = emptyList()
)