package com.white.notepilot.utils

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.core.content.FileProvider
import com.white.notepilot.data.model.Note
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShareHelper {
    
    private const val TAG = "ShareHelper"
    private const val FILE_PROVIDER_AUTHORITY = "com.white.notepilot.fileprovider"
    
    enum class ShareFormat {
        TEXT,
        HTML,
        PDF
    }
    
    /**
     * Share note as plain text
     */
    fun shareAsText(context: Context, note: Note) {
        try {
            val plainText = convertHtmlToPlainText(note.content)
            val shareText = "${note.title}\n\n$plainText"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Note"))
            Log.d(TAG, "Shared note as text: ${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing note as text", e)
        }
    }
    
    /**
     * Share note as HTML file
     */
    fun shareAsHtml(context: Context, note: Note) {
        try {
            val htmlContent = generateHtmlDocument(note)
            val file = createTempFile(context, note.title, "html", htmlContent)
            
            val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Note as HTML"))
            Log.d(TAG, "Shared note as HTML: ${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing note as HTML", e)
        }
    }
    
    /**
     * Share note as PDF file
     */
    fun shareAsPdf(context: Context, note: Note) {
        try {
            val pdfFile = generatePdfFile(context, note)
            val uri = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, pdfFile)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, note.title)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Note as PDF"))
            Log.d(TAG, "Shared note as PDF: ${note.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing note as PDF", e)
        }
    }
    
    /**
     * Export note to file and return URI
     */
    fun exportNote(context: Context, note: Note, format: ShareFormat): Uri? {
        return try {
            when (format) {
                ShareFormat.TEXT -> {
                    val plainText = convertHtmlToPlainText(note.content)
                    val content = "${note.title}\n\n$plainText"
                    val file = createTempFile(context, note.title, "txt", content)
                    FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
                }
                ShareFormat.HTML -> {
                    val htmlContent = generateHtmlDocument(note)
                    val file = createTempFile(context, note.title, "html", htmlContent)
                    FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file)
                }
                ShareFormat.PDF -> {
                    val pdfFile = generatePdfFile(context, note)
                    FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, pdfFile)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting note", e)
            null
        }
    }
    
    /**
     * Convert HTML content to plain text
     */
    private fun convertHtmlToPlainText(html: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString().trim()
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(html).toString().trim()
        }
    }
    
    /**
     * Generate complete HTML document with styling
     */
    private fun generateHtmlDocument(note: Note): String {
        val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(note.timestamp))
        
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>${note.title}</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 800px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background-color: white;
                        padding: 40px;
                        border-radius: 8px;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                    }
                    h1 {
                        color: #2c3e50;
                        margin-bottom: 10px;
                        font-size: 2em;
                    }
                    .timestamp {
                        color: #7f8c8d;
                        font-size: 0.9em;
                        margin-bottom: 30px;
                    }
                    .content {
                        font-size: 1.1em;
                    }
                    code {
                        background-color: #f4f4f4;
                        padding: 2px 6px;
                        border-radius: 3px;
                        font-family: 'Courier New', monospace;
                    }
                    a {
                        color: #3498db;
                        text-decoration: none;
                    }
                    a:hover {
                        text-decoration: underline;
                    }
                    ul, ol {
                        margin: 15px 0;
                        padding-left: 30px;
                    }
                    .footer {
                        margin-top: 40px;
                        padding-top: 20px;
                        border-top: 1px solid #ecf0f1;
                        text-align: center;
                        color: #95a5a6;
                        font-size: 0.85em;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>${note.title}</h1>
                    <div class="timestamp">Created on $timestamp</div>
                    <div class="content">
                        ${note.content}
                    </div>
                    <div class="footer">
                        Created with NotePilot
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Generate PDF file from note
     */
    private fun generatePdfFile(context: Context, note: Note): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas = page.canvas
        val paint = android.graphics.Paint().apply {
            textSize = 16f
            color = android.graphics.Color.BLACK
        }
        
        val titlePaint = android.graphics.Paint().apply {
            textSize = 24f
            color = android.graphics.Color.BLACK
            isFakeBoldText = true
        }
        
        val timestampPaint = android.graphics.Paint().apply {
            textSize = 12f
            color = android.graphics.Color.GRAY
        }
        
        var yPosition = 80f
        val leftMargin = 50f
        val rightMargin = 545f
        val lineHeight = 20f
        
        // Draw title
        canvas.drawText(note.title, leftMargin, yPosition, titlePaint)
        yPosition += 30f
        
        // Draw timestamp
        val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(note.timestamp))
        canvas.drawText(timestamp, leftMargin, yPosition, timestampPaint)
        yPosition += 40f
        
        // Draw content (plain text)
        val plainText = convertHtmlToPlainText(note.content)
        val words = plainText.split(" ")
        var line = ""
        
        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            val textWidth = paint.measureText(testLine)
            
            if (textWidth > (rightMargin - leftMargin)) {
                canvas.drawText(line, leftMargin, yPosition, paint)
                line = word
                yPosition += lineHeight
                
                // Check if we need a new page
                if (yPosition > 780f) {
                    pdfDocument.finishPage(page)
                    val newPage = pdfDocument.startPage(pageInfo)
                    yPosition = 50f
                }
            } else {
                line = testLine
            }
        }
        
        // Draw remaining text
        if (line.isNotEmpty()) {
            canvas.drawText(line, leftMargin, yPosition, paint)
        }
        
        pdfDocument.finishPage(page)
        
        // Save to file
        val file = File(context.cacheDir, "${sanitizeFileName(note.title)}.pdf")
        FileOutputStream(file).use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        
        return file
    }
    
    /**
     * Create temporary file with content
     */
    private fun createTempFile(
        context: Context,
        fileName: String,
        extension: String,
        content: String
    ): File {
        val sanitizedName = sanitizeFileName(fileName)
        val file = File(context.cacheDir, "$sanitizedName.$extension")
        file.writeText(content)
        return file
    }
    
    /**
     * Sanitize file name to remove invalid characters
     */
    private fun sanitizeFileName(fileName: String): String {
        return fileName
            .replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .take(50) // Limit length
    }
    
    /**
     * Clean up temporary files
     */
    fun cleanupTempFiles(context: Context) {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.extension in listOf("txt", "html", "pdf")) {
                    file.delete()
                }
            }
            Log.d(TAG, "Cleaned up temporary files")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up temp files", e)
        }
    }
}
