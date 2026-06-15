package com.yourcompany.standby.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.graphics.scale
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ImageUtils {

    fun saveBitmapToFile(bitmap: Bitmap, outputFile: File, quality: Int = 85): Boolean {
        return try {
            outputFile.parentFile?.mkdirs()
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun downloadImageToFile(context: Context, imageUrl: String, outputFile: File): Boolean {
        return try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Required for drawing and converting to files
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val drawable = result.drawable
                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                if (bitmap != null) {
                    val maxDimension = 1024
                    val scaled = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
                        val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
                        val newWidth = if (ratio > 1) maxDimension else (maxDimension * ratio).toInt()
                        val newHeight = if (ratio > 1) (maxDimension / ratio).toInt() else maxDimension
                        bitmap.scale(newWidth, newHeight)
                    } else {
                        bitmap
                    }
                    saveBitmapToFile(scaled, outputFile, 85)
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun cropToAspectRatio(bitmap: Bitmap, targetRatio: Float = 16f / 9f): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val currentRatio = width.toFloat() / height.toFloat()

        var newWidth = width
        var newHeight = height
        var xOffset = 0
        var yOffset = 0

        if (currentRatio > targetRatio) {
            // Target is taller: crop width
            newWidth = (height * targetRatio).toInt()
            xOffset = (width - newWidth) / 2
        } else if (currentRatio < targetRatio) {
            // Target is wider: crop height
            newHeight = (width / targetRatio).toInt()
            yOffset = (height - newHeight) / 2
        }

        return Bitmap.createBitmap(bitmap, xOffset, yOffset, newWidth, newHeight)
    }

    fun cleanupOldCacheFiles(context: Context, maxAgeDays: Int = 7) {
        val sharedCacheDir = File(context.cacheDir, "shared")
        if (sharedCacheDir.exists() && sharedCacheDir.isDirectory) {
            val files = sharedCacheDir.listFiles() ?: return
            val threshold = System.currentTimeMillis() - (maxAgeDays * 24L * 60 * 60 * 1000)
            files.forEach { file ->
                if (file.lastModified() < threshold) {
                    file.delete()
                }
            }
        }
    }

    // Programmatically render the Card Layout onto a 1080x608 bitmap for wallpaper and sharing
    fun renderCardToBitmap(
        context: Context,
        photoPath: String?,
        quoteText: String?,
        quoteTextColorHex: String,
        quoteTextAlignment: String,
        quoteTextSizeSp: Int,
        quoteIsBold: Boolean,
        quoteIsItalic: Boolean,
        photographerName: String?
    ): Bitmap {
        val width = 1080
        val height = 608
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Draw base background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.parseColor("#1A1A1A")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // 2. Draw 40% Left Photo area
        val photoWidth = (width * 0.4f).toInt()
        if (photoPath?.startsWith("solid:") == true) {
            val colorHex = photoPath.removePrefix("solid:")
            val parsedColor = try {
                android.graphics.Color.parseColor(colorHex)
            } catch (e: Exception) {
                android.graphics.Color.parseColor("#FF0000")
            }
            paint.color = parsedColor
            canvas.drawRect(0f, 0f, photoWidth.toFloat(), height.toFloat(), paint)
        } else {
            val resolvedPath = photoPath?.let { path ->
                if (path.startsWith("/")) path else File(context.filesDir, path).absolutePath
            }
            val photoFile = if (!resolvedPath.isNullOrEmpty()) File(resolvedPath) else null

            if (photoFile != null && photoFile.exists()) {
                try {
                    val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                    val photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath, options)
                    if (photoBitmap != null) {
                        val croppedPhoto = cropToAspectRatio(photoBitmap, 4f / 6f) // aspect ratio matching portion
                        val destRect = Rect(0, 0, photoWidth, height)
                        canvas.drawBitmap(croppedPhoto, null, destRect, paint)
                    }
                } catch (e: Exception) {
                    // Draw dark placeholder on error
                    paint.color = android.graphics.Color.parseColor("#2A2A2A")
                    canvas.drawRect(0f, 0f, photoWidth.toFloat(), height.toFloat(), paint)
                }
            } else {
                // Draw dark placeholder
                paint.color = android.graphics.Color.parseColor("#2A2A2A")
                canvas.drawRect(0f, 0f, photoWidth.toFloat(), height.toFloat(), paint)
            }
        }

        // 3. Draw time and date (Top-Right of remaining 60%)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.RIGHT
        }

        // Time
        val currentTime = LocalDateTime.now()
        textPaint.textSize = 42f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val timeStr = currentTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        canvas.drawText(timeStr, width - 40f, 80f, textPaint)

        // Date
        textPaint.textSize = 20f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val dateStr = currentTime.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))
        canvas.drawText(dateStr, width - 40f, 120f, textPaint)

        // 4. Draw Quote text centered on the right 60% portion using StaticLayout for multiline wrapping
        val quote = quoteText ?: "Tap to add your quote or reminder"
        val textPaintForQuote = android.text.TextPaint(android.text.TextPaint.ANTI_ALIAS_FLAG).apply {
            color = try {
                android.graphics.Color.parseColor(quoteTextColorHex)
            } catch (e: Exception) {
                android.graphics.Color.WHITE
            }
            textSize = (quoteTextSizeSp * 2.0f).coerceIn(24f, 48f) // Scale for 1080p canvas size
            val typefaceStyle = when {
                quoteIsBold && quoteIsItalic -> Typeface.BOLD_ITALIC
                quoteIsBold -> Typeface.BOLD
                quoteIsItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            typeface = Typeface.create(Typeface.DEFAULT, typefaceStyle)
        }

        val alignment = when (quoteTextAlignment.uppercase()) {
            "LEFT" -> android.text.Layout.Alignment.ALIGN_NORMAL
            "RIGHT" -> android.text.Layout.Alignment.ALIGN_OPPOSITE
            else -> android.text.Layout.Alignment.ALIGN_CENTER
        }

        val rightSideStart = photoWidth + 40
        val rightSideWidth = width - photoWidth - 80

        val staticLayout = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.text.StaticLayout.Builder.obtain(quote, 0, quote.length, textPaintForQuote, rightSideWidth)
                .setAlignment(alignment)
                .setLineSpacing(0f, 1.2f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            android.text.StaticLayout(
                quote,
                textPaintForQuote,
                rightSideWidth,
                alignment,
                1.2f,
                0f,
                false
            )
        }

        // Center the StaticLayout vertically in the remaining height
        canvas.save()
        val yOffset = (height - staticLayout.height) / 2f
        canvas.translate(rightSideStart.toFloat(), yOffset)
        staticLayout.draw(canvas)
        canvas.restore()

        // 5. Draw photographer attribution (Bottom-Left)
        if (!photographerName.isNullOrEmpty()) {
            val attribPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.argb(128, 255, 255, 255)
                textSize = 14f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText("Photo by $photographerName", 20f, height - 20f, attribPaint)
        }

        return bitmap
    }

    fun saveAndDownsampleImage(context: Context, uri: android.net.Uri, targetSize: Int = 1024): String? {
        return try {
            val resolver = context.contentResolver
            var rotation = 0
            resolver.openInputStream(uri)?.use { input ->
                val exifInterface = android.media.ExifInterface(input)
                val orientation = exifInterface.getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION,
                    android.media.ExifInterface.ORIENTATION_UNDEFINED
                )
                rotation = when (orientation) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            }

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, options)
            }

            val srcWidth = options.outWidth
            val srcHeight = options.outHeight
            if (srcWidth <= 0 || srcHeight <= 0) return null

            var inSampleSize = 1
            val maxDim = maxOf(srcWidth, srcHeight)
            if (maxDim > targetSize) {
                val halfDim = maxDim / 2
                while (halfDim / inSampleSize >= targetSize) {
                    inSampleSize *= 2
                }
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = inSampleSize
            }
            var bitmap = resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOptions)
            } ?: return null

            val currentMax = maxOf(bitmap.width, bitmap.height)
            if (currentMax > targetSize || rotation != 0) {
                val scale = if (currentMax > targetSize) targetSize.toFloat() / currentMax else 1f
                val matrix = android.graphics.Matrix().apply {
                    if (scale != 1f) postScale(scale, scale)
                    if (rotation != 0) postRotate(rotation.toFloat())
                }
                val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                    bitmap = scaledBitmap
                }
            }

            val relativePath = "photos/temp_${java.util.UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, relativePath)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun downsampleFileInPlace(context: Context, file: File, targetSize: Int = 1024) {
        try {
            val uri = android.net.Uri.fromFile(file)
            val downsampledPath = saveAndDownsampleImage(context, uri, targetSize)
            if (downsampledPath != null) {
                val downsampledFile = File(downsampledPath)
                file.delete()
                downsampledFile.renameTo(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
