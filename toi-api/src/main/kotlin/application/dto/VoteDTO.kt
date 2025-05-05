package application.dto

import domain.model.VoteModel
import kotlinx.serialization.Serializable

@Serializable
data class VoteDTO(
    val id: Int,
    val toiletId: Int,
    val userId: Int,
    val value: Int
) : DTO<VoteModel> {

    override fun toModel(): VoteModel {
        return VoteModel(
            id = id,
            toiletId = toiletId,
            userId = userId,
            value = value
        )
    }

    companion object {
        fun fromModel(model: VoteModel): VoteDTO {
            return VoteDTO(
                id = model.id ?: 0,
                toiletId = model.toiletId ?: 0,
                userId = model.userId ?: 0,
                value = model.value ?: 0
            )
        }
    }
}
