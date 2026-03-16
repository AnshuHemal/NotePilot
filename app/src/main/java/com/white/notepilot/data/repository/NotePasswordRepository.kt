package com.white.notepilot.data.repository

import com.white.notepilot.data.dao.NotePasswordDao
import com.white.notepilot.data.model.NotePassword
import com.white.notepilot.utils.NetworkUtils
import com.white.notepilot.utils.PasswordHelper
import javax.inject.Inject

class NotePasswordRepository @Inject constructor(
    private val passwordDao: NotePasswordDao,
    private val firebaseRepository: FirebaseRepository,
    private val networkUtils: NetworkUtils
) {
    
    suspend fun setPasswordForNote(
        noteId: Int,
        noteFirebaseId: String?,
        password: String,
        userId: String,
        lockType: String = "PASSWORD"
    ): Result<Unit> {
        return try {
            val passwordHash = if (lockType == "BIOMETRIC") {
                // For biometric, we store a placeholder hash
                "BIOMETRIC_AUTH"
            } else {
                PasswordHelper.hashPassword(password)
            }
            
            val notePassword = NotePassword(
                noteId = noteId,
                noteFirebaseId = noteFirebaseId,
                passwordHash = passwordHash,
                lockType = lockType,
                isSynced = false
            )
            
            passwordDao.insertPassword(notePassword)
            
            if (networkUtils.isNetworkAvailable() && !noteFirebaseId.isNullOrBlank()) {
                val syncResult = firebaseRepository.syncNotePasswordToFirestore(
                    noteFirebaseId = noteFirebaseId,
                    userId = userId,
                    passwordHash = passwordHash
                )
                
                if (syncResult.isSuccess) {
                    passwordDao.markPasswordAsSynced(noteId)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun verifyPasswordForNote(noteId: Int, password: String): Boolean {
        return try {
            val storedPassword = passwordDao.getPasswordForNote(noteId)
            storedPassword?.let {
                PasswordHelper.verifyPassword(password, it.passwordHash)
            } ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    suspend fun removePasswordForNote(noteId: Int, noteFirebaseId: String?, userId: String): Result<Unit> {
        return try {
            passwordDao.deletePasswordForNote(noteId)
            
            if (networkUtils.isNetworkAvailable() && !noteFirebaseId.isNullOrBlank()) {
                firebaseRepository.removeNotePasswordFromFirestore(noteFirebaseId, userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun hasPassword(noteId: Int): Boolean {
        return passwordDao.getPasswordForNote(noteId) != null
    }
    
    suspend fun syncUnsyncedPasswords(userId: String): Result<Int> {
        return try {
            val unsyncedPasswords = passwordDao.getUnsyncedPasswords()
            var syncedCount = 0
            
            unsyncedPasswords.forEach { password ->
                if (!password.noteFirebaseId.isNullOrBlank()) {
                    val result = firebaseRepository.syncNotePasswordToFirestore(
                        noteFirebaseId = password.noteFirebaseId,
                        userId = userId,
                        passwordHash = password.passwordHash
                    )
                    
                    if (result.isSuccess) {
                        passwordDao.markPasswordAsSynced(password.noteId)
                        syncedCount++
                    }
                }
            }
            
            Result.success(syncedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun setPasswordHashForNote(
        noteId: Int,
        noteFirebaseId: String?,
        passwordHash: String,
        userId: String
    ) {
        val notePassword = NotePassword(
            noteId = noteId,
            noteFirebaseId = noteFirebaseId,
            passwordHash = passwordHash,
            isSynced = false
        )
        passwordDao.insertPasswordSync(notePassword)
    }
    
    suspend fun setPasswordHashForNoteAsync(
        noteId: Int,
        noteFirebaseId: String?,
        passwordHash: String,
        userId: String
    ) {
        val notePassword = NotePassword(
            noteId = noteId,
            noteFirebaseId = noteFirebaseId,
            passwordHash = passwordHash,
            isSynced = false
        )
        passwordDao.insertPassword(notePassword)
    }
    
    suspend fun getPasswordHash(noteId: Int): String? {
        return try {
            val notePassword = passwordDao.getPasswordForNote(noteId)
            notePassword?.passwordHash
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun getLockType(noteId: Int): String? {
        return try {
            passwordDao.getLockType(noteId)
        } catch (e: Exception) {
            null
        }
    }
}
