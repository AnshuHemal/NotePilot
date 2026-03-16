package com.white.notepilot.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.text.Html
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QRCodeHelper {
    
    fun generateQRCode(
        content: String,
        size: Int = 512,
        backgroundColor: Int = Color.WHITE,
        foregroundColor: Int = Color.BLACK
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
                }
            }
            
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun encodeNoteToQR(
        title: String, 
        htmlContent: String, 
        categories: String = "",
        passwordHash: String = "",
        isLocked: Boolean = false
    ): String {
        // Encode note with categories and password info
        // Format: notepilot://share?title=...&content=...&categories=...&password=...&locked=...
        // Using deep link format for app detection
        val encodedTitle = android.net.Uri.encode(title)
        val encodedContent = android.net.Uri.encode(htmlContent)
        val encodedCategories = android.net.Uri.encode(categories)
        val encodedPassword = android.net.Uri.encode(passwordHash)
        
        return "notepilot://share?title=$encodedTitle&content=$encodedContent&categories=$encodedCategories&password=$encodedPassword&locked=$isLocked"
    }
    
    data class QRNoteData(
        val title: String,
        val htmlContent: String,
        val categories: String,
        val passwordHash: String,
        val isLocked: Boolean
    )
    
    fun decodeNoteFromQR(qrContent: String): QRNoteData? {
        return try {
            if (qrContent.startsWith("notepilot://share")) {
                val uri = android.net.Uri.parse(qrContent)
                val title = uri.getQueryParameter("title") ?: ""
                val htmlContent = uri.getQueryParameter("content") ?: ""
                val categories = uri.getQueryParameter("categories") ?: ""
                val passwordHash = uri.getQueryParameter("password") ?: ""
                val isLocked = uri.getQueryParameter("locked")?.toBoolean() ?: false
                
                QRNoteData(title, htmlContent, categories, passwordHash, isLocked)
            } else if (qrContent.startsWith("NOTEPILOT:")) {
                // Backward compatibility with old format
                val data = qrContent.removePrefix("NOTEPILOT:")
                val parts = data.split("|||")
                if (parts.size >= 2) {
                    val title = parts[0]
                    val htmlContent = parts[1]
                    val categories = if (parts.size >= 3) parts[2] else ""
                    val passwordHash = if (parts.size >= 4) parts[3] else ""
                    val isLocked = if (parts.size >= 5) parts[4].toBoolean() else false
                    
                    QRNoteData(title, htmlContent, categories, passwordHash, isLocked)
                } else null
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
