package adapter.persistence.database.repository

import adapter.persistence.database.table.Note
import adapter.persistence.database.table.Tag
import adapter.persistence.database.table.Toilet
import adapter.persistence.database.table.User
import adapter.persistence.database.table.Vote
import application.dto.ToiletDTO
import common.LoggerService.debugLog
import domain.model.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.util.*

 class ToiletRepository {

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
                            img = imageBytes,
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

    fun fetchToilet(id: Int): ToiletModel? {
        return try {
            transaction{
                Toilet.select { Toilet.id eq id }
                    .mapNotNull { row ->
                        ToiletModel(
                            id = row[Toilet.id],
                            name = row[Toilet.name],
                            addDate = row[Toilet.addDate].toString(),
                            category = row[Toilet.category],
                            entryMethod = row[Toilet.entryMethod],
                            priceCHF = row[Toilet.priceCHF],
                            code = row[Toilet.code],
                            latitude = row[Toilet.latitude],
                            longitude = row[Toilet.longitude],
                            tags = TagModel(),
                            notes = emptyList(),
                            votes = emptyList()
                        )
                    }.singleOrNull()
            }
        }catch (e:Exception){
            debugLog("fetchGoogleToilets error: ${e.message}")
            null
        }
    }


    fun updateToilet(request: ToiletModel): Boolean {
        return try {
            val id = request.id ?: return false

            transaction {
                Toilet.update({ Toilet.id eq id }) {
                    it[name] = request.name
                    it[category] = request.category
                    it[entryMethod] = request.entryMethod
                    it[priceCHF] = request.priceCHF
                    it[code] = request.code
                    it[latitude] = request.latitude
                    it[longitude] = request.longitude
                }

                Tag.update({ Tag.toiletId eq id }) {
                    it[babyRoom] = request.tags?.babyRoom ?: false
                    it[accessible] = request.tags?.accessible ?: false
                }
            }
            true
        } catch (e: Exception) {
            debugLog("updateToilet error: ${e.message}")
            false
        }
    }


    fun addToilet(request: ToiletDTO, userEmail: String): ToiletModel? {
        return try {
            transaction {
                val now = Instant.now()

                val toiletId = Toilet.insert {
                    it[name] = request.name
                    it[addDate] = now
                    it[category] = request.category
                    it[entryMethod] = request.entryMethod
                    it[priceCHF] = request.priceCHF
                    it[code] = request.code
                    it[latitude] = request.latitude
                    it[longitude] = request.longitude
                }[Toilet.id]

                val tagModel = request.tags?.let {tagDTO ->
                    Tag.insert {
                        it[Tag.toiletId] = toiletId
                        it[Tag.babyRoom] = tagDTO.babyRoom
                        it[Tag.accessible] = tagDTO.accessible
                    }
                    tagDTO.toModel().copy(toiletId = toiletId)
                }

                val userId = User.select { User.email eq userEmail }
                    .map { it[User.id] }
                    .firstOrNull() ?: throw IllegalStateException("User not found")


                val notes = mutableListOf<NoteModel>()
                val votes = mutableListOf<VoteModel>()

                // ⮕ Save all votes
                request.votes.forEach { voteDTO ->
                    Vote.insert {
                        it[Vote.toiletId] = toiletId
                        it[Vote.userId] = userId
                        it[Vote.value] = voteDTO.value
                    }
                    votes.add(voteDTO.copy(userId = userId, toiletId = toiletId).toModel())
                }

                // ⮕ Save all notes
                request.notes.forEach { noteDTO ->
                    val imageBytes = noteDTO.img?.let {
                        Base64.getDecoder().decode(it)
                    }

                    Note.insert {
                        it[Note.toiletId] = toiletId
                        it[Note.userId] = userId
                        it[Note.note] = noteDTO.note
                        it[Note.img] = ExposedBlob(imageBytes ?: ByteArray(0))
                        it[Note.addDate] = now
                    }

                    notes.add(noteDTO.copy(userId = userId, toiletId = toiletId).toModel())
                }

                return@transaction ToiletModel(
                    id = toiletId,
                    name = request.name,
                    addDate = now.toString(),
                    category = request.category,
                    entryMethod = request.entryMethod,
                    priceCHF = request.priceCHF,
                    code = request.code,
                    latitude = request.latitude,
                    longitude = request.longitude,
                    tags = tagModel ?: TagModel(),
                    votes = votes,
                    notes = notes
                )
            }
        } catch (e: Exception) {
            debugLog("addToilet error: ${e.message}")
            null
        }
    }

}
