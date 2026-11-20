package com.example.esnafm.data.repository

import com.example.esnafm.data.model.SupportTicketModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Destek ve Sorun Bidirimi İşlemlerinden Sorumlu Repository Katmanı
 */
class SupportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun createTicket(ticket: SupportTicketModel): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            firestore.collection("support_tickets")
                .document()
                .set(ticket.toMap())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}