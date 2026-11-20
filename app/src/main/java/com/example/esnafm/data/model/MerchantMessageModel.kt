package com.example.esnafm.data.model

/** A single message in a private customer/merchant conversation. */
data class MerchantMessageModel(
    val id: String = "",
    val senderId: String = "",
    val customerId: String = "",
    val merchantId: String = "",
    val message: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val attachmentUrl: String = "",
    val attachmentPath: String = "",
    val attachmentName: String = "",
    val attachmentMimeType: String = "",
    val attachmentType: String = "",
    val attachmentSizeBytes: Long = 0L
) {
    fun toMap(): Map<String, Any> = buildMap {
        put("senderId", senderId)
        put("customerId", customerId)
        put("merchantId", merchantId)
        put("message", message)
        put("createdAt", createdAt)
        if (attachmentPath.isNotBlank()) {
            put("attachmentPath", attachmentPath)
            put("attachmentName", attachmentName)
            put("attachmentMimeType", attachmentMimeType)
            put("attachmentType", attachmentType)
            put("attachmentSizeBytes", attachmentSizeBytes)
        }
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): MerchantMessageModel {
            val senderId = map["senderId"] as? String ?: ""
            return MerchantMessageModel(
                id = id,
                senderId = senderId,
                // Older messages predate customerId and were always customer-authored.
                customerId = map["customerId"] as? String ?: senderId,
                merchantId = map["merchantId"] as? String ?: "",
                message = map["message"] as? String ?: "",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L,
                attachmentUrl = map["attachmentUrl"] as? String ?: "",
                attachmentPath = map["attachmentPath"] as? String ?: "",
                attachmentName = map["attachmentName"] as? String ?: "",
                attachmentMimeType = map["attachmentMimeType"] as? String ?: "",
                attachmentType = map["attachmentType"] as? String ?: "",
                attachmentSizeBytes = (map["attachmentSizeBytes"] as? Number)?.toLong() ?: 0L
            )
        }
    }
}
