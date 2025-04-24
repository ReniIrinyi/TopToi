package util

import database.model.*
import dto.*
import java.util.*

object DtoMapper {
    fun User.toDTO(): UserDTO = UserDTO(
        id = id,
        email = email,
        name = name,
        authProvider = authProvider.toString(),
        imgUrl = imgUrl,
        imgBase64 = img?.takeIf { it.isNotEmpty() }?.let { Base64.getEncoder().encodeToString(it) },
        notes = notes.map { it.toDTO() },
        votes = votes.map { it.toDTO() }
    )

    fun NoteModel.toDTO(): NoteDTO = NoteDTO(
        id = id,
        toiletId = toiletId,
        userId = userId,
        note = note,
        addDate = addDate,
        imageBase64 = image?.takeIf { it.isNotEmpty() }?.let { Base64.getEncoder().encodeToString(it) }
    )

    fun VoteModel.toDTO(): VoteDTO = VoteDTO(
        id = id,
        toiletId = toiletId,
        userId = userId,
        value = value
    )

    fun TagModel.toDTO(): TagDTO = TagDTO(
        babyRoom = babyRoom,
        accessible = accessible
    )

    fun ToiletModel.toDTO(): ToiletDTO = ToiletDTO(
        id = id,
        name = name,
        addDate = addDate,
        category = category,
        entryMethod = entryMethod,
        priceCHF = priceCHF,
        code = code,
        latitude = latitude,
        longitude = longitude,
        tags = tags?.toDTO(),
        notes = notes.map { it.toDTO() },
        votes = votes.map { it.toDTO() }
    )

}