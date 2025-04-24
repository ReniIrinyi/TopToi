package request

import kotlinx.serialization.Serializable

@Serializable
data class VoteRequest(
    val vote: Int
)