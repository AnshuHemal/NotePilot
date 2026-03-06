package com.white.notepilot.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.white.notepilot.data.model.Note
import com.white.notepilot.utils.FirebaseConstants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun syncNoteToFirestore(
        note: Note,
        userId: String,
        firestoreId: String? = null
    ) : Result<String> {
        return try {
            val noteData = hashMapOf(
                FirebaseConstants.FIELD_TITLE to note.title,
                FirebaseConstants.FIELD_CONTENT to note.content,
                FirebaseConstants.FIELD_COLOR_CODE to note.colorCode,
                FirebaseConstants.FIELD_TIMESTAMP to note.timestamp,
                FirebaseConstants.FIELD_LOCAL_ID to note.id,
                FirebaseConstants.FIELD_USER_ID to userId,
            )

            val docRef = if (firestoreId != null) {
                // Update existing document
                getUserNotesCollection(userId).document(firestoreId).set(noteData).await()
                firestoreId
            } else {
                // Check if a document with the same content already exists
                val existingDoc = getUserNotesCollection(userId)
                    .whereEqualTo(FirebaseConstants.FIELD_TITLE, note.title)
                    .whereEqualTo(FirebaseConstants.FIELD_CONTENT, note.content)
                    .limit(1)
                    .get()
                    .await()
                
                if (!existingDoc.isEmpty) {
                    // Document with same content exists, update it instead of creating new
                    val existingDocId = existingDoc.documents[0].id
                    getUserNotesCollection(userId).document(existingDocId).set(noteData).await()
                    existingDocId
                } else {
                    // Create new document
                    val newDocRef = getUserNotesCollection(userId)
                        .add(noteData)
                        .await()
                    newDocRef.id
                }
            }
            Result.success(docRef)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteNoteFromFirestore(
        firestoreId: String,
        userId: String
    ): Result<Unit> {
        return try {
            getUserNotesCollection(userId)
                .document(firestoreId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserNotes(userId: String) : Result<List<Map<String, Any>>> {
        return try {
            val snapshot = getUserNotesCollection(userId)
                .orderBy(FirebaseConstants.FIELD_TIMESTAMP, Query.Direction.DESCENDING)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    data.toMutableMap().apply {
                        put("firestoreId", doc.id)
                    }
                }
            }

            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAllUserNotes(userId: String): Result<Unit> {
        return try {
            val snapshot = getUserNotesCollection(userId).get().await()

            val batch = firestore.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAllNotesToFirestore(
        notes: List<Note>,
        userId: String
    ): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val collection = getUserNotesCollection(userId)

            notes.forEach { note ->
                val noteData = hashMapOf(
                    FirebaseConstants.FIELD_TITLE to note.title,
                    FirebaseConstants.FIELD_CONTENT to note.content,
                    FirebaseConstants.FIELD_COLOR_CODE to note.colorCode,
                    FirebaseConstants.FIELD_TIMESTAMP to note.timestamp,
                    FirebaseConstants.FIELD_LOCAL_ID to note.id,
                    FirebaseConstants.FIELD_USER_ID to userId
                )

                val docRef = collection.document()
                batch.set(docRef, noteData)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToUserNotes(userId: String): Flow<Result<List<Map<String, Any>>>> = callbackFlow {
        val listener = getUserNotesCollection(userId)
            .orderBy(FirebaseConstants.FIELD_TIMESTAMP, Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val notes = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data ->
                            data.toMutableMap().apply {
                                put("firestoreId", doc.id)
                            }
                        }
                    }
                    trySend(Result.success(notes))
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun getNoteByFirestoreId(
        firestoreId: String,
        userId: String
    ): Result<Map<String, Any>?> {
        return try {
            val doc = getUserNotesCollection(userId)
                .document(firestoreId)
                .get()
                .await()

            val noteData = doc.data?.toMutableMap()?.apply {
                put("firestoreId", doc.id)
            }

            Result.success(noteData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getUserNotesCollection(userId: String) =
        firestore.collection(FirebaseConstants.USERS_COLLECTION)
            .document(userId)
            .collection(FirebaseConstants.NOTES_COLLECTION)
}