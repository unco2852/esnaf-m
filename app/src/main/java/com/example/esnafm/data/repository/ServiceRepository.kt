package com.example.esnafm.data.repository

import com.example.esnafm.data.model.ServiceModel
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
 * Firestore "services" koleksiyonundaki veri ekleme, silme, güncelleme ve canlı dinleme işlemlerini yöneten Repository.
 */
class ServiceRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val servicesCollection = firestore.collection("services")

    suspend fun getServiceById(serviceId: String): Result<ServiceModel> = withContext(Dispatchers.IO) {
        try {
            require(serviceId.isNotBlank()) { "Hizmet kimliği boş olamaz." }
            val snapshot = servicesCollection.document(serviceId).get().await()
            val data = snapshot.data ?: error("Hizmet bulunamadı.")
            Result.success(ServiceModel.fromMap(snapshot.id, data))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun getMerchantServices(merchantId: String): Result<List<ServiceModel>> = withContext(Dispatchers.IO) {
        try {
            require(merchantId.isNotBlank()) { "İşletme kimliği boş olamaz." }
            val snapshot = servicesCollection.whereEqualTo("merchantId", merchantId).limit(200).get().await()
            Result.success(snapshot.documents.mapNotNull { doc -> doc.data?.let { ServiceModel.fromMap(doc.id, it) } })
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    /**
     * "services" koleksiyonunu gerçek zamanlı (Snapshot Listener) dinleyen Flow.
     */
    fun getServicesFlow(): Flow<List<ServiceModel>> = callbackFlow {
        val listenerRegistration = servicesCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(2000)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val services = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data ->
                            ServiceModel.fromMap(doc.id, data)
                        }
                    }
                    trySend(services)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    fun getMerchantServicesFlow(merchantId: String): Flow<List<ServiceModel>> = callbackFlow {
        if (merchantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val registration = servicesCollection
            .whereEqualTo("merchantId", merchantId)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { doc -> doc.data?.let { ServiceModel.fromMap(doc.id, it) } })
                }
            }
        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    /**
     * "services" koleksiyonuna yeni bir hizmet ekler.
     */
    suspend fun addService(service: ServiceModel): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = servicesCollection.document()
            val newService = service.copy(id = docRef.id)
            docRef.set(newService.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * "services" koleksiyonundaki bir hizmeti günceller.
     */
    suspend fun updateService(service: ServiceModel): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(service.id.isNotBlank()) { "Güncellenecek hizmet ID'si boş olamaz." }
            servicesCollection.document(service.id).update(service.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * "services" koleksiyonundan belirtilen id ve merchantId'ye sahip hizmeti siler.
     */
    suspend fun deleteService(serviceId: String, currentMerchantId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(serviceId.isNotBlank()) { "Silinecek hizmet ID'si boş olamaz." }

            val docSnapshot = servicesCollection.document(serviceId).get().await()
            if (!docSnapshot.exists()) {
                return@withContext Result.failure(Exception("Hizmet bulunamadı."))
            }

            val itemMerchantId = docSnapshot.getString("merchantId") ?: ""
            if (itemMerchantId.isNotBlank() && currentMerchantId.isNotBlank() && itemMerchantId != currentMerchantId) {
                return@withContext Result.failure(Exception("Bu hizmeti silme yetkiniz bulunmamaktadır."))
            }

            servicesCollection.document(serviceId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
