package com.example.esnafm.data.repository

import android.net.Uri
import com.example.esnafm.data.model.MerchantMessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MerchantMessageRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val messages = firestore.collection("merchant_messages")
    private val conversations = firestore.collection("merchant_conversations")

    fun getMerchantMessagesFlow(merchantId: String): Flow<List<MerchantMessageModel>> = callbackFlow {
        if (merchantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = conversations
            .whereEqualTo("merchantId", merchantId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val latestFirst = snapshot.documents.mapNotNull { document ->
                        if (document.getString("status") == "closed") return@mapNotNull null
                        val customerId = document.getString("customerId") ?: return@mapNotNull null
                        MerchantMessageModel(
                            id = document.getString("lastMessageId") ?: document.id,
                            customerId = customerId,
                            merchantId = merchantId,
                            createdAt = document.getLong("lastMessageAt")
                                ?: document.getLong("createdAt")
                                ?: 0L
                        )
                    }.sortedByDescending { it.createdAt }
                    trySend(latestFirst)
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    /** The query is scoped to the authenticated participant so Firestore can enforce private access. */
    fun getConversationFlow(
        merchantId: String,
        customerId: String,
        currentUserId: String
    ): Flow<List<MerchantMessageModel>> = callbackFlow {
        val authenticatedUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
        if (merchantId.isBlank() || customerId.isBlank() ||
            authenticatedUserId.isBlank() || currentUserId != authenticatedUserId ||
            (authenticatedUserId != merchantId && authenticatedUserId != customerId)
        ) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val participantField = if (authenticatedUserId == merchantId) "merchantId" else "customerId"
        val registration = messages
            .whereEqualTo(participantField, authenticatedUserId)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val conversation = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { MerchantMessageModel.fromMap(doc.id, it) }
                    }.filter { it.merchantId == merchantId && it.customerId == customerId }
                        .sortedBy { it.createdAt }
                    launch { trySend(resolveAttachmentUrls(conversation)) }
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMessage(
        senderId: String,
        customerId: String,
        merchantId: String,
        text: String,
        attachmentUri: Uri? = null,
        attachmentName: String = "",
        attachmentMimeType: String = "",
        attachmentSizeBytes: Long = 0L
    ): Result<String> = withContext(Dispatchers.IO) {
        var uploadedRef: com.google.firebase.storage.StorageReference? = null
        try {
            val trimmedMessage = text.trim()
            require(senderId.isNotBlank() && senderId == FirebaseAuth.getInstance().currentUser?.uid) {
                "Mesaj göndermek için giriş yapmalısınız."
            }
            require(customerId.isNotBlank() && merchantId.isNotBlank()) { "Sohbet bulunamadı." }
            require(senderId == customerId || senderId == merchantId) { "Bu sohbete mesaj gönderme yetkiniz yok." }
            require(trimmedMessage.length <= 2000) { "Mesaj 2000 karakteri geçemez." }
            require(trimmedMessage.isNotBlank() || attachmentUri != null) { "Mesaj veya dosya ekleyin." }
            val maxBytes = if (attachmentMimeType.startsWith("video/")) MAX_VIDEO_ATTACHMENT_BYTES else MAX_ATTACHMENT_BYTES
            require(attachmentSizeBytes <= maxBytes || attachmentSizeBytes <= 0L) {
                if (attachmentMimeType.startsWith("video/")) "Videolar en fazla 100 MB olabilir." else "Fotoğraf ve dosyalar en fazla 25 MB olabilir."
            }

            val conversationRef = firestore.collection("merchant_conversations")
                .document("$merchantId--$customerId")
            val document = messages.document()
            firestore.runTransaction { transaction ->
                val conversation = transaction.get(conversationRef)
                if (!conversation.exists()) {
                    require(senderId == customerId) { "Sohbeti önce müşteri başlatmalıdır." }
                    val now = System.currentTimeMillis()
                    transaction.set(conversationRef, mapOf(
                        "customerId" to customerId,
                        "merchantId" to merchantId,
                        "createdAt" to now,
                        "lastMessageAt" to now,
                        "lastMessageId" to "pending",
                        "status" to "active"
                    ))
                } else {
                    require(conversation.getString("customerId") == customerId && conversation.getString("merchantId") == merchantId) {
                        "Sohbet katılımcıları doğrulanamadı."
                    }
                    require(conversation.getString("status") != "closed") {
                        "Bu sohbet esnaf tarafından kapatıldı. İşletme sayfasından yeni bir sohbet başlatabilirsiniz."
                    }
                }
            }.await()

            var attachmentUrl = ""
            var attachmentPath = ""
            val safeName = attachmentName
                .replace(Regex("[\\r\\n\\u0000-\\u001f]"), "")
                .take(120)

            if (attachmentUri != null) {
                val mimeType = attachmentMimeType.ifBlank { "application/octet-stream" }
                attachmentPath = "merchant_chats/$merchantId/$customerId/${document.id}"
                uploadedRef = storage.reference.child(attachmentPath)
                val metadata = StorageMetadata.Builder().setContentType(mimeType).build()
                uploadedRef.putFile(attachmentUri, metadata).await()
                attachmentUrl = ""
            }

            val mimeType = attachmentMimeType.ifBlank { "" }
            val model = MerchantMessageModel(
                id = document.id,
                senderId = senderId,
                customerId = customerId,
                merchantId = merchantId,
                message = trimmedMessage,
                createdAt = System.currentTimeMillis(),
                attachmentUrl = attachmentUrl,
                attachmentPath = attachmentPath,
                attachmentName = safeName,
                attachmentMimeType = mimeType,
                attachmentType = when {
                    mimeType.startsWith("image/") -> "image"
                    mimeType.startsWith("video/") -> "video"
                    attachmentUri != null -> "file"
                    else -> ""
                },
                attachmentSizeBytes = attachmentSizeBytes.coerceAtLeast(0L)
            )
            val batch = firestore.batch()
            batch.set(document, model.toMap())
            batch.update(conversationRef, mapOf(
                "lastMessageAt" to model.createdAt,
                "lastMessageId" to document.id
            ))
            batch.commit().await()
            Result.success(document.id)
        } catch (e: Exception) {
            runCatching { uploadedRef?.delete()?.await() }
            Result.failure(e)
        }
    }

    /** Esnafın bitirdiği sohbeti, mesajları ve sohbet eklerini kalıcı olarak siler. */
    suspend fun finishConversation(merchantId: String, customerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val authenticatedUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
            require(merchantId.isNotBlank() && customerId.isNotBlank()) { "Sohbet bilgileri bulunamadı." }
            require(authenticatedUserId == merchantId) { "Bu sohbeti yalnızca ilgili işletme kapatabilir." }

            val conversationRef = firestore.collection("merchant_conversations")
                .document("$merchantId--$customerId")
            val conversationSnapshot = conversationRef.get().await()
            if (conversationSnapshot.exists()) {
                require(conversationSnapshot.getString("merchantId") == merchantId &&
                    conversationSnapshot.getString("customerId") == customerId) {
                    "Sohbet katılımcıları doğrulanamadı."
                }
                if (conversationSnapshot.getString("status") != "closed") {
                    conversationRef.update(
                        mapOf("status" to "closed", "closedAt" to System.currentTimeMillis())
                    ).await()
                }
            }

            val attachmentPrefix = "merchant_chats/$merchantId/$customerId/"
            var cursor: com.google.firebase.firestore.DocumentSnapshot? = null
            while (true) {
                var query = messages
                    .whereEqualTo("merchantId", merchantId)
                    .orderBy(com.google.firebase.firestore.FieldPath.documentId())
                    .limit(400)
                if (cursor != null) query = query.startAfter(cursor!!)
                val page = query.get().await()
                if (page.isEmpty) break
                cursor = page.documents.last()

                val conversationMessages = page.documents.filter { document ->
                    val data = document.data.orEmpty()
                    val storedCustomerId = if (data.containsKey("customerId")) {
                        document.getString("customerId")
                    } else {
                        document.getString("senderId")
                    }
                    storedCustomerId == customerId
                }
                if (conversationMessages.isEmpty()) continue

                val attachmentPaths = conversationMessages.mapNotNull { it.getString("attachmentPath") }
                    .filter { it.startsWith(attachmentPrefix) }
                    .distinct()
                for (path in attachmentPaths) {
                    try {
                        storage.reference.child(path).delete().await()
                    } catch (error: StorageException) {
                        if (error.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw error
                    }
                }

                val batch = firestore.batch()
                conversationMessages.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }

            if (conversationRef.get().await().exists()) conversationRef.delete().await()
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    private suspend fun resolveAttachmentUrls(messages: List<MerchantMessageModel>): List<MerchantMessageModel> = coroutineScope {
        messages.map { message ->
            async {
                if (message.attachmentPath.isBlank()) message
                else message.copy(
                    attachmentUrl = runCatching {
                        storage.reference.child(message.attachmentPath).downloadUrl.await().toString()
                    }.getOrDefault("")
                )
            }
        }.awaitAll()
    }

    companion object {
        const val MAX_ATTACHMENT_BYTES = 25L * 1024L * 1024L
        const val MAX_VIDEO_ATTACHMENT_BYTES = 100L * 1024L * 1024L
    }
}
