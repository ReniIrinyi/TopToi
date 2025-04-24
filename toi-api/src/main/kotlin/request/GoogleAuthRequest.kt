// dto/GoogleAuthRequest.kt
package request

import kotlinx.serialization.Serializable

@Serializable
data class GoogleAuthRequest(val idToken: String)
