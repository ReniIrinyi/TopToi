package service

import UserRequest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.typesafe.config.ConfigFactory
import dto.ToiletRequest
import database.model.NoteModel
import database.model.Tags
import database.model.ToiletModel
import database.model.VoteModel
import database.table.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Service{
    private val toilets = mutableListOf<ToiletModel>()
    private var nextId = 1

    fun createLocalUser(request: UserRequest): Boolean {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        return transaction {
            val exists = User.select { User.email eq request.email }.count() > 0
            if (exists) return@transaction false
            User.insert {
                it[email] = request.email
                it[passwordHash] = hashedPassword
                it[authProvider] = AuthProvider.ENUM_LOCAL
            }
            true
        }
    }

    fun createGoogleUser(email: String, name:String, imgUrl:String): Unit {
        transaction {
            User.insert {
                it[User.email] = email
                it[passwordHash] = "google_oauth"
                it[authProvider] = AuthProvider.ENUM_GOOGLE
                it[User.imgUrl]=imgUrl
                it[User.name] = name
            }
        }
    }

    fun authenticateUser(request: UserRequest): Boolean {
        return transaction {
            val userRow = User.select { User.email eq request.email }.singleOrNull() ?: return@transaction false
            if (userRow[User.authProvider] != AuthProvider.ENUM_LOCAL) return@transaction false
            val hash = userRow[User.passwordHash]
            BCrypt.checkpw(request.password, hash)
        }
    }

    fun getUserByEmail(email: String): database.model.User? {
        return transaction {
            User.select { User.email eq email }
                .map {
                    try {
                        val imageBytes = it[User.img]?.bytes ?: ByteArray(0)
                        database.model.User(
                            id = it[User.id],
                            email = it[User.email],
                            name = it[User.name],
                            imgUrl = it[User.imgUrl],
                            authProvider = it[User.authProvider],
                            passwordHash = "",
                            img = imageBytes
                        )
                    } catch (e: Exception) {
                        debugLog("Mapping hiba: ${e.message}")
                        null
                    }
                }
                .singleOrNull()
        }
    }


    fun isUserExist(email: String): Boolean {
        return transaction {
            User.select { User.email eq email }.count() > 0
        }
    }

    fun verifyGoogleIdToken(idToken: String): GoogleIdToken? {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf("1030506683349-q6dlqpqbpt54qhsr4v96r1npo02v9k6l.apps.googleusercontent.com"))
            .build()
        val clientSchlüssel = "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"
        val token: GoogleIdToken? = verifier.verify(idToken)
        return token
    }

    fun generateJwt(email: String): String {
        val fileConfig = ConfigFactory.parseFile(File("src/main/resources/application.conf"))
        val secret = fileConfig.getString("jwt.secret")

        val algorithm = Algorithm.HMAC256(secret)

        return JWT.create()
            .withSubject("UserAuth")
            .withClaim("email", email)
            .withIssuedAt(Date())
            .withExpiresAt(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24 Std
            .sign(algorithm)
    }

    suspend fun fetchToilets(lat: Double, lng: Double): List<ToiletModel> {
        val googleToilets = fetchGoogleToilets(lat, lng)
        debugLog(googleToilets)

        val dbToilets = fetchToiletsFromDatabase()
        debugLog(dbToilets)

        if (googleToilets.isNotEmpty() && dbToilets.isNotEmpty()) {
            val finalToilets = mutableListOf<ToiletModel>()

            for (googleToilet in googleToilets) {
                val matchedToilet = dbToilets.find {
                    it.latitude == googleToilet.latitude && it.longitude == googleToilet.longitude
                }

                if (matchedToilet != null) {
                    finalToilets.add(
                        googleToilet.copy(
                            id = matchedToilet.id,
                            notes = matchedToilet.notes,
                            votes = matchedToilet.votes
                        )
                    )
                } else {
                    finalToilets.add(googleToilet)
                }
            }

            val unmatchedDbToilets = dbToilets.filter { db ->
                googleToilets.none { it.latitude == db.latitude && it.longitude == db.longitude }
            }

            return finalToilets + unmatchedDbToilets
        }

        if (googleToilets.isNotEmpty()) {
            return googleToilets
        }

        if (dbToilets.isNotEmpty()) {
            return dbToilets
        }

        return emptyList()
    }


    private fun fetchToiletsFromDatabase(): List<ToiletModel> {
        return transaction {
            Toilet.selectAll().map { row ->
                val toiletId = row[Toilet.id]
                val notes = Note.select { Note.toiletId eq toiletId }.map {
                    val imageBytes = it[Note.img].bytes
                    NoteModel(
                        userId = it[Note.userId].toString(),
                        note = it[Note.note],
                        toiletId = it[Note.toiletId],
                        img = imageBytes,
                        addDate = it[Note.addDate].toString()
                    )
                }

                val votes = Vote.select { Vote.toiletId eq toiletId }.map {
                    VoteModel(
                        userId = it[Vote.userId].toString(),
                        value = it[Vote.value]
                    )
                }

                ToiletModel(
                    id = toiletId,
                    name = row[Toilet.name],
                    addDate = row[Toilet.addDate].toString(),
                    category = row[Toilet.category],
                    openHours = listOf(),
                    tags = Tags(
                        BABY_ROOM = row[Toilet.babyRoom],
                        WHEELCHAIR_ACCESSIBLE = row[Toilet.wheelchairAccessible]
                    ),
                    entryMethod = row[Toilet.entryMethod],
                    priceCHF = row[Toilet.priceCHF],
                    code = row[Toilet.code],
                    latitude = row[Toilet.latitude],
                    longitude = row[Toilet.longitude],
                    notes = notes,
                    votes = votes,
                    userId = "",
                )
            }
        }
    }



    private suspend fun fetchGoogleToilets(lat: Double, lng: Double): List<ToiletModel> {
        val apiKey = "AIzaSyCyiyQzvlmWxLZkMC-SrNriQ3BcUXFII_M"
        val radius = 1500
        val keyword = "public toilet"

        val client = HttpClient(CIO)
        val response: String = client.get("https://maps.googleapis.com/maps/api/place/nearbysearch/json") {
            url {
                parameters.append("location", "$lat,$lng")
                parameters.append("radius", radius.toString())
                parameters.append("keyword", keyword)
                parameters.append("key", apiKey)
            }
        }.bodyAsText()
        client.close()

        val json = Json.parseToJsonElement(response).jsonObject
        val results = json["results"]?.jsonArray ?: return emptyList()

        return results.mapIndexed { index, element ->
            val obj = element.jsonObject
            val location = obj["geometry"]!!.jsonObject["location"]!!.jsonObject

            ToiletModel(
                id = nextId + index,
                name = obj["name"]?.jsonPrimitive?.content ?: "Ismeretlen WC",
                addDate = "",
                latitude = location["lat"]!!.jsonPrimitive.double,
                longitude = location["lng"]!!.jsonPrimitive.double,
                notes = emptyList(),
                votes = emptyList()
            )
        }
    }

    fun fetchToilet(id: String): ToiletModel? {
        return toilets.find { it.id.toString() == id }
    }

    fun addToilet(request: ToiletRequest, userEmail:String): ToiletModel {
        return transaction {
            try {
                val toiletId = Toilet.insert {
                    it[name] = request.name
                    it[category] = request.category
                    it[entryMethod] = request.entryMethod
                    it[priceCHF] = request.priceCHF
                    it[code] = request.code
                    it[latitude] = request.latitude
                    it[longitude] = request.longitude
                    it[babyRoom] = request.tags.BABY_ROOM
                    it[wheelchairAccessible] = request.tags.WHEELCHAIR_ACCESSIBLE
                }[Toilet.id]

                val userId = User
                    .select { User.email eq userEmail }
                    .map { it[User.id] }
                    .firstOrNull() ?: throw IllegalStateException("User not found")


                if (request.vote != null && (request.vote == 1 || request.vote == -1)) {
                    Vote.insert {
                        it[Vote.toiletId] = toiletId
                        it[Vote.userId] = userId
                        it[value] = if (request.vote > 0) 1 else -1
                    }
                }

                if (!request.note.isNullOrBlank()) {
                    debugLog(request)

                    val imageBytes: ByteArray? = request.imageBase64?.let {
                        Base64.getDecoder().decode(it)
                    }


                    Note.insert {
                        it[Note.toiletId] = toiletId
                        it[Note.userId] = userId
                        it[note] = request.note
                        it[img] = ExposedBlob(imageBytes ?: ByteArray(0))
                        it[addDate] = Instant.now()                 }
                }

                ToiletModel(
                    id = toiletId,
                    name = request.name,
                    addDate = request.addDate,
                    category = request.category,
                    openHours = request.openHours,
                    tags = Tags(
                        BABY_ROOM = request.tags.BABY_ROOM,
                        WHEELCHAIR_ACCESSIBLE = request.tags.WHEELCHAIR_ACCESSIBLE
                    ),
                    entryMethod = request.entryMethod,
                    priceCHF = request.priceCHF,
                    code = request.code,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    notes = emptyList(),
                    votes = emptyList()
                )

            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }

    }

    fun vote(toiletID: String, userID: String, voteValue: Int): String {
        val toilet = fetchToilet(toiletID) ?: return "Toilet not found"
        val newVotes = toilet.votes.toMutableList()
        val index = newVotes.indexOfFirst { it.userId == userID }
        when {
            index == -1 && voteValue != 0 -> newVotes.add(VoteModel(userId = userID, value = voteValue))
            voteValue == 0 && index != -1 -> newVotes.removeAt(index)
            index != -1 -> newVotes[index] = VoteModel(userId = userID, value = voteValue)
        }
        val updatedToilet = toilet.copy(votes = newVotes)
        toilets.removeIf { it.id.toString() == toiletID }
        toilets.add(updatedToilet)
        return "Vote updated"
    }

    fun addNote(toiletID: String, userID: String, noteText: String, imageBytes:ByteArray): String {
        if (noteText.length < 3) return "Note is too short!"

        transaction {

            Note.insert {
                it[toiletId] = toiletID.toInt()
                it[userId] = userID.toInt()
                it[note] = noteText
                it[addDate] = Instant.now()
                it[img] = ExposedBlob(imageBytes)
            }
        }

        return "Note added"
    }

    fun updateNote(toiletID: String, userID: String, noteText: String, imageBytes: ByteArray): String {
        return addNote(toiletID, userID, noteText, imageBytes)
    }

    fun removeNote(toiletID: String, userID: String): String {
        val toilet = fetchToilet(toiletID) ?: return "Toilet not found"
        val newNotes = toilet.notes.toMutableList()
        val index = newNotes.indexOfFirst { it.userId == userID }
        if (index != -1) {
            newNotes.removeAt(index)
        }
        val updatedToilet = toilet.copy(notes = newNotes)
        toilets.removeIf { it.id.toString() == toiletID }
        toilets.add(updatedToilet)
        return "Note removed"
    }

    private val logFile = File("logs/debug.log")
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun debugLog(data: Any) {
        val timestamp = LocalDateTime.now().format(formatter)
        val traceElement = Thread.currentThread().stackTrace
            .firstOrNull { it.className.contains("service") && it.methodName != "debugLog" }

        val location = if (traceElement != null) {
            "${traceElement.fileName}:${traceElement.lineNumber} -> ${traceElement.methodName}()"
        } else {
            "unknown source"
        }

        val logEntry = buildString {
            append("[$timestamp] [$location]\n")
            appendLine(
                try {
                    Json.encodeToString(data)
                } catch (e: Exception) {
                    data.toString()
                }
            )
        }

        logFile.parentFile.mkdirs()
        logFile.appendText(logEntry)
    }

}