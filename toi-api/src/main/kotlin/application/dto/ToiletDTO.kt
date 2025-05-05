package application.dto

import domain.model.*
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
    val tags: TagDTO? = null,
    val notes: List<NoteDTO> = emptyList(),
    val votes: List<VoteDTO> = emptyList()
) : DTO<ToiletModel> {

    override fun toModel(): ToiletModel {
        return ToiletModel(
            id = id,
            name = name,
            addDate = addDate,
            category = category,
            entryMethod = entryMethod,
            priceCHF = priceCHF,
            code = code,
            latitude = latitude,
            longitude = longitude,
            tags = tags?.toModel() ?: TagModel(),
            notes = notes.map { it.toModel() },
            votes = votes.map { it.toModel() }
        )
    }

    companion object {
        fun fromModel(model: ToiletModel): ToiletDTO {
            return ToiletDTO(
                id = model.id ?: 0,
                name = model.name,
                addDate = model.addDate ?: "",
                category = model.category,
                entryMethod = model.entryMethod,
                priceCHF = model.priceCHF,
                code = model.code,
                latitude = model.latitude,
                longitude = model.longitude,
                tags = model.tags?.let { TagDTO.fromModel(it) },
                notes = model.notes.map { NoteDTO.fromModel(it) },
                votes = model.votes.map { VoteDTO.fromModel(it) }
            )
        }
    }
}
