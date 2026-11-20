package com.example.esnafm.ui.profile

import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole

data class ProfileCompletionResult(
    val percentage: Int,
    val completedFieldsCount: Int,
    val totalFieldsCount: Int,
    val missingFields: List<String>,
    val statusText: String,
    val explanationText: String
)

/**
 * Kullanıcı ve Esnaf Profil Doluluk Oranını Hesaplayan Test Edilebilir Yardımcı Sınıf
 */
object ProfileCompletionUtil {

    fun calculateCompletion(profile: UserProfile?, addressCount: Int = 0): ProfileCompletionResult {
        if (profile == null) {
            return ProfileCompletionResult(
                percentage = 0,
                completedFieldsCount = 0,
                totalFieldsCount = 4,
                missingFields = listOf("Ad Soyad", "E-posta", "Telefon", "Adres"),
                statusText = "Profiliniz henüz doldurulmadı.",
                explanationText = "Profilinizi tamamlayarak bilgilerinizi güncel tutabilirsiniz."
            )
        }

        val missing = mutableListOf<String>()
        var completed = 0

        if (profile.role == UserRole.MERCHANT) {
            val total = 6
            if (profile.name.isNotBlank()) completed++ else missing.add("Ad Soyad")
            if (profile.phone.isNotBlank()) completed++ else missing.add("Telefon Numarası")
            if (profile.businessName.isNotBlank()) completed++ else missing.add("İşletme Adı")
            if (profile.category.isNotBlank()) completed++ else missing.add("İşletme Kategorisi")
            if (profile.address.isNotBlank() || profile.businessAddress.isNotBlank()) completed++ else missing.add("İşletme Adresi")
            if (profile.workingHours.isNotBlank()) completed++ else missing.add("Çalışma Saatleri")

            val pct = (completed * 100) / total
            val status = if (pct == 100) "Tebrikler! İşletme profiliniz %100 tamamlandı." else "İşletme profilinizin %$pct kadarı tamamlandı."
            val exp = if (pct == 100) "Müşteriler dükkanınıza kolayca ulaşabilir." else "Eksik bilgileri tamamlayarak dükkanınızın görünürlüğünü artırabilirsiniz."

            return ProfileCompletionResult(
                percentage = pct,
                completedFieldsCount = completed,
                totalFieldsCount = total,
                missingFields = missing,
                statusText = status,
                explanationText = exp
            )
        } else {
            val total = 4
            if (profile.name.isNotBlank()) completed++ else missing.add("Ad Soyad")
            if (profile.email.isNotBlank()) completed++ else missing.add("E-posta Adresi")
            if (profile.phone.isNotBlank()) completed++ else missing.add("Telefon Numarası")
            if (profile.address.isNotBlank() || addressCount > 0) completed++ else missing.add("Teslimat Adresi")

            val pct = (completed * 100) / total
            val status = if (pct == 100) "Harika! Profiliniz %100 tamamlandı." else "Profilinizin %$pct kadarı tamamlandı."
            val exp = if (pct == 100) "Tüm kişisel ve iletişim bilgileriniz güncel." else "Profilinizi tamamlayarak sipariş ve randevu süreçlerinizi hızlandırabilirsiniz."

            return ProfileCompletionResult(
                percentage = pct,
                completedFieldsCount = completed,
                totalFieldsCount = total,
                missingFields = missing,
                statusText = status,
                explanationText = exp
            )
        }
    }
}