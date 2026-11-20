package com.example.esnafm.data.model

/**
 * Firestore "services" koleksiyonu için Hizmet Veri Modeli.
 * Stok alanı içermez (hizmet mantığı ürün stoklarından farklıdır).
 */
data class ServiceModel(
    val id: String = "",
    val merchantId: String = "",
    val title: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val depositAmount: Double = 0.0,
    val duration: String? = null,
    val category: String = "",
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "merchantId" to merchantId,
            "title" to title,
            "description" to description,
            "price" to price,
            "depositAmount" to depositAmount,
            "duration" to duration,
            "category" to category,
            "imageUrl" to imageUrl,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ServiceModel {
            return ServiceModel(
                id = id,
                merchantId = map["merchantId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                description = map["description"] as? String,
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                depositAmount = (map["depositAmount"] as? Number)?.toDouble() ?: 0.0,
                duration = map["duration"] as? String,
                category = map["category"] as? String ?: "",
                imageUrl = map["imageUrl"] as? String,
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
