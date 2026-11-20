package com.example.esnafm.data.model

/**
 * Kullanıcıya ait Kayıtlı Adres Modeli
 */
data class AddressModel(
    val id: String = "",
    val uid: String = "",
    val title: String = "Ev", // "Ev", "İş", "Diğer"
    val fullAddress: String = "",
    val description: String = "",
    val isDefault: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "title" to title,
            "fullAddress" to fullAddress,
            "description" to description,
            "isDefault" to isDefault
        )
    }

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): AddressModel {
            return AddressModel(
                id = id,
                uid = map["uid"] as? String ?: "",
                title = map["title"] as? String ?: "Ev",
                fullAddress = map["fullAddress"] as? String ?: "",
                description = map["description"] as? String ?: "",
                isDefault = map["isDefault"] as? Boolean ?: false
            )
        }
    }
}