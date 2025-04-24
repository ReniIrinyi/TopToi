package dto
import kotlinx.serialization.Serializable

@Serializable
data class ToiletDTO(
    val id: Int,
    val name: String,
    val addDate: String,
    val category: String?,
    val entryMethod: String?,
    val priceCHF: Double?,
    val code: String?,
    val latitude: Double,
    val longitude: Double,
    val tags: TagDTO?,
    val notes: List<NoteDTO> = emptyList(),
    val votes: List<VoteDTO> = emptyList()
)

@Serializable
data class TagDTO(
    val babyRoom: Boolean,
    val accessible: Boolean
)

@Serializable
data class NoteDTO(
    val id: Int,
    val toiletId: Int,
    val userId: Int,
    val note: String,
    val addDate: String,
    val imageBase64: String? = null
)