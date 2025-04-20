package database.model

import kotlinx.serialization.Serializable
import java.sql.Blob

@Serializable
data class NoteModel(
    val userId: String,
    val toiletId:Int,
    val addDate: String,
    val note: String,
    val img:ByteArray?,
)