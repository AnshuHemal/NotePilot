package com.white.notepilot.utils

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64

object PasswordHelper {
    
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val KEY_ALGORITHM = "AES"
    private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 10000
    private const val KEY_LENGTH = 256
    
    fun hashPassword(password: String): String {
        val salt = generateSalt()
        val hash = pbkdf2(password, salt)
        return "$salt:$hash"
    }
    
    fun verifyPassword(password: String, storedHash: String): Boolean {
        return try {
            val parts = storedHash.split(":")
            if (parts.size != 2) return false
            
            val salt = parts[0]
            val hash = parts[1]
            val testHash = pbkdf2(password, salt)
            
            hash == testHash
        } catch (e: Exception) {
            false
        }
    }
    
    private fun pbkdf2(password: String, salt: String): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }
    
    fun encryptContent(content: String, password: String): String {
        val salt = generateSalt()
        val key = deriveKey(password, salt)
        val iv = generateIV()
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val secretKey = SecretKeySpec(key, KEY_ALGORITHM)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encrypted = cipher.doFinal(content.toByteArray())
        
        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedString = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        
        return "$salt:$ivString:$encryptedString"
    }
    
    fun decryptContent(encryptedData: String, password: String): String? {
        return try {
            val parts = encryptedData.split(":")
            if (parts.size != 3) return null
            
            val salt = parts[0]
            val ivString = parts[1]
            val encryptedString = parts[2]
            
            val key = deriveKey(password, salt)
            val iv = Base64.decode(ivString, Base64.NO_WRAP)
            val encrypted = Base64.decode(encryptedString, Base64.NO_WRAP)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val secretKey = SecretKeySpec(key, KEY_ALGORITHM)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decrypted = cipher.doFinal(encrypted)
            
            String(decrypted)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun deriveKey(password: String, salt: String): ByteArray {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt.toByteArray(),
            ITERATIONS,
            KEY_LENGTH
        )
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        return factory.generateSecret(spec).encoded
    }
    
    private fun generateIV(): ByteArray {
        val random = SecureRandom()
        val iv = ByteArray(16)
        random.nextBytes(iv)
        return iv
    }
    
    fun validatePassword(password: String): PasswordValidation {
        return when {
            password.length < 4 -> PasswordValidation.TooShort
            password.length > 20 -> PasswordValidation.TooLong
            else -> PasswordValidation.Valid
        }
    }
    
    sealed class PasswordValidation {
        object Valid : PasswordValidation()
        object TooShort : PasswordValidation()
        object TooLong : PasswordValidation()
    }
}
