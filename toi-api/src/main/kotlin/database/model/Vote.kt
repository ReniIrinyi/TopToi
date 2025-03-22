package database.model

import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    val userId: String,
    val value: Int
)