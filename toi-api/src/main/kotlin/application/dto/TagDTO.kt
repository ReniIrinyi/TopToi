package application.dto

import domain.model.TagModel
import kotlinx.serialization.Serializable

@Serializable
data class TagDTO(
    val babyRoom: Boolean,
    val accessible: Boolean
) : DTO<TagModel> {

    override fun toModel(): TagModel = TagModel(
        babyRoom = babyRoom,
        accessible = accessible
    )

    companion object {
        fun fromModel(model: TagModel): TagDTO = TagDTO(
            babyRoom = model.babyRoom ?: false,
            accessible = model.accessible ?: false
        )
    }
}
