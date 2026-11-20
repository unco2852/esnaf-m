package com.example.esnafm.data.repository

import com.example.esnafm.data.model.OrderModel
import com.example.esnafm.data.model.ItemModel
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
 * Firestore "orders" koleksiyonundaki müşteri sipariş verilerini yöneten Repository.
 */
class OrderRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val ordersCollection = firestore.collection("orders")

    /**
     * Oturum açmış müşterinin kendi siparişlerini canlı olarak dinler.
     */
    fun getCustomerOrdersFlow(customerId: String): Flow<List<OrderModel>> = callbackFlow {
        if (customerId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = ordersCollection
            .whereEqualTo("customerId", customerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data -> OrderModel.fromMap(doc.id, data) }
                    }.sortedByDescending { it.createdAt }
                    trySend(orders)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /** Esnafa gelen siparişleri canlı dinler. */
    fun getMerchantOrdersFlow(merchantId: String): Flow<List<OrderModel>> = callbackFlow {
        if (merchantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listenerRegistration = ordersCollection
            .whereEqualTo("merchantId", merchantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val orders = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { OrderModel.fromMap(doc.id, it) }
                    }.sortedByDescending { it.createdAt }
                    trySend(orders)
                }
            }

        awaitClose { listenerRegistration.remove() }
    }.flowOn(Dispatchers.IO)

    /** Stoku transaction ile düşerek gerçek ürün fiyatı üzerinden sipariş oluşturur. */
    suspend fun createOrder(
        itemId: String,
        customerId: String,
        customerEmail: String,
        quantity: Int,
        recipientName: String,
        customerPhone: String,
        deliveryAddress: String,
        fulfillmentType: String,
        paymentMethod: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            require(itemId.isNotBlank()) { "Ürün bulunamadı." }
            require(customerId.isNotBlank()) { "Sipariş vermek için giriş yapmalısınız." }
            require(quantity > 0) { "Adet en az 1 olmalıdır." }
            require(recipientName.isNotBlank()) { "Teslim alacak kişinin adını girin." }
            require(customerPhone.isNotBlank()) { "Sipariş için telefon numarası girin." }
            require(fulfillmentType in setOf("delivery", "pickup")) { "Teslimat türü geçersiz." }
            require(paymentMethod in setOf("cash_on_delivery", "pay_at_store", "mock_card")) { "Ödeme yöntemi geçersiz." }
            require(fulfillmentType == "pickup" || deliveryAddress.isNotBlank()) { "Teslimat adresini girin." }

            val itemRef = firestore.collection("items").document(itemId)
            val orderRef = ordersCollection.document()
            val orderId = firestore.runTransaction { transaction ->
                val itemSnapshot = transaction.get(itemRef)
                val itemData = itemSnapshot.data ?: throw IllegalStateException("Ürün bulunamadı.")
                val item = ItemModel.fromMap(itemSnapshot.id, itemData)

                require(item.isActive) { "Bu ürün artık satışta değil." }
                require(item.merchantId.isNotBlank()) { "Ürün satıcısı bulunamadı." }
                require(item.stock >= quantity) { "Yeterli stok yok. Mevcut stok: ${item.stock}." }

                val createdAt = System.currentTimeMillis()
                val order = OrderModel(
                    id = orderRef.id,
                    itemId = item.id,
                    customerId = customerId,
                    customerEmail = customerEmail,
                    merchantId = item.merchantId,
                    itemTitle = item.title,
                    price = item.price,
                    quantity = quantity,
                    status = "pending",
                    recipientName = recipientName.trim(),
                    customerPhone = customerPhone.trim(),
                    deliveryAddress = deliveryAddress.trim(),
                    fulfillmentType = fulfillmentType,
                    paymentMethod = paymentMethod,
                    paymentStatus = if (paymentMethod == "mock_card") "demo_paid" else "unpaid",
                    createdAt = createdAt,
                    approvalDeadlineAt = createdAt + 30L * 60L * 1000L
                )

                transaction.update(itemRef, "stock", item.stock - quantity)
                transaction.set(orderRef, order.toMap())
                orderRef.id
            }.await()

            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Esnaf siparişin durumunu günceller. */
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(orderId.isNotBlank()) { "Sipariş kimliği boş olamaz." }
            require(status in setOf("confirmed", "preparing", "ready", "out_for_delivery", "delivered", "cancelled")) { "Geçersiz sipariş durumu." }
            val orderRef = ordersCollection.document(orderId)
            if (status == "cancelled") {
                firestore.runTransaction { transaction ->
                    val orderSnapshot = transaction.get(orderRef)
                    val orderData = orderSnapshot.data ?: throw IllegalStateException("Sipariş bulunamadı.")
                    val order = OrderModel.fromMap(orderSnapshot.id, orderData)
                    require(order.status in setOf("pending", "confirmed")) { "Bu sipariş artık iptal edilemez." }
                    require(order.paymentStatus != "paid" && order.paymentStatus != "processing") {
                        "Ödemesi alınmış veya işlemde olan sipariş destek üzerinden iptal edilmelidir."
                    }
                    if (order.itemId.isNotBlank() && order.quantity > 0) {
                        val itemRef = firestore.collection("items").document(order.itemId)
                        val itemSnapshot = transaction.get(itemRef)
                        val stock = (itemSnapshot.getLong("stock") ?: 0L).toInt()
                        transaction.update(itemRef, "stock", stock + order.quantity)
                    }
                    val now = System.currentTimeMillis()
                    val updates = mutableMapOf<String, Any>(
                        "status" to "cancelled",
                        "cancelledAt" to now,
                        "cancellationReason" to "merchant_cancelled"
                    )
                    if (order.paymentStatus == "demo_paid") {
                        updates["paymentStatus"] = "demo_refunded"
                        updates["refundAt"] = now
                    }
                    transaction.update(orderRef, updates)
                }.await()
            } else {
                orderRef.update("status", status).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Müşteri, sipariş hazırlanmadan/teslimata çıkmadan kendi siparişini iptal eder. */
    suspend fun cancelCustomerOrder(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
                ?: error("Siparişi iptal etmek için giriş yapmalısınız.")
            require(orderId.isNotBlank()) { "Sipariş bulunamadı." }
            val orderRef = ordersCollection.document(orderId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(orderRef)
                val data = snapshot.data ?: error("Sipariş bulunamadı.")
                val order = OrderModel.fromMap(snapshot.id, data)
                require(order.customerId == uid) { "Bu sipariş size ait değil." }
                require(order.status == "pending") {
                    "Esnaf siparişi onayladıktan sonra uygulamadan iptal edilemez."
                }
                require(order.paymentStatus !in setOf("paid", "processing")) {
                    "Gerçek ödeme durumundaki siparişler destek üzerinden iptal edilmelidir."
                }

                val now = System.currentTimeMillis()
                val updates = mutableMapOf<String, Any>(
                    "status" to "cancelled",
                    "cancelledAt" to now,
                    "cancellationReason" to "customer_cancelled"
                )
                if (order.paymentStatus == "demo_paid") {
                    updates["paymentStatus"] = "demo_refunded"
                    updates["refundAt"] = now
                }
                transaction.update(orderRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
