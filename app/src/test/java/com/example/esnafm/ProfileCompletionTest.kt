package com.example.esnafm

import com.example.esnafm.data.model.UserProfile
import com.example.esnafm.data.model.UserRole
import com.example.esnafm.ui.profile.ProfileCompletionUtil
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileCompletionTest {

    @Test
    fun customer_fullProfile_returns100Percent() {
        val profile = UserProfile(
            name = "Ahmet Yılmaz",
            email = "ahmet@example.com",
            phone = "05551234567",
            address = "Karaköy Mah. No:1 İstanbul",
            role = UserRole.CUSTOMER
        )

        val result = ProfileCompletionUtil.calculateCompletion(profile, addressCount = 1)
        assertEquals(100, result.percentage)
        assertEquals(0, result.missingFields.size)
    }

    @Test
    fun customer_partialProfile_returns50Percent() {
        val profile = UserProfile(
            name = "Mehmet Demir",
            email = "mehmet@example.com",
            phone = "",
            address = "",
            role = UserRole.CUSTOMER
        )

        val result = ProfileCompletionUtil.calculateCompletion(profile, addressCount = 0)
        assertEquals(50, result.percentage)
        assertEquals(2, result.missingFields.size)
    }

    @Test
    fun merchant_fullProfile_returns100Percent() {
        val profile = UserProfile(
            name = "Hasan Usta",
            phone = "05321112233",
            businessName = "Usta Berber",
            category = "Berber & Kuaför",
            address = "Kadıköy Çarşı No:5 İstanbul",
            workingHours = "08:00 - 20:00",
            role = UserRole.MERCHANT
        )

        val result = ProfileCompletionUtil.calculateCompletion(profile, addressCount = 0)
        assertEquals(100, result.percentage)
        assertEquals(0, result.missingFields.size)
    }
}