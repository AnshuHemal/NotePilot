package com.white.notepilot.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Html
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.white.notepilot.R

object QRCodeImageGenerator {
    
    fun generateNotePreviewImage(
        context: Context,
        title: String,
        htmlContent: String,
        categories: String = "",
        width: Int = 1080,
        isDarkMode: Boolean = false
    ): Bitmap {
        val padding = 60f
        val titleSize = 72f
        val contentSize = 48f
        val categorySize = 42f
        val lineSpacing = 1.5f
        
        val backgroundColor = if (isDarkMode) Color.parseColor("#1E1E1E") else Color.WHITE
        val textColor = if (isDarkMode) Color.WHITE else Color.parseColor("#212121")
        val accentColor = Color.parseColor("#6200EE")
        val categoryBgColor = if (isDarkMode) Color.parseColor("#3700B3") else Color.parseColor("#E8DEF8")
        val categoryTextColor = if (isDarkMode) Color.WHITE else Color.parseColor("#6200EE")
        
        val plainContent = Html.fromHtml(htmlContent, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        
        val titlePaint = TextPaint().apply {
            color = textColor
            textSize = titleSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val contentPaint = TextPaint().apply {
            color = textColor
            textSize = contentSize
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        
        val categoryPaint = TextPaint().apply {
            color = categoryTextColor
            textSize = categorySize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        
        val availableWidth = (width - 2 * padding).toInt()
        
        val titleLayout = StaticLayout.Builder.obtain(
            title,
            0,
            title.length,
            titlePaint,
            availableWidth
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(lineSpacing, 1f)
            .setIncludePad(false)
            .build()
        
        val contentLayout = StaticLayout.Builder.obtain(
            plainContent,
            0,
            plainContent.length,
            contentPaint,
            availableWidth
        ).setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(lineSpacing, 1f)
            .setIncludePad(false)
            .build()
        
        val headerHeight = 120f
        val dividerHeight = 4f
        val spaceBetween = 60f
        val categorySpacing = 40f
        
        // Parse categories
        val categoryList = if (categories.isNotBlank()) {
            categories.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            emptyList()
        }
        
        // Calculate category heights
        var categoryTotalHeight = 0f
        if (categoryList.isNotEmpty()) {
            categoryTotalHeight = categorySpacing + (categoryList.size * 80f) + categorySpacing
        }
        
        val totalHeight = (padding + headerHeight + dividerHeight + spaceBetween + 
                          titleLayout.height + categoryTotalHeight + spaceBetween + 
                          contentLayout.height + padding).toInt()
        
        val bitmap = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        canvas.drawColor(backgroundColor)
        
        val headerPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width.toFloat(), headerHeight, headerPaint)
        
        val headerTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val headerTextBounds = Rect()
        val headerText = "NotePilot"
        headerTextPaint.getTextBounds(headerText, 0, headerText.length, headerTextBounds)
        canvas.drawText(
            headerText,
            width / 2f,
            headerHeight / 2f + headerTextBounds.height() / 2f,
            headerTextPaint
        )
        
        val dividerPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        canvas.drawRect(
            padding,
            headerHeight,
            width - padding,
            headerHeight + dividerHeight,
            dividerPaint
        )
        
        var currentY = headerHeight + dividerHeight + spaceBetween
        
        canvas.save()
        canvas.translate(padding, currentY)
        titleLayout.draw(canvas)
        canvas.restore()
        
        currentY += titleLayout.height
        
        // Draw categories if present
        if (categoryList.isNotEmpty()) {
            currentY += categorySpacing
            
            val categoryBgPaint = Paint().apply {
                color = categoryBgColor
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            
            categoryList.forEach { category ->
                val categoryBounds = Rect()
                categoryPaint.getTextBounds(category, 0, category.length, categoryBounds)
                
                val categoryWidth = categoryBounds.width() + 60f
                val categoryHeight = 70f
                val categoryRadius = 35f
                
                val categoryRect = RectF(
                    padding,
                    currentY,
                    padding + categoryWidth,
                    currentY + categoryHeight
                )
                
                canvas.drawRoundRect(categoryRect, categoryRadius, categoryRadius, categoryBgPaint)
                
                canvas.drawText(
                    category,
                    padding + 30f,
                    currentY + categoryHeight / 2f + categoryBounds.height() / 2f,
                    categoryPaint
                )
                
                currentY += categoryHeight + 20f
            }
            
            currentY += categorySpacing - 20f
        }
        
        canvas.save()
        canvas.translate(padding, currentY)
        contentLayout.draw(canvas)
        canvas.restore()
        
        val cornerRadius = 24f
        val borderPaint = Paint().apply {
            color = accentColor
            style = Paint.Style.STROKE
            strokeWidth = 8f
            isAntiAlias = true
        }
        
        val borderRect = RectF(4f, 4f, width - 4f, totalHeight - 4f)
        canvas.drawRoundRect(borderRect, cornerRadius, cornerRadius, borderPaint)
        
        return bitmap
    }
}
