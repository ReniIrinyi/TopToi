package adapter.persistence.database.repository

import adapter.persistence.database.table.AuthProvider
import adapter.persistence.database.table.Note
import adapter.persistence.database.table.User
import adapter.persistence.database.table.Vote
import common.LoggerService.debugLog
import domain.model.NoteModel
import domain.model.UserModel
import domain.model.VoteModel
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserRepository  {
     fun createLocalUser(user: UserModel): Boolean {
        return try {
            transaction {
                if (User.select { User.email eq user.email }.count() > 0) return@transaction false

                User.insert {
                    it[email] = user.email
                    it[password] = BCrypt.hashpw(user.password, BCrypt.gensalt())
                    it[authProvider] = AuthProvider.ENUM_LOCAL
                    it[name] = user.name
                    it[imgUrl] = user.imgUrl
                    it[img] = user.img?.let { bytes -> org.jetbrains.exposed.sql.statements.api.ExposedBlob(bytes) }
                }
                true
            }
        } catch (e: Exception) {
            debugLog("createLocalUser error: ${e.message}")
            false
        }
    }

     fun createGoogleUser(user: UserModel): Boolean {
        return try {
            transaction {
                User.insert {
                    it[email] = user.email
                    it[password] = user.password
                    it[authProvider] = AuthProvider.ENUM_GOOGLE
                    it[name] = user.name
                    it[imgUrl] = user.imgUrl
                    it[img] = user.img?.let { bytes -> org.jetbrains.exposed.sql.statements.api.ExposedBlob(bytes) }
                }
                true
            }
        } catch (e: Exception) {
            debugLog("createGoogleUser error: ${e.message}")
            false
        }
    }


     fun authenticateLocalUser(user: UserModel): Boolean {
        return try {
            transaction {
                val userRow = User.select { User.email eq user.email }.singleOrNull() ?: return@transaction false
                if (userRow[User.authProvider] != AuthProvider.ENUM_LOCAL) return@transaction false
                BCrypt.checkpw(user.password, userRow[User.password])
            }
        } catch (e: Exception) {
            debugLog("authenticateLocalUser error: ${e.message}")
            false
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

     fun getUserByEmail(email: String): UserModel? {
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
                                img = noteRow[Note.img]?.bytes
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

                        UserModel(
                            id = userId,
                            email = it[User.email],
                            name = it[User.name] ?: "",
                            authProvider = it[User.authProvider].toString(),
                            imgUrl = it[User.imgUrl],
                            img = it[User.img]?.bytes,
                            notes = notes,
                            votes = votes,
                            password = it[User.password] ?: ""
                        )
                    }.singleOrNull()
            }
        } catch (e: Exception) {
            debugLog("getUserByEmail error: ${e.message}")
            null
        }
    }
}
