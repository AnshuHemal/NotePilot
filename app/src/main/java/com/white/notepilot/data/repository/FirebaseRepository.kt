package com.white.notepilot.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.white.notepilot.data.model.Note
import com.white.notepilot.utils.FirebaseConstants
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
                getUserNotesCollection(userId).document(firestoreId).set(noteData).await()
                firestoreId
            } else {
                val existingDoc = getUserNotesCollection(userId)
                    .whereEqualTo(FirebaseConstants.FIELD_TITLE, note.title)
                    .whereEqualTo(FirebaseConstants.FIELD_CONTENT, note.content)
                    .limit(1)
                    .get()
                    .await()
                
                if (!existingDoc.isEmpty) {
                    val existingDocId = existingDoc.documents[0].id
                    getUserNotesCollection(userId).document(existingDocId).set(noteData).await()
                    existingDocId
                } else {
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

    private fun getUserNotesCollection(userId: String) =
        firestore.collection(FirebaseConstants.USERS_COLLECTION)
            .document(userId)
            .collection(FirebaseConstants.NOTES_COLLECTION)
    
    suspend fun syncCategoryToFirestore(
        category: com.white.notepilot.data.model.Category,
        userId: String,
        firestoreId: String? = null
    ): Result<String> {
        return try {
            val categoryData = hashMapOf(
                "name" to category.name,
                "color" to category.color,
                "icon" to category.icon,
                "created_at" to category.createdAt,
                "local_id" to category.id,
                "user_id" to userId
            )
            
            val docRef = if (firestoreId != null) {
                getUserCategoriesCollection(userId).document(firestoreId).set(categoryData).await()
                firestoreId
            } else {
                val existingDoc = getUserCategoriesCollection(userId)
                    .whereEqualTo("name", category.name)
                    .limit(1)
                    .get()
                    .await()
                
                if (!existingDoc.isEmpty) {
                    val existingDocId = existingDoc.documents[0].id
                    getUserCategoriesCollection(userId).document(existingDocId).set(categoryData).await()
                    existingDocId
                } else {
                    val newDocRef = getUserCategoriesCollection(userId).add(categoryData).await()
                    newDocRef.id
                }
            }
            Result.success(docRef)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun fetchUserCategories(userId: String): Result<List<Map<String, Any>>> {
        return try {
            val snapshot = getUserCategoriesCollection(userId)
                .orderBy("name")
                .get()
                .await()
            
            val categories = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    data.toMutableMap().apply {
                        put("firestoreId", doc.id)
                    }
                }
            }
            
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategoryFromFirestore(
        firestoreId: String,
        userId: String
    ): Result<Unit> {
        return try {
            getUserCategoriesCollection(userId)
                .document(firestoreId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getUserCategoriesCollection(userId: String) =
        firestore.collection(FirebaseConstants.USERS_COLLECTION)
            .document(userId)
            .collection("categories")

    suspend fun submitFeedback(
        feedback: com.white.notepilot.data.model.Feedback
    ): Result<String> {
        return try {
            val feedbackData = hashMapOf(
                "userId" to feedback.userId,
                "userEmail" to feedback.userEmail,
                "feedbackType" to feedback.feedbackType.name,
                "subject" to feedback.subject,
                "description" to feedback.description,
                "deviceInfo" to feedback.deviceInfo,
                "appVersion" to feedback.appVersion,
                "attachmentUrls" to feedback.attachmentUrls,
                "timestamp" to feedback.timestamp,
                "status" to feedback.status.name
            )
            
            val docRef = firestore.collection("feedback")
                .add(feedbackData)
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}