package service

import dto.ToiletRequest
import database.model.Note
import database.model.Tags
import database.model.Toilet
import database.model.Vote
import java.time.Instant

class Service{
    private val toilets = mutableListOf<Toilet>()
    private var nextId = 1

    fun fetchToilets(): List<Toilet> {
        return toilets
    }

    fun fetchToilet(id: String): Toilet? {
        return toilets.find { it.id.toString() == id }
    }

    fun addToilet(request: ToiletRequest): Toilet {
        val newToilet = Toilet(
            id = nextId++,
            name = request.name,
            addDate = request.addDate,
            category = request.category,
            openHours = request.openHours,
            tags = Tags(
                BABY_ROOM = request.tags.BABY_ROOM,
                WHEELCHAIR_ACCESSIBLE = request.tags.WHEELCHAIR_ACCESSIBLE
            ),
            entryMethod = request.entryMethod,
            priceHUF = request.priceHUF,
            priceEUR = request.priceEUR,
            code = request.code,
            latitude = request.latitude,
            longitude = request.longitude,
            notes = emptyList(),
            votes = emptyList()
        )
        toilets.add(newToilet)
        return newToilet
    }

    fun vote(toiletID: String, userID: String, voteValue: Int): String {
        val toilet = fetchToilet(toiletID) ?: return "Toilet not found"
        val newVotes = toilet.votes.toMutableList()
        val index = newVotes.indexOfFirst { it.userId == userID }
        when {
            index == -1 && voteValue != 0 -> newVotes.add(Vote(userId = userID, value = voteValue))
            voteValue == 0 && index != -1 -> newVotes.removeAt(index)
            index != -1 -> newVotes[index] = Vote(userId = userID, value = voteValue)
        }
        val updatedToilet = toilet.copy(votes = newVotes)
        toilets.removeIf { it.id.toString() == toiletID }
        toilets.add(updatedToilet)
        return "Vote updated"
    }

    fun addNote(toiletID: String, userID: String, noteText: String): String {
        if (noteText.length < 3) return "Note is too short!"
        val toilet = fetchToilet(toiletID) ?: return "Toilet not found"
        val newNote = Note(userId = userID, addDate = Instant.now().toString(), text = noteText)
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
}