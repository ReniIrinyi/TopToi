package application.dto

import domain.model.NoteModel
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class NoteDTO(
    val id: Int,
    val toiletId: Int,
    val userId: Int,
    val note: String,
    val addDate: String,
    val img: String?
) : DTO<NoteModel> {

    override fun toModel(): NoteModel {
        return NoteModel(
            id = id,
            toiletId = toiletId,
            userId = userId,
            note = note,
            addDate = addDate,
            img = img?.let { Base64.getDecoder().decode(it) }
        )
    }

    companion object {
        fun fromModel(model: NoteModel): NoteDTO {
            return NoteDTO(
                id = model.id ?: 0,
                toiletId = model.toiletId ?: 0,
                userId = model.userId ?: 0,
                note = model.note ?: "",
                addDate = model.addDate ?: "",
                img = model.img?.let {
                    Base64.getEncoder().encodeToString(it)
                }
            )
        }
    }
}
