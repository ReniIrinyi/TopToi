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
import database.table.Note
import database.table.Toilet
import database.table.User
import database.table.Vote
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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

    fun registerUser(request: UserRequest): Boolean {
        val hashedPassword = BCrypt.hashpw(request.password, BCrypt.gensalt())
        return transaction {
            val exists = User.select { User.email eq request.email }.count() > 0
            if (exists) return@transaction false
            User.insert {
                it[email] = request.email
                it[passwordHash] = hashedPassword
            }
            true
        }
    }

    fun authenticateUser(request: UserRequest): Boolean {
        return transaction {
            val userRow = User.select { User.email eq request.email }.singleOrNull() ?: return@transaction false
            val hash = userRow[User.passwordHash]
            BCrypt.checkpw(request.password, hash)
        }
    }

    fun verifyGoogleIdToken(idToken: String): String? {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(listOf("1030506683349-ism7bd2gihcggefm9gmdsejb6lelcq6d.apps.googleusercontent.com"))
            .build()

        val clientSchlüssel = "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"
        val token: GoogleIdToken? = verifier.verify(idToken)
        val email = token?.payload?.email ?: return null

        transaction {
            val exists = User.select { User.email eq email }.count() > 0
            if (!exists) {
                User.insert {
                    it[User.email] = email
                    it[passwordHash] = "google_oauth"
                }
            }
        }

        return email
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
                    NoteModel(
                        userId = it[Note.userId].toString(),
                        addDate = it[Note.addDate],
                        text = it[Note.text]
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
                    addDate = row[Toilet.addDate],
                    category = row[Toilet.category],
                    openHours = listOf(), // TODO
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
                    votes = votes
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

    fun addToilet(request: ToiletRequest): ToiletModel {
        val toiletId = try {
            transaction {
                Toilet.insert {
                    it[name] = request.name
                    it[addDate] = request.addDate
                    it[category] = request.category
                    it[entryMethod] = request.entryMethod
                    it[priceCHF] = request.priceCHF
                    it[code] = request.code
                    it[latitude] = request.latitude
                    it[longitude] = request.longitude
                    it[babyRoom] = request.tags.BABY_ROOM
                    it[wheelchairAccessible] = request.tags.WHEELCHAIR_ACCESSIBLE
                } get Toilet.id
            }
        }
            catch (e:Exception){
                e.printStackTrace()
                return ToiletModel(
                    id = TODO(),
                    userId = TODO(),
                    name = TODO(),
                    addDate = TODO(),
                    category = TODO(),
                    openHours = TODO(),
                    tags = TODO(),
                    entryMethod = TODO(),
                    priceCHF = TODO(),
                    code = TODO(),
                    latitude = TODO(),
                    longitude = TODO(),
                    notes = TODO(),
                    votes = TODO()
                )
            }
        return ToiletModel(
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

    fun addNote(toiletID: String, userID: String, noteText: String): String {
        if (noteText.length < 3) return "Note is too short!"
        val toilet = fetchToilet(toiletID) ?: return "Toilet not found"
        val newNote = NoteModel(userId = userID, addDate = Instant.now().toString(), text = noteText)
        val newNotes = toilet.notes.toMutableList()
        val index = newNotes.indexOfFirst { it.userId == userID }
        if (index == -1) {
            newNotes.add(newNote)
        } else {
            newNotes[index] = newNote
        }
        val updatedToilet = toilet.copy(notes = newNotes.sortedByDescending { it.addDate })
        toilets.removeIf { it.id.toString() == toiletID }
        toilets.add(updatedToilet)
        return "Note added/updated"
    }

    fun updateNote(toiletID: String, userID: String, noteText: String): String {
        return addNote(toiletID, userID, noteText)
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