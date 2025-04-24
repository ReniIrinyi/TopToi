package request

import database.table.AuthProvider
import kotlinx.serialization.Serializable

@Serializable
data class UserRequest(
    val email: String,
    val password: String?,
    val name: String,
    val imgUrl: String? = null
)