package domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UserModel(
    val id: Int? = null,
    val email: String,
    val password: String,
    val authProvider: String,
    val name:String,
    val imgUrl:String?="",
    val img: ByteArray?=null,
    val notes: List<NoteModel> = emptyList(),
    val votes: List<VoteModel> = emptyList()
)