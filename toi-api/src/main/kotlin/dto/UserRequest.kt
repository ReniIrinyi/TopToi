import database.table.AuthProvider
import kotlinx.serialization.Serializable
import java.sql.Blob

@Serializable
data class UserRequest(
    val email: String,
    val password: String,
    val authProvider: AuthProvider,
    val name:String,
    val imgUrl:String?,
    val img:ByteArray?
)