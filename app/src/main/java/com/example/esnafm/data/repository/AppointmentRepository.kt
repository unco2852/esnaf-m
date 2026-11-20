package com.example.esnafm.data.repository

import com.example.esnafm.data.model.AppointmentModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
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
            .limit(200)
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
            .limit(200)
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
            require(appointment.date.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) { "Randevu tarihi geçersiz." }
            require(appointment.time.matches(Regex("^\\d{2}:\\d{2}$"))) { "Randevu saati geçersiz." }
            require(appointment.merchantId.isNotBlank() && appointment.customerId.isNotBlank()) { "Randevu bilgileri eksik." }
            val docRef = appointmentsCollection.document()
            val newAppointment = appointment.copy(id = docRef.id)
            val dateKey = appointment.date.filter(Char::isDigit)
            val timeKey = appointment.time.filter(Char::isDigit)
            val slotRef = slotReference(appointment.merchantId, dateKey, timeKey)
            val resultId = firestore.runTransaction { transaction ->
                val existingSlot = transaction.get(slotRef)
                require(!existingSlot.exists()) { "Bu saat az önce başka bir müşteri tarafından alındı. Lütfen başka saat seçin." }

                val slotData = mapOf(
                    "appointmentId" to docRef.id,
                    "customerId" to appointment.customerId,
                    "merchantId" to appointment.merchantId,
                    "serviceId" to appointment.serviceId,
                    "date" to appointment.date,
                    "time" to appointment.time,
                    "slotDateKey" to dateKey,
                    "slotTimeKey" to timeKey
                )
                transaction.set(slotRef, slotData)
                transaction.set(docRef, newAppointment.toMap() + mapOf("slotDateKey" to dateKey, "slotTimeKey" to timeKey))
                docRef.id
            }.await()
            Result.success(resultId)
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
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: error("Oturum bulunamadı.")
            val appointmentRef = appointmentsCollection.document(appointmentId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(appointmentRef)
                val data = snapshot.data ?: error("Randevu bulunamadı.")
                require(data["merchantId"] == uid) { "Bu randevuyu güncelleme yetkiniz yok." }
                require(data["status"] == "pending") { "Yalnızca bekleyen randevular güncellenebilir." }
                val slotRef = slotReference(data)
                val slotSnapshot = slotRef?.let(transaction::get)
                transaction.update(appointmentRef, "status", newStatus)
                if (newStatus == "rejected" && slotRef != null && slotSnapshot?.exists() == true) {
                    transaction.delete(slotRef)
                }
            }.await()
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
                val slotRef = slotReference(data)
                val slotSnapshot = slotRef?.let(transaction::get)
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
                if (slotRef != null && slotSnapshot?.exists() == true) transaction.delete(slotRef)
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
                val slotRef = slotReference(data)
                val slotSnapshot = slotRef?.let(tx::get)
                tx.update(ref, mapOf(
                    "status" to "completed",
                    "balancePaymentStatus" to "demo_paid",
                    "balancePaidAt" to System.currentTimeMillis()
                ))
                if (slotRef != null && slotSnapshot?.exists() == true) tx.delete(slotRef)
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
                val slotRef = slotReference(data)
                val slotSnapshot = slotRef?.let(tx::get)
                tx.update(ref, mapOf(
                    "status" to "completed",
                    "balancePaymentStatus" to "cash_paid",
                    "balancePaidAt" to System.currentTimeMillis()
                ))
                if (slotRef != null && slotSnapshot?.exists() == true) tx.delete(slotRef)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun slotReference(merchantId: String, dateKey: String, timeKey: String): DocumentReference =
        firestore.collection("appointment_slots")
            .document(merchantId)
            .collection("days")
            .document(dateKey)
            .collection("times")
            .document(timeKey)

    private fun slotReference(appointmentData: Map<String, Any?>): DocumentReference? {
        val merchantId = appointmentData["merchantId"] as? String ?: return null
        val dateKey = appointmentData["slotDateKey"] as? String ?: return null
        val timeKey = appointmentData["slotTimeKey"] as? String ?: return null
        if (merchantId.isBlank() || dateKey.isBlank() || timeKey.isBlank()) return null
        return slotReference(merchantId, dateKey, timeKey)
    }
}
