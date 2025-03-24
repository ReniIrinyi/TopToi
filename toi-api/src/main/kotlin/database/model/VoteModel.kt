package database.model

import kotlinx.serialization.Serializable

@Serializable
data class VoteModel(
    val userId: String,
    val value: Int
)