package com.example.esnafm.data.repository

import com.example.esnafm.data.model.AddressModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Kullanıcı Adres İşlemlerinden Sorumlu Repository Katmanı
 */
class AddressRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun getUserAddressesFlow(uid: String): Flow<List<AddressModel>> = callbackFlow {
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = firestore.collection("users")
            .document(uid)
            .collection("addresses")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val addresses = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { AddressModel.fromMap(doc.id, it) }
                    }
                    trySend(addresses)
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun saveAddress(address: AddressModel): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val docRef = if (address.id.isBlank()) {
                firestore.collection("users").document(address.uid).collection("addresses").document()
            } else {
                firestore.collection("users").document(address.uid).collection("addresses").document(address.id)
            }
            docRef.set(address.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(uid: String, addressId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("users")
                .document(uid)
                .collection("addresses")
                .document(addressId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}