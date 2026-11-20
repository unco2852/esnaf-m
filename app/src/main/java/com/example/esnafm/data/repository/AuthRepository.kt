package com.example.esnafm.data.repository

import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
        try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: throw Exception("Kullanıcı kimliği alınamadı.")

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

            firestore.collection("users")
                .document(uid)
                .set(profile.toMap())
                .await()

            Result.success(profile)
        } catch (e: Exception) {
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
            android.util.Log.d("EsnafmAuthDebug", "3. AuthRepository.signIn - Firebase Auth UID: '$uid', expectedRole: '$expectedRole'")

            val profileResult = getUserProfile(uid)
            if (profileResult.isSuccess) {
                var fetchedProfile = profileResult.getOrThrow()
                android.util.Log.d("EsnafmAuthDebug", "6. AuthRepository.signIn - Fetched Profile UID: '${fetchedProfile.uid}', Role: '${fetchedProfile.role}'")

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
                    android.util.Log.d("EsnafmAuthDebug", "6b. AuthRepository.signIn - ROLE MISMATCH! Actual: ${fetchedProfile.role}, Expected: $expectedRole")
                    return@withContext Result.failure(
                        Exception("Bu hesap $actualRoleName hesabıdır. Lütfen $actualRoleName giriş kapısını kullanarak giriş yapınız.")
                    )
                }

                Result.success(fetchedProfile)
            } else {
                android.util.Log.d("EsnafmAuthDebug", "6c. AuthRepository.signIn - Profile not found in Firestore for UID: '$uid'")
                Result.failure(Exception("Kullanıcı profili veritabanında bulunamadı. Lütfen önce kayıt olun."))
            }
        } catch (e: Exception) {
            android.util.Log.d("EsnafmAuthDebug", "6d. AuthRepository.signIn - Error: ${e.localizedMessage}")
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
            android.util.Log.d("EsnafmAuthDebug", "4. AuthRepository.getUserProfile - UID: '$uid', Document exists: $exists")

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

                Result.success(profile)
            } else {
                Result.failure(Exception("Kullanıcı profili bulunamadı."))
            }
        } catch (e: Exception) {
            android.util.Log.d("EsnafmAuthDebug", "4b. AuthRepository.getUserProfile - Exception for UID '$uid': ${e.localizedMessage}")
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

            firestore.collection("users")
                .document(profile.uid)
                .update(updateMap)
                .await()

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

            // Firestore 'users' belgesini sil
            try {
                firestore.collection("users").document(uid).delete().await()
            } catch (e: Exception) {
                // Ignore document deletion failure if missing
            }

            // Firebase Auth hesabını sil
            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Firestore 'users' koleksiyonundan 'MERCHANT' rolüne sahip tüm esnafları canlı dinleyen Flow.
     */
    fun getMerchantsFlow(): Flow<List<UserProfile>> = callbackFlow {
        val listenerRegistration = firestore.collection("users")
            .whereEqualTo("role", UserRole.MERCHANT.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val merchants = snapshot.documents.mapNotNull { doc ->
                        doc.data?.let { data -> UserProfile.fromMap(data) }
                    }
                    trySend(merchants)
                }
            }

        awaitClose {
            listenerRegistration.remove()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Mevcut oturumu kapatır.
     */
    fun signOut() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Sessizce yut
        }
    }
}
