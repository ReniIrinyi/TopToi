package domain.model

import adapter.persistence.database.table.Tag
import kotlinx.serialization.Serializable

@Serializable
data class ToiletModel(
    val id: Int?=null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val addDate: String? = null,
    val entryMethod: String?="",
    val priceCHF: Double?=0.0,
    val code: String?="",
    val category: String?="",
    val tags: TagModel,
    val notes: List<NoteModel> = emptyList(),
    val votes: List<VoteModel> = emptyList()
)
