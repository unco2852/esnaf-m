package com.example.esnafm.data.model

/**
 * Kullanıcı Rolleri:
 * CUSTOMER -> Müşteri
 * MERCHANT -> Esnaf
 */
enum class UserRole {
    CUSTOMER,
    MERCHANT;

    companion object {
        fun fromRoleString(roleStr: String?, isMerchantByFields: Boolean = false): UserRole {
            // Eğer işletme detayları mevcutsa bu hesap kesinlikle Esnaf hesabıdır
            if (isMerchantByFields) {
                return MERCHANT
            }

            if (roleStr.isNullOrBlank()) {
                return CUSTOMER
            }

            val cleanStr = roleStr.trim().uppercase()
            return when {
                cleanStr == "MERCHANT" || cleanStr == "ESNAF" || cleanStr == "TRADESMAN" -> MERCHANT
                cleanStr == "CUSTOMER" || cleanStr == "MUSTERI" || cleanStr == "MÜŞTERİ" -> CUSTOMER
                else -> try {
                    valueOf(cleanStr)
                } catch (e: Exception) {
                    CUSTOMER
                }
            }
        }
    }
}

/**
 * Firestore 'users' koleksiyonunda saklanan Kapsamlı Kullanıcı Profil Modeli
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val surname: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.CUSTOMER,
    val photoUrl: String = "",
    val birthDate: String = "",
    val gender: String = "",
    val bio: String = "",
    val preferredContactMethod: String = "Email",
    val isEmailVerified: Boolean = false,
    val isPhoneVerified: Boolean = false,
    
    // Adres Alanları
    val city: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val street: String = "",
    val buildingNo: String = "",
    val address: String = "",
    val addressNote: String = "",
    
    // Esnaf Özel Alanları
    val businessName: String = "",
    val category: String = "",
    val businessDescription: String = "",
    val businessPhone: String = "",
    val businessAddress: String = "",
    val workingHours: String = "09:00 - 18:00",
    val businessLogoUrl: String = "",
    val businessLatitude: Double? = null,
    val businessLongitude: Double? = null,
    val isBusinessOpen: Boolean = true
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "surname" to surname,
            "phone" to phone,
            "role" to role.name,
            "photoUrl" to photoUrl,
            "birthDate" to birthDate,
            "gender" to gender,
            "bio" to bio,
            "preferredContactMethod" to preferredContactMethod,
            "isEmailVerified" to isEmailVerified,
            "isPhoneVerified" to isPhoneVerified,
            "city" to city,
            "district" to district,
            "neighborhood" to neighborhood,
            "street" to street,
            "buildingNo" to buildingNo,
            "address" to address,
            "addressNote" to addressNote,
            "businessName" to businessName,
            "category" to category,
            "businessDescription" to businessDescription,
            "businessPhone" to businessPhone,
            "businessAddress" to businessAddress,
            "workingHours" to workingHours,
            "businessLogoUrl" to businessLogoUrl,
            "businessLatitude" to businessLatitude,
            "businessLongitude" to businessLongitude,
            "isBusinessOpen" to isBusinessOpen
        )
    }

    /** Deliberately limited public listing; never includes personal contact or account fields. */
    fun toPublicMerchantMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "role" to UserRole.MERCHANT.name,
        "businessName" to businessName.ifBlank { name },
        "category" to category,
        "businessDescription" to businessDescription,
        "businessPhone" to businessPhone,
        "businessAddress" to businessAddress.ifBlank { address },
        "workingHours" to workingHours,
        "businessLogoUrl" to businessLogoUrl,
        "businessLatitude" to businessLatitude,
        "businessLongitude" to businessLongitude,
        "city" to city,
        "district" to district,
        "isOpen" to isBusinessOpen
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): UserProfile {
            val businessName = map["businessName"] as? String ?: ""
            val category = map["category"] as? String ?: ""
            val businessAddress = map["businessAddress"] as? String ?: ""
            val isMerchantByFields = businessName.isNotBlank() || category.isNotBlank() || businessAddress.isNotBlank()

            val roleStr = (map["role"] ?: map["userType"] ?: map["accountType"] ?: map["roleStr"]) as? String
            val roleEnum = UserRole.fromRoleString(roleStr, isMerchantByFields = isMerchantByFields)
            val profileUid = map["uid"] as? String ?: ""

            return UserProfile(
                uid = profileUid,
                email = map["email"] as? String ?: "",
                name = map["name"] as? String ?: "",
                surname = map["surname"] as? String ?: "",
                phone = map["phone"] as? String ?: "",
                role = roleEnum,
                photoUrl = map["photoUrl"] as? String ?: "",
                birthDate = map["birthDate"] as? String ?: "",
                gender = map["gender"] as? String ?: "",
                bio = map["bio"] as? String ?: "",
                preferredContactMethod = map["preferredContactMethod"] as? String ?: "Email",
                isEmailVerified = map["isEmailVerified"] as? Boolean ?: false,
                isPhoneVerified = map["isPhoneVerified"] as? Boolean ?: false,
                city = map["city"] as? String ?: "",
                district = map["district"] as? String ?: "",
                neighborhood = map["neighborhood"] as? String ?: "",
                street = map["street"] as? String ?: "",
                buildingNo = map["buildingNo"] as? String ?: "",
                address = map["address"] as? String ?: "",
                addressNote = map["addressNote"] as? String ?: "",
                businessName = businessName,
                category = category,
                businessDescription = map["businessDescription"] as? String ?: "",
                businessPhone = map["businessPhone"] as? String ?: "",
                businessAddress = businessAddress,
                workingHours = map["workingHours"] as? String ?: "09:00 - 18:00",
                businessLogoUrl = map["businessLogoUrl"] as? String ?: "",
                businessLatitude = (map["businessLatitude"] as? Number)?.toDouble(),
                businessLongitude = (map["businessLongitude"] as? Number)?.toDouble(),
                isBusinessOpen = map["isBusinessOpen"] as? Boolean
                    ?: map["isOpen"] as? Boolean
                    ?: true
            )
        }

        fun fromPublicMerchantMap(map: Map<String, Any?>): UserProfile {
            val businessAddress = map["businessAddress"] as? String ?: ""
            val businessPhone = map["businessPhone"] as? String ?: ""
            return fromMap(
                map + mapOf(
                    "role" to UserRole.MERCHANT.name,
                    // Detail screens use these shared display properties. Both values
                    // come from the explicitly public business listing, not /users.
                    "address" to businessAddress,
                    "phone" to businessPhone
                )
            )
        }
    }
}
