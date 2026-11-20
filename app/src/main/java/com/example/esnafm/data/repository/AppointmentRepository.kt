package com.example.esnafm.data.repository

import com.example.esnafm.data.model.AppointmentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firestore "appointments" koleksiyonundaki randevu ve hizmet talebi işlemlerini yöneten Repository.
 */
class AppointmentRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val appointmentsCollection = firestore.collection("appointments")

    /**
     * Müşterinin kendi oluşturduğu randevu taleplerini canlı dinleyen Flow.
     */
    fun getCustomerAppointmentsFlow(customerId: String): Flow<List<AppointmentModel>> = callbackFlow {
        if (customerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = appointmentsCollection
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointments = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data -> AppointmentModel.fromMap(doc.id, data) }
                    }.sortedByDescending { it.createdAt }
                    trySend(appointments)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Esnafa gelen randevu taleplerini canlı dinleyen Flow.
     */
    fun getMerchantAppointmentsFlow(merchantId: String): Flow<List<AppointmentModel>> = callbackFlow {
        if (merchantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = appointmentsCollection
            .whereEqualTo("merchantId", merchantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointments = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data -> AppointmentModel.fromMap(doc.id, data) }
                    }.sortedByDescending { it.createdAt }
                    trySend(appointments)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Yeni bir randevu talebi oluşturur.
     */
    suspend fun createAppointment(appointment: AppointmentModel): Result<String> = withContext(Dispatchers.IO) {
        try {
            val docRef = appointmentsCollection.document()
            val newAppointment = appointment.copy(id = docRef.id)
            docRef.set(newAppointment.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Esnaf tarafından randevu durumunu günceller ("approved", "rejected", "cancelled").
     */
    suspend fun updateAppointmentStatus(appointmentId: String, newStatus: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(appointmentId.isNotBlank()) { "Randevu ID boş olamaz." }
            require(newStatus in setOf("approved", "rejected")) { "Randevu durumu geçersiz." }
            appointmentsCollection.document(appointmentId)
                .update("status", newStatus)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Müşteri başlamamış randevusunu iptal eder; yatırılmış demo depozito demo iadesi olarak işaretlenir. */
    suspend fun cancelCustomerAppointment(appointmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: error("Randevuyu iptal etmek için giriş yapmalısınız.")
            require(appointmentId.isNotBlank()) { "Randevu bulunamadı." }
            val appointmentRef = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(appointmentRef)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                val appointment = AppointmentModel.fromMap(snapshot.id, data)
                require(appointment.customerId == uid) { "Bu randevu size ait değil." }
                require(appointment.status in setOf("pending", "approved")) {
                    "Hizmet başladıktan sonra randevu uygulamadan iptal edilemez. Esnafla iletişime geçin."
                }

                val now = System.currentTimeMillis()
                val updates = mutableMapOf<String, Any>(
                    "status" to "cancelled",
                    "cancelledAt" to now,
                    "cancellationReason" to "customer_cancelled"
                )
                if (appointment.depositPaymentStatus == "demo_paid") {
                    updates["depositPaymentStatus"] = "demo_refunded"
                    updates["depositRefundAt"] = now
                }
                transaction.update(appointmentRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markDepositPaidDemo(appointmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: error("Ödeme için giriş yapmalısınız.")
            val ref = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(ref)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                val appointment = AppointmentModel.fromMap(snapshot.id, data)
                require(appointment.customerId == uid) { "Bu randevu size ait değil." }
                require(appointment.status == "approved") { "Depozito yalnızca esnaf onayından sonra demo olarak ödenebilir." }
                require(appointment.depositAmount > 0.0 && appointment.depositPaymentStatus == "unpaid") {
                    "Ödenecek depozito bulunmuyor veya daha önce işlendi."
                }
                tx.update(ref, mapOf(
                    "depositPaymentStatus" to "demo_paid",
                    "depositPaidAt" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markBalancePaidDemo(appointmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: error("Ödeme için giriş yapmalısınız.")
            val ref = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(ref)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                val appointment = AppointmentModel.fromMap(snapshot.id, data)
                require(appointment.customerId == uid) { "Bu randevu size ait değil." }
                require(appointment.status == "service_completed" && appointment.balancePaymentStatus == "requested") {
                    "Esnaf henüz kalan ödeme talebi göndermedi."
                }
                tx.update(ref, mapOf(
                    "status" to "completed",
                    "balancePaymentStatus" to "demo_paid",
                    "balancePaidAt" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeServiceAndRequestBalance(appointmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: error("Oturum bulunamadı.")
            val ref = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(ref)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                val appointment = AppointmentModel.fromMap(snapshot.id, data)
                require(appointment.merchantId == uid) { "Bu randevuyu güncelleme yetkiniz yok." }
                require(appointment.status == "approved") { "Yalnızca onaylanmış hizmet tamamlanabilir." }
                require(appointment.depositPaymentStatus in setOf("not_required", "demo_paid", "cash_paid")) {
                    "Hizmet tamamlanmadan önce depozito demo olarak işlenmelidir."
                }
                val remaining = (appointment.price - appointment.depositAmount).coerceAtLeast(0.0)
                require(remaining > 0.0) { "Talep edilecek kalan tutar yok." }
                tx.update(ref, mapOf(
                    "status" to "service_completed",
                    "balancePaymentStatus" to "requested",
                    "balancePaymentRequestedAt" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeServiceWithCash(appointmentId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: error("Oturum bulunamadı.")
            val ref = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { tx ->
                val snapshot = tx.get(ref)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                val appointment = AppointmentModel.fromMap(snapshot.id, data)
                require(appointment.merchantId == uid) { "Bu randevuyu güncelleme yetkiniz yok." }
                require(appointment.status in setOf("approved", "service_completed")) {
                    "Bu randevu artık tamamlanamaz."
                }
                require(appointment.depositPaymentStatus in setOf("not_required", "demo_paid", "cash_paid")) {
                    "Hizmet tamamlanmadan önce depozito demo olarak işlenmelidir."
                }
                tx.update(ref, mapOf(
                    "status" to "completed",
                    "balancePaymentStatus" to "cash_paid",
                    "balancePaidAt" to System.currentTimeMillis()
                ))
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
