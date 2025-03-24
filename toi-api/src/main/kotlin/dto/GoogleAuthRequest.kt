// dto/GoogleAuthRequest.kt
package dto

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)
