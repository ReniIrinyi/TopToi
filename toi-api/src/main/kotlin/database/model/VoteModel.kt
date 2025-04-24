package database.model

import kotlinx.serialization.Serializable

@Serializable
data class VoteModel(
    val id: Int?=null,
    val toiletId: Int?=null,
    val userId: Int?=null,
    val value: Int?=null
)