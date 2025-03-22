package dto

import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val vote: Int
)