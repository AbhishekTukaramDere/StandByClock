package com.yourcompany.standby.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yourcompany.standby.util.ImageUtils
import java.io.File
import java.util.UUID
import kotlin.math.min

@Composable
fun CropScreen(
    photoPath: String,
    onCropped: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Resolve path (could be relative or absolute)
    val resolvedPhotoPath = remember(photoPath, context) {
        if (photoPath.startsWith("/")) photoPath else File(context.filesDir, photoPath).absolutePath
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val density = context.resources.displayMetrics.density
        val W_view = constraints.maxWidth.toFloat()
        val H_view = constraints.maxHeight.toFloat()

        // Cropping Viewport
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset = Offset(
                            x = offset.x + pan.x,
                            y = offset.y + pan.y
                        )
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = resolvedPhotoPath,
                contentDescription = "Source Photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            )

            // Crop Grid Mask representing 1:1 square viewport
            Box(
                modifier = Modifier
                    .size(300.dp) // 1:1 aspect ratio
                    .border(width = 2.dp, color = Color.White, shape = RoundedCornerShape(4.dp))
            )
        }

        // Action controls (Bottom)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onCancel,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Text(text = "Cancel", color = Color.White)
            }

            Button(
                onClick = {
                    val relativePath = "photos/${UUID.randomUUID()}.jpg"
                    val croppedFile = File(context.filesDir, relativePath)
                    try {
                        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                        val srcBitmap = BitmapFactory.decodeFile(resolvedPhotoPath, options)
                        if (srcBitmap != null) {
                            val minSize = 10
                            if (srcBitmap.width >= minSize && srcBitmap.height >= minSize) {
                                val gridSizeDp = 300f
                                val gridSizePx = gridSizeDp * density

                                // Compute scale factor of ContentScale.Fit layout
                                val fitScale = min(W_view / srcBitmap.width.toFloat(), H_view / srcBitmap.height.toFloat())
                                val scaleFactor = fitScale * scale

                                // Compute corresponding bitmap coordinates for the crop grid viewport
                                val cropSize = gridSizePx / scaleFactor
                                val cropLeft = (-gridSizePx / 2f - offset.x) / scaleFactor + srcBitmap.width.toFloat() / 2f
                                val cropTop = (-gridSizePx / 2f - offset.y) / scaleFactor + srcBitmap.height.toFloat() / 2f

                                val x = cropLeft.toInt().coerceIn(0, srcBitmap.width - minSize)
                                val y = cropTop.toInt().coerceIn(0, srcBitmap.height - minSize)
                                val maxAvailableWidth = srcBitmap.width - x
                                val maxAvailableHeight = srcBitmap.height - y
                                val maxDim = maxAvailableWidth.coerceAtMost(maxAvailableHeight)

                                val cropDimension = if (maxDim >= minSize) {
                                    cropSize.toInt().coerceIn(minSize, maxDim)
                                } else {
                                    minSize
                                }

                                val croppedBitmap = Bitmap.createBitmap(
                                    srcBitmap, x, y, cropDimension, cropDimension
                                )
                                var finalBitmap = croppedBitmap
                                if (cropDimension > 1024) {
                                    finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 1024, 1024, true)
                                }
                                val success = ImageUtils.saveBitmapToFile(finalBitmap, croppedFile, 85)
                                if (finalBitmap != croppedBitmap) {
                                    finalBitmap.recycle()
                                }
                                croppedBitmap.recycle()
                                srcBitmap.recycle()

                                if (success) {
                                    onCropped(relativePath)
                                } else {
                                    Toast.makeText(context, "Error saving cropped photo", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                srcBitmap.recycle()
                                Toast.makeText(context, "Image is too small to crop", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Unable to load original image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Cropping failed", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
            ) {
                Text(text = "Crop", color = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropScreenPreview() {
    Column {
        Box(
            modifier = Modifier
                .size(150.dp)
                .background(Color.DarkGray)
        )
    }
}
