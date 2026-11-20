package com.example.esnafm.data.repository

import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.notifications.NotificationTokenId
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Firebase Auth ve Firestore entegrasyonundan sorumlu Repository katmanı.
 * Kullanıcı rollerinin kalıcılığını ve güvenliğini garanti altına alır.
 */
class AuthRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    val currentUser: FirebaseUser?
        get() = try {
            auth.currentUser
        } catch (e: Exception) {
            null
        }

    /**
     * E-posta ve şifre ile yeni kullanıcı kaydı oluşturur
     * ve kullanıcının profil detaylarını Firestore 'users' koleksiyonuna ekler.
     */
    suspend fun signUp(
        email: String,
        pass: String,
        name: String,
        role: UserRole,
        businessName: String = "",
        category: String = "",
        address: String = "",
        phone: String = "",
        city: String = "",
        district: String = "",
        businessPhone: String = "",
        businessDescription: String = "",
        workingHours: String = "09:00 - 18:00"
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        var createdUser: FirebaseUser? = null
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Kullanıcı kimliği alınamadı.")
            createdUser = authResult.user

            val profile = UserProfile(
                uid = uid,
                email = email,
                name = name,
                phone = phone,
                role = role,
                city = city,
                district = district,
                businessName = businessName,
                category = category,
                address = address,
                businessDescription = businessDescription,
                businessPhone = businessPhone.ifBlank { phone },
                businessAddress = if (role == UserRole.MERCHANT) address else "",
                workingHours = workingHours.ifBlank { "09:00 - 18:00" }
            )

            val batch = firestore.batch()
            batch.set(firestore.collection("users").document(uid), profile.toMap())
            if (role == UserRole.MERCHANT) {
                batch.set(firestore.collection("public_merchants").document(uid), profile.toPublicMerchantMap())
            }
            batch.commit().await()
            if (role == UserRole.CUSTOMER || role == UserRole.MERCHANT) {
                runCatching { registerNotificationToken(uid) }
            }

            Result.success(profile)
        } catch (e: Exception) {
            // Avoid leaving an Auth account with no usable Firestore profile after a failed signup.
            if (createdUser != null) {
                runCatching { createdUser?.delete()?.await() }
                auth.signOut()
            }
            Result.failure(e)
        }
    }

    /**
     * E-posta ve şifre ile kullanıcı girişi yapar ve profilini Firestore'dan çekerek doğrular.
     * expectedRole: Giriş kapısından beklenen rol (Müşteri/Esnaf). Rol uyumsuzluğunda akıllı onarım veya doğrulama yapılır.
     */
    suspend fun signIn(
        email: String,
        pass: String,
        expectedRole: UserRole? = null
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val authResult = auth.signInWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Giriş yapılamadı.")
            val profileResult = getUserProfile(uid)
            if (profileResult.isSuccess) {
                var fetchedProfile = profileResult.getOrThrow()

                // Rol Doğrulaması & Otomatik Onarım: İşletme detayları olan kullanıcı Esnaf olarak onarılır
                if (expectedRole == UserRole.MERCHANT && fetchedProfile.role != UserRole.MERCHANT && fetchedProfile.businessName.isNotBlank()) {
                    fetchedProfile = fetchedProfile.copy(role = UserRole.MERCHANT)
                    try {
                        firestore.collection("users").document(uid).update("role", "MERCHANT").await()
                    } catch (e: Exception) {
                        // Onarım hatası sessizce yutulur
                    }
                }

                if (expectedRole != null && fetchedProfile.role != expectedRole) {
                    val actualRoleName = if (fetchedProfile.role == UserRole.MERCHANT) "Esnaf" else "Müşteri"
                    auth.signOut()
                    return@withContext Result.failure(
                        Exception("Bu hesap $actualRoleName hesabıdır. Lütfen $actualRoleName giriş kapısını kullanarak giriş yapınız.")
                    )
                }

                runCatching { registerNotificationToken(uid) }

                Result.success(fetchedProfile)
            } else {
                Result.failure(Exception("Kullanıcı profili veritabanında bulunamadı. Lütfen önce kayıt olun."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Firestore'dan girilen UID'ye sahip kullanıcının profilini çeker.
     * Esnaf hesaplarında bozulan rol bilgileri otomatik onarılır.
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("users").document(uid).get().await()
            val exists = snapshot.exists() && snapshot.data != null
            if (exists) {
                val rawData = snapshot.data!!
                val profile = UserProfile.fromMap(rawData)

                // Veritabanındaki 'role' alanı eksik veya hatalıysa ve işletme adı varsa veritabanında onar
                if (profile.role == UserRole.MERCHANT && rawData["role"]?.toString()?.uppercase() != "MERCHANT") {
                    try {
                        firestore.collection("users").document(uid).update("role", "MERCHANT")
                    } catch (e: Exception) {
                        // Arka plan güncellenme hatası
                    }
                }

                // Existing merchants publish only the explicitly public listing fields.
                // This also lets an existing merchant repair its public listing on next sign-in.
                if (profile.role == UserRole.MERCHANT && uid == auth.currentUser?.uid) {
                    firestore.collection("public_merchants")
                        .document(uid)
                        .set(profile.toPublicMerchantMap())
                        .await()
                }

                Result.success(profile)
            } else {
                Result.failure(Exception("Kullanıcı profili bulunamadı."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Firestore'daki mevcut kullanıcı profilini günceller.
     * DİKKAT: Kullanıcı rolü profil düzenleme sırasında asla ezilmez veya değiştirilmez!
     */
    suspend fun updateUserProfile(profile: UserProfile): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(profile.uid.isNotBlank()) { "Kullanıcı UID boş olamaz." }

            // Profil güncellemelerinde 'role' alanının üzerine yazılması kesin olarak engellenir
            val updateMap = profile.toMap().toMutableMap()
            updateMap.remove("role")

            val batch = firestore.batch()
            batch.update(firestore.collection("users").document(profile.uid), updateMap)
            if (profile.role == UserRole.MERCHANT) {
                batch.set(
                    firestore.collection("public_merchants").document(profile.uid),
                    profile.toPublicMerchantMap(),
                    com.google.firebase.firestore.SetOptions.merge()
                )
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setBusinessOpen(uid: String, isOpen: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(uid.isNotBlank() && uid == auth.currentUser?.uid) { "İşletme durumu yalnızca hesap sahibi tarafından değiştirilebilir." }
            val batch = firestore.batch()
            batch.update(firestore.collection("users").document(uid), "isBusinessOpen", isOpen)
            batch.update(firestore.collection("public_merchants").document(uid), "isOpen", isOpen)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Kullanıcıya şifre sıfırlama e-postası gönderir.
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            require(email.isNotBlank()) { "E-posta adresi boş olamaz." }
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Oturum açmış kullanıcının hesabını ve Firestore verilerini siler.
     */
    suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = auth.currentUser ?: throw Exception("Oturum açmış kullanıcı bulunamadı.")
            val uid = user.uid

            // Delete Auth first. A recent-login error must leave the profile intact.
            // The auth onDelete Cloud Function removes the associated Firebase data.
            user.delete().await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Public listing documents contain only business-facing fields.
     */
    fun getMerchantsFlow(): Flow<List<UserProfile>> = callbackFlow {
        val listenerRegistration = firestore.collection("public_merchants")
            .whereEqualTo("role", UserRole.MERCHANT.name)
            .limit(200)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val merchants = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data -> UserProfile.fromPublicMerchantMap(data) }
                    }
                    trySend(merchants)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun getPublicMerchantProfile(uid: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        try {
            require(uid.isNotBlank()) { "İşletme kimliği boş olamaz." }
            val snapshot = firestore.collection("public_merchants").document(uid).get().await()
            val data = snapshot.data ?: error("İşletme profili bulunamadı.")
            Result.success(UserProfile.fromPublicMerchantMap(data))
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    /**
     * Mevcut oturumu kapatır.
     */
    suspend fun signOut() = withContext(Dispatchers.IO) {
        try {
            val uid = auth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                runCatching {
                    val token = FirebaseMessaging.getInstance().token.await()
                    firestore.collection("users").document(uid)
                        .collection("notification_tokens").document(NotificationTokenId.from(token)).delete().await()
                }
            }
            auth.signOut()
        } catch (e: Exception) {
            // Sessizce yut
        }
    }

    private suspend fun registerNotificationToken(uid: String) {
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .collection("notification_tokens").document(NotificationTokenId.from(token))
            .set(mapOf("token" to token, "platform" to "android", "updatedAt" to System.currentTimeMillis()))
            .await()
    }
}
