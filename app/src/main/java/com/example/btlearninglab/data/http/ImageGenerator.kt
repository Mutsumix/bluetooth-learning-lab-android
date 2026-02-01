package com.example.btlearninglab.data.http

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for generating demo images for E-Paper display.
 * Generates 296x128 JPEG images with quality 95.
 */
object ImageGenerator {

    /**
     * Generates an image with weight and date for E-Paper display.
     *
     * @param weight Current weight in grams
     * @param date Date to display (defaults to today)
     * @return ByteArray containing JPEG image data (296x128, quality 95)
     */
    fun generateWeightImage(weight: Double, date: Date = Date()): ByteArray {
        // Create 296x128 Bitmap
        val bitmap = Bitmap.createBitmap(296, 128, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background with white
        canvas.drawColor(Color.WHITE)

        // Format date
        val dateFormatter = SimpleDateFormat("yyyy/MM/dd", Locale.JAPAN)
        val dateString = dateFormatter.format(date)

        // Draw date
        val datePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(dateString, 10f, 25f, datePaint)

        // Draw weight label
        val labelPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText("現在の重さ:", 10f, 60f, labelPaint)

        // Draw weight value (large and bold)
        val weightPaint = Paint().apply {
            color = Color.BLACK
            textSize = 48f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val weightText = String.format(Locale.JAPAN, "%.1f g", weight)
        canvas.drawText(weightText, 10f, 110f, weightPaint)

        // Convert to JPEG with quality 95
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)

        return outputStream.toByteArray()
    }

    /**
     * Generates a demo image for E-Paper display.
     *
     * @return ByteArray containing JPEG image data (296x128, quality 95)
     */
    @Deprecated("Use generateWeightImage instead")
    fun generateDemoImage(): ByteArray {
        return generateWeightImage(0.0)
    }
}
