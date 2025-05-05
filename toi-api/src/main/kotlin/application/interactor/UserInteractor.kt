package application.interactor

import adapter.persistence.database.repository.UserRepository
import application.dto.UserDTO
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.typesafe.config.ConfigFactory
import common.LoggerService.debugLog
import domain.model.UserModel
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.util.*

class UserInteractor(
    private val userRepository: UserRepository,
) {

    fun createLocalUser(dto: UserDTO): Boolean {
        val existing = userRepository.getUserByEmail(dto.email)
        if (existing != null) return false

        val hashed = dto.password?.let { BCrypt.hashpw(it, BCrypt.gensalt()) } ?: return false
        val newUser = dto.copy(password = hashed).toModel()
        userRepository.createLocalUser(newUser)
        return true
    }

    fun authenticateLocalUser(user: UserDTO): Boolean {
        return userRepository.authenticateLocalUser(user.toModel())
    }

    fun authenticateGoogleUser(user: UserDTO): Pair<UserDTO?,String?> {
        val token = verifyGoogleIdToken(user.idToken)
        val payload= token?.payload
        if (payload?.email != null) {
            val email = payload.email
            val name = payload["name"] as? String ?: ""
            val img = payload["picture"] as? String ?: ""

            val exists = userRepository.isUserExist(email)
            debugLog(exists)

            if (!exists) {
                val userModel = UserModel(
                    id = 0,
                    email = email,
                    name = name,
                    authProvider = "GOOGLE",
                    imgUrl = img,
                    notes = emptyList(),
                    votes = emptyList(),
                    password = "google_oauth"
                )
                userRepository.createGoogleUser(userModel)
            }


            val jwtToken = generateJwt(email)
            val userDto = userRepository.getUserByEmail(email)?.let { UserDTO.fromModel(it) }
            return Pair(userDto, jwtToken)
        }
        return Pair(null, null)
    }

    fun verifyGoogleIdToken(idToken: String?): GoogleIdToken? {
        debugLog("here");
        try {
            val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(listOf("1030506683349-q6dlqpqbpt54qhsr4v96r1npo02v9k6l.apps.googleusercontent.com"))
                .build()
            val clientSchlüssel = "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"
            val token: GoogleIdToken? = verifier.verify(idToken)
            return token
        } catch (e:Exception){
            debugLog("verifyGoogleIdToken error: ${e.message}")
            return null
        }
    }

    fun generateJwt(email: String): String {
        try {
            val fileConfig = ConfigFactory.parseFile(File("src/main/resources/application.conf"))
            val secret = fileConfig.getString("jwt.secret")

            val algorithm = Algorithm.HMAC256(secret)

            return JWT.create()
                .withSubject("UserAuth")
                .withClaim("email", email)
                .withIssuedAt(Date())
                .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Std
                .sign(algorithm)
        } catch (e:Exception){
            debugLog("generateJwtError: ${e.message}")
            return ""
        }

    }
}
