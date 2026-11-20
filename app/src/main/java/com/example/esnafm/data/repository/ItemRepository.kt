package com.example.esnafm.data.repository

import com.example.esnafm.data.model.ItemModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore "items" koleksiyonundaki veri ekleme, silme, güncelleme ve canlı dinleme işlemlerini yöneten Repository.
 */
class ItemRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val itemsCollection = firestore.collection("items")

    /**
     * "items" koleksiyonunu gerçek zamanlı (Snapshot Listener) dinleyen Flow.
     * Veritabanında ürün eklendiğinde, silindiğinde veya güncellendiğinde otomatik tetiklenir.
     */
    fun getItemsFlow(): Flow<List<ItemModel>> = callbackFlow {
        val listenerRegistration = itemsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Yedeğe geç: orderBy olmadan dinle
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data ->
                            ItemModel.fromMap(doc.id, data)
                        }
                    }
                    trySend(items)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * "items" koleksiyonuna yeni bir ürün/hizmet ekler.
     * Otomatik döküman ID'si oluşturur ve yazma işlemini gerçekleştirir.
     */
    suspend fun addItem(item: ItemModel): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = itemsCollection.document()
            val newItem = item.copy(id = docRef.id)
            docRef.set(newItem.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * "items" koleksiyonundaki tüm ürünleri tek seferlik Firestore'dan çeker.
     */
    suspend fun getItems(): Result<List<ItemModel>> = withContext(Dispatchers.IO) {
        try {
            val snapshot = itemsCollection.get().await()
            val itemsList = snapshot.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    ItemModel.fromMap(doc.id, data)
                }
            }
            Result.success(itemsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Firestore "items" koleksiyonundan belirtilen id ve merchantId'ye sahip ürünü siler.
     * Güvenlik kontrolü: Yalnızca dükkan sahibi veya yetkili esnaf silebilir.
     */
    suspend fun deleteItem(itemId: String, currentMerchantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(itemId.isNotBlank()) { "Silinecek ürün ID'si boş olamaz." }

            val docSnapshot = itemsCollection.document(itemId).get().await()
            if (!docSnapshot.exists()) {
                return@withContext Result.failure(Exception("Ürün veritabanında bulunamadı."))
            }

            val itemMerchantId = docSnapshot.getString("merchantId") ?: ""
            // Ürünün merchantId'si dolu ise ve işlem yapan esnaf ID'si ile uyuşmuyorsa silmeyi engelle
            if (itemMerchantId.isNotBlank() && currentMerchantId.isNotBlank() && itemMerchantId != currentMerchantId) {
                return@withContext Result.failure(Exception("Bu ürünü silme yetkiniz bulunmamaktadır."))
            }

            itemsCollection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * "items" koleksiyonundaki mevcut bir ürünü günceller.
     */
    suspend fun updateItem(item: ItemModel): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(item.id.isNotBlank()) { "Güncellenecek ürünün ID'si boş olamaz." }
            itemsCollection.document(item.id).update(item.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
