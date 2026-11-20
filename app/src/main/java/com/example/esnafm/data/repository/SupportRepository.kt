package com.example.esnafm.data.repository

import com.example.esnafm.data.model.SupportTicketModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Destek ve Sorun Bidirimi İşlemlerinden Sorumlu Repository Katmanı
 */
class SupportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun createTicket(ticket: SupportTicketModel): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid ?: error("Destek talebi göndermek için giriş yapmalısınız.")
            val safeTicket = ticket.copy(
                uid = uid,
                userEmail = auth.currentUser?.email.orEmpty().take(254),
                subject = ticket.subject.trim().take(100),
                message = ticket.message.trim().take(4000),
                createdAt = System.currentTimeMillis()
            )
            require(safeTicket.subject.isNotBlank()) { "Lütfen bir konu seçin." }
            require(safeTicket.message.length >= 10) { "Mesaj en az 10 karakter olmalıdır." }
            firestore.collection("support_tickets")
                .document()
                .set(safeTicket.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
