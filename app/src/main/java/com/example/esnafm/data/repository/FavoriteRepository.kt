package com.example.esnafm.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/** Stores each customer's favorites under their private user document. */
class FavoriteRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun observeFavorites(uid: String): Flow<Set<String>> = callbackFlow {
        if (uid.isBlank() || auth.currentUser?.uid != uid) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(uid)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) trySend(snapshot.documents.map { it.id }.toSet())
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun setFavorite(uid: String, merchantId: String, favorite: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(uid.isNotBlank() && uid == auth.currentUser?.uid) { "Favorilerinizi yalnızca kendi hesabınızda değiştirebilirsiniz." }
            require(merchantId.isNotBlank() && merchantId != uid) { "İşletme seçimi geçersiz." }
            val ref = firestore.collection("users").document(uid).collection("favorites").document(merchantId)
            if (favorite) {
                ref.set(mapOf("merchantId" to merchantId, "createdAt" to System.currentTimeMillis())).await()
            } else {
                ref.delete().await()
            }
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}
