package com.example.btlearninglab.data.http

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    /**
     * Generates an image from the logo asset for E-Paper display.
     * Loads gadget-lab-logo.png from assets, converts to grayscale, and resizes to 296x128.
     *
     * @param context Android context for accessing assets
     * @return ByteArray containing JPEG image data (296x128, quality 95)
     */
    fun generateLogoImage(context: Context): ByteArray {
        val original = context.assets.open("gadget-lab-logo.png").use { stream ->
            BitmapFactory.decodeStream(stream)
        }

        // Scale to fit 296x128 maintaining aspect ratio, then center on white background
        val targetW = 296
        val targetH = 128
        val scale = minOf(targetW.toFloat() / original.width, targetH.toFloat() / original.height)
        val scaledW = (original.width * scale).toInt()
        val scaledH = (original.height * scale).toInt()

        val scaled = Bitmap.createScaledBitmap(original, scaledW, scaledH, true)

        val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val left = (targetW - scaledW) / 2f
        val top = (targetH - scaledH) / 2f
        canvas.drawBitmap(scaled, left, top, Paint().apply { isAntiAlias = true })

        // Convert to grayscale
        val grayBitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
        val grayCanvas = Canvas(grayBitmap)
        val grayPaint = Paint().apply {
            colorFilter = android.graphics.ColorMatrixColorFilter(
                android.graphics.ColorMatrix().also { it.setSaturation(0f) }
            )
        }
        grayCanvas.drawBitmap(bitmap, 0f, 0f, grayPaint)

        val outputStream = ByteArrayOutputStream()
        grayBitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)

        return outputStream.toByteArray()
    }

    @Deprecated("Use generateWeightImage instead")
    fun generateDemoImage(): ByteArray {
        return generateWeightImage(0.0)
    }
}
