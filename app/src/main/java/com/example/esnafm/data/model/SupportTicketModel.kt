package com.example.esnafm.data.model

/**
 * Destek ve Sorun Bildirimi Talebi Modeli
 */
data class SupportTicketModel(
    val id: String = "",
    val uid: String = "",
    val userEmail: String = "",
    val subject: String = "Sorun Bildir", // "Sorun Bildir", "Özellik Öner", "Diğer"
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "userEmail" to userEmail,
            "subject" to subject,
            "message" to message,
            "createdAt" to createdAt
        )
    }
}