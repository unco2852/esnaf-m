package com.example.esnafm.data.model

/**
 * Firestore "appointments" koleksiyonu için Randevu & Hizmet Talebi Modeli.
 * Durumlar (status): "pending" (Beklemede), "approved" (Onaylandı), "rejected" (Reddedildi), "cancelled" (İptal Edildi)
 */
data class AppointmentModel(
    val id: String = "",
    val customerId: String = "",
    val customerEmail: String = "",
    val merchantId: String = "",
    val serviceId: String = "",
    val serviceTitle: String = "",
    val price: Double = 0.0,
    val depositAmount: Double = 0.0,
    val depositPaidAt: Long? = null,
    val date: String = "",
    val time: String = "",
    val note: String? = null,
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis(),
    val recipientName: String = "",
    val customerPhone: String = "",
    val serviceLocation: String = "at_business",
    val serviceAddress: String = "",
    val depositPaymentStatus: String = "not_required",
    val depositRefundAt: Long? = null,
    val balancePaymentStatus: String = "unpaid",
    val balancePaymentRequestedAt: Long? = null,
    val balancePaidAt: Long? = null,
    val cancelledAt: Long? = null,
    val cancellationReason: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "customerId" to customerId,
            "customerEmail" to customerEmail,
            "merchantId" to merchantId,
            "serviceId" to serviceId,
            "serviceTitle" to serviceTitle,
            "price" to price,
            "depositAmount" to depositAmount,
            "depositPaidAt" to depositPaidAt,
            "date" to date,
            "time" to time,
            "note" to note,
            "status" to status,
            "createdAt" to createdAt,
            "recipientName" to recipientName,
            "customerPhone" to customerPhone,
            "serviceLocation" to serviceLocation,
            "serviceAddress" to serviceAddress,
            "depositPaymentStatus" to depositPaymentStatus,
            "depositRefundAt" to depositRefundAt,
            "balancePaymentStatus" to balancePaymentStatus,
            "balancePaymentRequestedAt" to balancePaymentRequestedAt,
            "balancePaidAt" to balancePaidAt,
            "cancelledAt" to cancelledAt,
            "cancellationReason" to cancellationReason
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): AppointmentModel {
            return AppointmentModel(
                id = id,
                customerId = map["customerId"] as? String ?: "",
                customerEmail = map["customerEmail"] as? String ?: "",
                merchantId = map["merchantId"] as? String ?: "",
                serviceId = map["serviceId"] as? String ?: "",
                serviceTitle = map["serviceTitle"] as? String ?: "",
                price = (map["price"] as? Number)?.toDouble() ?: 0.0,
                depositAmount = (map["depositAmount"] as? Number)?.toDouble() ?: 0.0,
                depositPaidAt = (map["depositPaidAt"] as? Number)?.toLong(),
                date = map["date"] as? String ?: "",
                time = map["time"] as? String ?: "",
                note = map["note"] as? String,
                status = map["status"] as? String ?: "pending",
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                recipientName = map["recipientName"] as? String ?: "",
                customerPhone = map["customerPhone"] as? String ?: "",
                serviceLocation = map["serviceLocation"] as? String ?: "at_business",
                serviceAddress = map["serviceAddress"] as? String ?: "",
                depositPaymentStatus = map["depositPaymentStatus"] as? String
                    ?: if (((map["depositAmount"] as? Number)?.toDouble() ?: 0.0) > 0.0) "unpaid" else "not_required",
                depositRefundAt = (map["depositRefundAt"] as? Number)?.toLong(),
                balancePaymentStatus = map["balancePaymentStatus"] as? String ?: "unpaid",
                balancePaymentRequestedAt = (map["balancePaymentRequestedAt"] as? Number)?.toLong(),
                balancePaidAt = (map["balancePaidAt"] as? Number)?.toLong(),
                cancelledAt = (map["cancelledAt"] as? Number)?.toLong(),
                cancellationReason = map["cancellationReason"] as? String ?: ""
            )
        }
    }
}
