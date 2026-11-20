package com.example.esnafm.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Firebase Storage ile ürün ve hizmet görsellerini yükleme işlemlerinden sorumlu Repository.
 */
class StorageRepository {
    private val storage by lazy { FirebaseStorage.getInstance() }

    /**
     * Seçilen görseli (Uri) Firebase Storage'a yükler ve indirme URL'sini döndürür.
     * folder: "product_images" veya "service_images"
     */
    suspend fun uploadImage(imageUri: Uri, folder: String, merchantId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val filename = "${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child("$folder/$merchantId/$filename")

            ref.putFile(imageUri).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
