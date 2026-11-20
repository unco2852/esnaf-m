package com.example.esnafm.data.repository

import android.net.Uri
import com.example.esnafm.data.model.MerchantMessageModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
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

    fun getMerchantMessagesFlow(merchantId: String): Flow<List<MerchantMessageModel>> = callbackFlow {
        if (merchantId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = messages
            .whereEqualTo("merchantId", merchantId)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val latestFirst = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { MerchantMessageModel.fromMap(doc.id, it) }
                    }.sortedByDescending { it.createdAt }
                    launch { trySend(resolveAttachmentUrls(latestFirst)) }
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
        if (merchantId.isBlank() || customerId.isBlank() || currentUserId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val participantField = if (currentUserId == merchantId) "merchantId" else "customerId"
        val registration = messages
            .whereEqualTo(participantField, currentUserId)
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
            firestore.runTransaction { transaction ->
                val conversation = transaction.get(conversationRef)
                if (!conversation.exists()) {
                    require(senderId == customerId) { "Sohbeti önce müşteri başlatmalıdır." }
                    transaction.set(conversationRef, mapOf(
                        "customerId" to customerId,
                        "merchantId" to merchantId,
                        "createdAt" to System.currentTimeMillis()
                    ))
                } else {
                    require(conversation.getString("customerId") == customerId && conversation.getString("merchantId") == merchantId) {
                        "Sohbet katılımcıları doğrulanamadı."
                    }
                }
            }.await()

            val document = messages.document()
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
            document.set(model.toMap()).await()
            Result.success(document.id)
        } catch (e: Exception) {
            runCatching { uploadedRef?.delete()?.await() }
            Result.failure(e)
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
