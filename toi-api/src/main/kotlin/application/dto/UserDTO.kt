package application.dto

import domain.model.UserModel
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class UserDTO(
    val id: Int? = null,
    val idToken: String? = null,
    val email: String,
    val password: String? = null,
    val name: String? = null,
    val authProvider: String? = null,
    val imgUrl: String? = null,
    val imgBase64: String? = null,
    val notes: List<NoteDTO> = emptyList(),
    val votes: List<VoteDTO> = emptyList()
) : DTO<UserModel> {
    override fun toModel(): UserModel {
        return UserModel(
            id = id,
            email = email,
            name = name ?: "",
            authProvider = authProvider ?: "",
            imgUrl = imgUrl,
            img = imgBase64?.let { Base64.getDecoder().decode(it) },
            notes = notes.map { it.toModel() },
            votes = votes.map { it.toModel() }
        )
    }

    companion object {
        fun fromModel(user: UserModel): UserDTO {
            return UserDTO(
                id = user.id,
                email = user.email,
                name = user.name,
                authProvider = user.authProvider,
                imgUrl = user.imgUrl,
                imgBase64 = user.img?.takeIf { it.isNotEmpty() }
                    ?.let { Base64.getEncoder().encodeToString(it) },
                notes = user.notes.map { NoteDTO.fromModel(it) },
                votes = user.votes.map { VoteDTO.fromModel(it) }
            )
        }
    }
}
