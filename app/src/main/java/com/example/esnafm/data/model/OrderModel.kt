package com.example.esnafm.data.model

/**
 * Firestore "orders" koleksiyonu için Sipariş Veri Modeli.
 * Durumlar (status): "pending" (Beklemede), "preparing" (Hazırlanıyor), "delivered" (Teslim Edildi), "cancelled" (İptal Edildi)
 */
data class OrderModel(
    val id: String = "",
    val itemId: String = "",
    val customerId: String = "",
    val merchantId: String = "",
    val itemTitle: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val deliveryAddress: String = "",
    val fulfillmentType: String = "delivery",
    val paymentMethod: String = "cash_on_delivery",
    val paymentStatus: String = "unpaid",
    val approvalDeadlineAt: Long = createdAt + 30L * 60L * 1000L,
    val refundAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "itemId" to itemId,
            "customerId" to customerId,
            "merchantId" to merchantId,
            "itemTitle" to itemTitle,
            "price" to price,
            "quantity" to quantity,
            "status" to status,
            "createdAt" to createdAt,
            "deliveryAddress" to deliveryAddress,
            "fulfillmentType" to fulfillmentType,
            "paymentMethod" to paymentMethod,
            "paymentStatus" to paymentStatus,
            "approvalDeadlineAt" to approvalDeadlineAt,
            "refundAt" to refundAt,
            "cancelledAt" to cancelledAt,
            "cancellationReason" to cancellationReason
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): OrderModel {
            return OrderModel(
                id = id,
                itemId = map["itemId"] as? String ?: "",
                customerId = map["customerId"] as? String ?: "",
                merchantId = map["merchantId"] as? String ?: "",
                itemTitle = map["itemTitle"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                quantity = (map["quantity"] as? Number)?.toInt() ?: 1,
                status = map["status"] as? String ?: "pending",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                deliveryAddress = map["deliveryAddress"] as? String ?: "",
                fulfillmentType = map["fulfillmentType"] as? String ?: "delivery",
                paymentMethod = map["paymentMethod"] as? String ?: "cash_on_delivery",
                paymentStatus = map["paymentStatus"] as? String ?: "unpaid",
                approvalDeadlineAt = (map["approvalDeadlineAt"] as? Number)?.toLong()
                    ?: ((map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()) + 30L * 60L * 1000L,
                refundAt = (map["refundAt"] as? Number)?.toLong(),
                cancelledAt = (map["cancelledAt"] as? Number)?.toLong(),
                cancellationReason = map["cancellationReason"] as? String ?: ""
            )
        }
    }
}
