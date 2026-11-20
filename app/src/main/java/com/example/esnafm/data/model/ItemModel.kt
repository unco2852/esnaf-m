package com.example.esnafm.data.model

/**
 * Firestore "items" koleksiyonu için Ürün / Hizmet Veri Modeli.
 * merchantId: Ürünü ekleyen esnafın Firebase Auth UID değeri.
 */
data class ItemModel(
    val id: String = "",
    val merchantId: String = "",
    val title: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val category: String = "",
    val description: String? = null,
    val imageUrl: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "merchantId" to merchantId,
            "title" to title,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "description" to description,
            "imageUrl" to imageUrl,
            "isActive" to isActive,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ItemModel {
            return ItemModel(
                id = id,
                merchantId = map["merchantId"] as? String ?: "",
                title = map["title"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                stock = (map["stock"] as? Number)?.toInt() ?: 0,
                category = map["category"] as? String ?: "",
                description = map["description"] as? String,
                imageUrl = map["imageUrl"] as? String,
                isActive = map["isActive"] as? Boolean ?: true,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
