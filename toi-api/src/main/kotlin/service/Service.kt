package service

import adapter.database.table.*
import adapter.persistence.database.repository.table.*
import adapter.persistence.database.table.*
import adapter.table.*
import application.request.UserRequest
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.typesafe.config.ConfigFactory
import application.request.ToiletRequest
import domain.model.NoteModel
import domain.model.TagModel
import domain.model.ToiletModel
import domain.model.VoteModel
import database.table.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import persistence.database.table.*
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class Service{
    private val toilets = mutableListOf<ToiletModel>()
    private var nextId = 1

    fun createLocalUser(request: UserRequest): Boolean {
        return try {
            transaction {
                if (User.select { User.email eq request.email }.count() > 0) return@transaction false

                User.insert {
                    it[email] = request.email
                    it[password] = BCrypt.hashpw(request.password, BCrypt.gensalt())
                    it[authProvider] = AuthProvider.ENUM_LOCAL.name
                    it[name] = request.name
                    it[imgUrl] = request.imgUrl
                }
                true
            }
        } catch (e: Exception) {
            debugLog("createLocalUser error: ${e.message}")
            false
        }
    }

    fun createGoogleUser(email: String, name: String, imgUrl: String):Boolean {
       return try {
            transaction {
                User.insert {
                    it[User.email] = email
                    it[password] = "google_oauth"
                    it[authProvider] = AuthProvider.ENUM_GOOGLE.name
                    it[User.name] = name
                    it[User.imgUrl] = imgUrl
                }
                true
            }
        } catch (e: Exception) {
            debugLog("createGoogleUser error: ${e.message}")
           false
        }
    }


    fun authenticateUser(request: UserRequest): Boolean {
        return try {
            transaction {
                val userRow = User.select { User.email eq request.email }.singleOrNull() ?: return@transaction false
                if (userRow[User.authProvider] != AuthProvider.ENUM_LOCAL.name) return@transaction false
                BCrypt.checkpw(request.password, userRow[User.password])
            }
        } catch (e: Exception) {
            debugLog("authenticateUser error: ${e.message}")
            false
        }
    }

    fun getUserByEmail(email: String): domain.model.UserModel? {
        return try {
            transaction {
                User.select { User.email eq email }
                    .mapNotNull {
                        val userId = it[User.id]

                        val notes = Note.select { Note.userId eq userId }.map { noteRow ->
                            NoteModel(
                                id = noteRow[Note.id],
                                toiletId = noteRow[Note.toiletId],
                                userId = userId,
                                note = noteRow[Note.note],
                                addDate = noteRow[Note.addDate].toString(),
                                image = noteRow[Note.img]?.bytes
                            )
                        }

                        val votes = Vote.select { Vote.userId eq userId }.map { voteRow ->
                            VoteModel(
                                id = voteRow[Vote.id],
                                toiletId = voteRow[Vote.toiletId],
                                userId = userId,
                                value = voteRow[Vote.value]
                            )
                        }

                        domain.model.UserModel(
                            id = userId,
                            email = it[User.email],
                            name = it[User.name] ?: "",
                            authProvider = it[User.authProvider].toString(),
                            imgUrl = it[User.imgUrl],
                            img = it[User.img]?.bytes,
                            notes = notes,
                            votes = votes
                        )
                    }.singleOrNull()
            }
        } catch (e: Exception) {
            debugLog("getUserByEmail error: ${e.message}")
            null
        }
    }

    fun isUserExist(email: String): Boolean {
        return try {
            transaction {
                User.select { User.email eq email }.count() > 0
            }
        } catch (e: Exception) {
            debugLog("isUserExist error: ${e.message}")
            false
        }
    }


    fun verifyGoogleIdToken(idToken: String): GoogleIdToken? {
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

    suspend fun fetchToilets(lat: Double, lng: Double): List<ToiletModel> {
        try {
            val googleToilets = fetchGoogleToilets(lat, lng)
            debugLog(googleToilets)

            val dbToilets = fetchToiletsFromDatabase()
            debugLog(dbToilets)

            val finalToilets = mutableListOf<ToiletModel>()

            for (googleToilet in googleToilets) {
                val match = dbToilets.find { it.latitude == googleToilet.latitude && it.longitude == googleToilet.longitude }
                if (match != null) {
                    finalToilets.add(
                        googleToilet.copy(
                            id = match.id,
                            notes = match.notes,
                            votes = match.votes,
                            tags = match.tags
                        )
                    )
                } else {
                    finalToilets.add(googleToilet)
                }
            }

            val unmatchedDbToilets = dbToilets.filterNot { db ->
                finalToilets.any { g -> g.latitude == db.latitude && g.longitude == db.longitude }
            }

            finalToilets.addAll(unmatchedDbToilets)

            return finalToilets
        } catch (e:Exception){
            debugLog("fetchToilets error: ${e.message}")
            return emptyList()
        }

    }


    private fun fetchToiletsFromDatabase(): List<ToiletModel>  {
        return try {
            transaction{
                Toilet.selectAll().map { row ->
                    val toiletId = row[Toilet.id]
                    val notes = Note.select { Note.toiletId eq toiletId }.map {
                        val imageBytes = it[Note.img]?.bytes ?: ByteArray(0)
                        NoteModel(
                            id = it[Note.id],
                            userId = it[Note.userId],
                            note = it[Note.note],
                            toiletId = it[Note.toiletId],
                            image = imageBytes,
                            addDate = it[Note.addDate].toString(),
                        )
                    }

                    val votes = Vote.select { Vote.toiletId eq toiletId }.map {
                        VoteModel(
                            id = it[Vote.id],
                            userId = it[Vote.userId],
                            toiletId = it[Vote.toiletId],
                            value = it[Vote.value]
                        )
                    }
                    val tagRow = Tag.select { Tag.toiletId eq toiletId }.singleOrNull()
                    val tags = tagRow?.let {
                        TagModel(
                            toiletId = it[Tag.toiletId],
                            babyRoom = it[Tag.babyRoom] ?: false,
                            accessible = it[Tag.accessible] ?: false
                        )
                    } ?: TagModel()

                    ToiletModel(
                        id = toiletId,
                        name = row[Toilet.name],
                        addDate = row[Toilet.addDate].toString(),
                        category = row[Toilet.category],
                        tags = tags,
                        entryMethod = row[Toilet.entryMethod],
                        priceCHF = row[Toilet.priceCHF],
                        code = row[Toilet.code],
                        latitude = row[Toilet.latitude],
                        longitude = row[Toilet.longitude],
                        notes = notes,
                        votes = votes,
                    )
                }
            }
        } catch (e:Exception){
            debugLog("fetchToiletsFromDatabase error: ${e.message}")
            return emptyList()
        }
    }


    private suspend fun fetchGoogleToilets(lat: Double, lng: Double): List<ToiletModel> {
        try {
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
                    id = nextId++,
                    name = obj["name"]?.jsonPrimitive?.content ?: "Unknown WC",
                    tags = TagModel(),
                    latitude = location["lat"]!!.jsonPrimitive.double,
                    longitude = location["lng"]!!.jsonPrimitive.double,
                )
            }
        }catch (e:Exception){
            debugLog("fetchGoogleToilets error: ${e.message}")
            return emptyList()
        }

    }

    fun fetchToilet(id: String): ToiletModel? {
        return try {
            transaction{
                toilets.find { it.id.toString() == id }
            }
        }catch (e:Exception){
            debugLog("fetchGoogleToilets error: ${e.message}")
            null
        }
    }

    fun addToilet(request: ToiletRequest, userEmail: String): ToiletModel? {
        return try {
            transaction {
                val toiletId = Toilet.insert {
                    it[name] = request.name
                    it[addDate] = Instant.now()
                    it[category] = request.category
                    it[entryMethod] = request.entryMethod
                    it[priceCHF] = request.priceCHF
                    it[code] = request.code
                    it[latitude] = request.latitude
                    it[longitude] = request.longitude
                }[Toilet.id]

                Tag.insert {
                    it[Tag.toiletId] = toiletId
                    it[Tag.babyRoom] = request.tags?.babyRoom
                    it[Tag.accessible] = request.tags?.accessible
                }

                val userId = User.select { User.email eq userEmail }
                    .map { it[User.id] }
                    .firstOrNull() ?: throw IllegalStateException("User not found")

                val votes = mutableListOf<VoteModel>()
                val notes = mutableListOf<NoteModel>()

                if (request.vote != null) {
                    Vote.insert {
                        it[Vote.toiletId] = toiletId
                        it[Vote.userId] = userId
                        it[value] = if (request.vote > 0) 1 else -1
                    }
                    votes.add(
                        VoteModel(
                        id = 0,
                        userId = userId,
                        toiletId = toiletId,
                        value = request.vote
                    )
                    )
                }

                if (!request.note.isNullOrBlank()) {
                    val imageBytes: ByteArray? = request.img?.let {
                        Base64.getDecoder().decode(it)
                    }

                    Note.insert {
                        it[Note.toiletId] = toiletId
                        it[Note.userId] = userId
                        it[note] = request.note
                        it[img] = ExposedBlob(imageBytes ?: ByteArray(0))
                        it[addDate] = Instant.now()
                    }
                    notes.add(
                        NoteModel(
                        id = 0,
                        userId = userId,
                        toiletId = toiletId,
                        note = request.note,
                        image = imageBytes,
                        addDate = Instant.now().toString()
                    )
                    )
                }

                ToiletModel(
                    id = toiletId,
                    name = request.name,
                    category = request.category,
                    tags = TagModel(
                        toiletId = toiletId,
                        babyRoom = request.tags?.babyRoom,
                        accessible = request.tags?.accessible
                    ),
                    entryMethod = request.entryMethod,
                    priceCHF = request.priceCHF,
                    code = request.code,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    notes = notes,
                    votes = votes,
                    addDate = Instant.now().toString()
                )
            }
        } catch (e: Exception) {
            debugLog("addToilet error: ${e.message}")
            null
        }
    }

    fun addVote(toiletID: Int, userID: Int, voteValue: Int): Boolean {
        return try {
            transaction {
                val updated = Vote.update({ (Vote.toiletId eq toiletID) and (Vote.userId eq userID) }) {
                    it[value] = voteValue
                }

                if (updated == 0) {
                    Vote.insert {
                        it[Vote.toiletId] = toiletID
                        it[Vote.userId] = userID
                        it[value] = voteValue
                    }
                }
            }
           true
        } catch (e: Exception) {
            debugLog("addVote error: ${e.message}")
            false
        }
    }

    fun addNote(toiletID: Int, userID: Int, noteText: String, imageBytes: ByteArray? = null): Boolean {
        return try {
            transaction {
                val existing = Note.select {
                    (Note.toiletId eq toiletID) and (Note.userId eq userID)
                }.singleOrNull()

                if (existing != null) {
                    Note.update({ (Note.toiletId eq toiletID) and (Note.userId eq userID) }) {
                        it[note] = noteText
                        it[addDate] = Instant.now()
                        it[img] = imageBytes?.let { it1 -> ExposedBlob(it1) }
                    }
                } else {
                    Note.insert {
                        it[Note.toiletId] = toiletID
                        it[Note.userId] = userID
                        it[note] = noteText
                        it[addDate] = Instant.now()
                        it[img] = imageBytes?.let { it1 -> ExposedBlob(it1) }
                    }
                }
            }
           true
        } catch (e: Exception) {
            debugLog("addNote error: ${e.message}")
            false
        }
    }

    fun removeNote(toiletID: Int, userID: Int): Boolean {
        return try {
            transaction {
                val deleted = Note.deleteWhere {
                    (Note.toiletId eq toiletID) and (Note.userId eq userID)
                }

                if (deleted > 0) true else false
            }
        } catch (e: Exception) {
            debugLog("removeNote error: ${e.message}")
            false
        }
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