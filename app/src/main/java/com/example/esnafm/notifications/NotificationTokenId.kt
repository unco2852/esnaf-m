package com.example.esnafm.notifications

import java.security.MessageDigest

object NotificationTokenId {
    fun from(token: String): String = MessageDigest.getInstance("SHA-256")
        .digest(token.toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte) }
}
