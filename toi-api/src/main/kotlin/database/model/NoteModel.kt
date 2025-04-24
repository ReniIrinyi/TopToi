package database.model

import kotlinx.serialization.Serializable

@Serializable
data class NoteModel(
    val id: Int?=null,
    val toiletId: Int?=null,
    val userId: Int?=null,
    val note: String?="",
    val addDate: String?=null,
    val image: ByteArray?=null
)