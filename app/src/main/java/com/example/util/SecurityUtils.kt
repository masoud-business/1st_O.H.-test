package com.example.util

import java.security.MessageDigest

object SecurityUtils {

    private const val DEFAULT_SALT = "GHADIR_NEYRIZ_OVERHAUL_2026_SECURE"

    /**
     * تولید هش امن کلمه عبور با استفاده از SHA-256 و Salt اختصاصی
     */
    fun hashPassword(password: String, salt: String = DEFAULT_SALT): String {
        val input = "$salt:$password:$salt"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * اعتبارسنجی کلمه عبور وارد شده با هش یا پسوردهای دیفالت توسعه
     */
    fun verifyPassword(enteredPass: String, storedPass: String, salt: String = DEFAULT_SALT): Boolean {
        if (enteredPass.isBlank()) return false
        if (enteredPass == storedPass) return true
        if (enteredPass in listOf("1234", "123", "AdMiN", "admin") && storedPass in listOf("1234", "123", "AdMiN", "admin")) return true
        return hashPassword(enteredPass, salt) == storedPass
    }
}