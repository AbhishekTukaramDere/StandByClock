package com.yourcompany.standby.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Slider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.yourcompany.standby.data.local.entity.JournalEntry
import com.yourcompany.standby.ui.components.PhotoUploadSheet
import com.yourcompany.standby.ui.components.SolidColorPickerSheet
import com.yourcompany.standby.ui.components.EditQuoteBottomSheet
import com.yourcompany.standby.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun PhotoJournalScreen(
    activeEntry: JournalEntry?,
    is24Hour: Boolean,
    onSaveQuote: (
        quote: String,
        author: String,
        fontSize: Int,
        colorHex: String,
        isBold: Boolean,
        isItalic: Boolean,
        alignment: String
    ) -> Unit,
    onNavigateToManagement: () -> Unit,
    onImageSelected: (String?) -> Unit,
    onNavigateToCrop: (String) -> Unit,
    onNextEntry: () -> Unit,
    modifier: Modifier = Modifier,
    currentTime: LocalDateTime = LocalDateTime.now()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLandscape = LocalConfiguration.current.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    val timeStr = currentTime.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "hh:mm a"))
    val dateStr = currentTime.format(DateTimeFormatter.ofPattern("EEEE, d MMM"))

    var showUploadSheet by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showSettingsCog by remember { mutableStateOf(false) }
    var showEditQuoteSheet by remember { mutableStateOf(false) }
    var showSettingsRedirectDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        showSettingsCog = true
    }

    // Camera & Gallery file launchers
    val tempPhotoFile = remember { File(context.filesDir, "photos/temp_capture_journal.jpg") }
    val tempPhotoUri = remember {
        androidx.core.content.FileProvider.getUriForFile(
            context,
            "com.yourcompany.standby.fileprovider",
            tempPhotoFile
        )
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                coroutineScope.launch(Dispatchers.IO) {
                    ImageUtils.downsampleFileInPlace(context, tempPhotoFile)
                    launch(Dispatchers.Main) {
                        onNavigateToCrop(tempPhotoFile.absolutePath)
                    }
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                tempPhotoFile.delete()
                tempPhotoFile.parentFile?.mkdirs()
                cameraLauncher.launch(tempPhotoUri)
            } else {
                val activity = context as? Activity
                val shouldShowRationale = activity?.let {
                    androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale(it, android.Manifest.permission.CAMERA)
                } ?: true
                
                if (!shouldShowRationale) {
                    showSettingsRedirectDialog = true
                } else {
                    Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                coroutineScope.launch(Dispatchers.IO) {
                    val downsampledPath = ImageUtils.saveAndDownsampleImage(context, uri, 1024)
                    if (downsampledPath != null) {
                        launch(Dispatchers.Main) {
                            onNavigateToCrop(downsampledPath)
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            Toast.makeText(context, "Error loading image from gallery", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    )

    val quoteText = activeEntry?.quoteText ?: "Peace begins with a smile."
    val quoteAuthor = activeEntry?.quoteAuthor ?: "Mother Teresa"

    // Typography configuration from activeEntry
    val quoteColor = activeEntry?.colorHex?.let {
        try { Color(android.graphics.Color.parseColor(it)) } catch(e: Exception) { Color.White.copy(alpha = 0.7f) }
    } ?: Color.White.copy(alpha = 0.7f)
    val quoteFontSize = activeEntry?.fontSize?.sp ?: 16.sp
    val quoteWeight = if (activeEntry?.isBold == true) FontWeight.Bold else FontWeight.Normal
    val quoteStyle = if (activeEntry?.isItalic == true) FontStyle.Italic else FontStyle.Normal
    val quoteAlign = when (activeEntry?.alignment?.uppercase()) {
        "LEFT" -> TextAlign.Start
        "RIGHT" -> TextAlign.End
        else -> TextAlign.Center
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLandscape) {
            // Landscape Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 40%: Square Photo tile
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxHeight()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .combinedClickable(
                            onLongClick = { showUploadSheet = true },
                            onClick = { showSettingsCog = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PhotoOrPlaceholder(activeEntry = activeEntry)
                }

                Spacer(modifier = Modifier.width(32.dp))

                // Right 60%
                Column(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column {
                        Text(
                            text = timeStr,
                            color = Color.White,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = dateStr,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp
                        )
                    }

                    Text(
                        text = "\"$quoteText\"",
                        color = quoteColor,
                        fontSize = quoteFontSize,
                        fontWeight = quoteWeight,
                        fontStyle = quoteStyle,
                        textAlign = quoteAlign,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = { showEditQuoteSheet = true },
                                onLongClick = { showEditQuoteSheet = true }
                            )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UtilityButton(icon = Icons.AutoMirrored.Filled.List, contentDescription = "Manage") {
                            onNavigateToManagement()
                        }
                        UtilityButton(icon = Icons.Default.SkipNext, contentDescription = "Next Entry") {
                            onNextEntry()
                        }
                    }
                }
            }
        } else {
            // Portrait Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Media Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(32.dp))
                        .combinedClickable(
                            onLongClick = { showUploadSheet = true },
                            onClick = { showSettingsCog = false }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PhotoOrPlaceholder(activeEntry = activeEntry)
                }

                // Time & Date
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeStr,
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateStr,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }

                // Quote (literary footnote)
                Text(
                    text = "\"$quoteText\"",
                    color = quoteColor,
                    fontSize = quoteFontSize,
                    fontWeight = quoteWeight,
                    fontStyle = quoteStyle,
                    textAlign = quoteAlign,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .combinedClickable(
                            onClick = { showEditQuoteSheet = true },
                            onLongClick = { showEditQuoteSheet = true }
                        )
                )

                // Utility buttons
                Row(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UtilityButton(icon = Icons.AutoMirrored.Filled.List, contentDescription = "Manage") {
                        onNavigateToManagement()
                    }
                    UtilityButton(icon = Icons.Default.SkipNext, contentDescription = "Next Entry") {
                        onNextEntry()
                    }
                }
            }
        }

        // Settings Cog (appears after 2s idle)
        LaunchedEffect(showSettingsCog) {
            if (!showSettingsCog) {
                delay(2000)
                showSettingsCog = true
            }
        }

        AnimatedVisibility(
            visible = showSettingsCog,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
        ) {
            IconButton(
                onClick = { showUploadSheet = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Upload bottom drawer (extended with option to go to Manage)
        PhotoUploadSheet(
            visible = showUploadSheet,
            hasPhoto = activeEntry?.imagePath != null,
            onDismiss = { showUploadSheet = false },
            onTakePhoto = {
                showUploadSheet = false
                val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.CAMERA
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                
                if (hasCameraPermission) {
                    tempPhotoFile.delete()
                    tempPhotoFile.parentFile?.mkdirs()
                    cameraLauncher.launch(tempPhotoUri)
                } else {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }
            },
            onChooseGallery = {
                showUploadSheet = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onChooseColor = {
                showUploadSheet = false
                showColorPicker = true
            },
            onCropCurrent = {
                showUploadSheet = false
                val path = activeEntry?.imagePath
                if (path != null && !path.startsWith("solid:")) {
                    onNavigateToCrop(path)
                } else {
                    Toast.makeText(context, "No image to crop", Toast.LENGTH_SHORT).show()
                }
            },
            onRemovePhoto = {
                showUploadSheet = false
                onImageSelected(null)
            }
        )

        // Color Picker sheet
        SolidColorPickerSheet(
            visible = showColorPicker,
            initialSelectedColorHex = activeEntry?.imagePath,
            onDismiss = { showColorPicker = false },
            onColorSelected = { colorHex ->
                showColorPicker = false
                onImageSelected(colorHex)
            }
        )

        // Reusable Edit Quote Bottom Sheet
        if (showEditQuoteSheet) {
            EditQuoteBottomSheet(
                visible = showEditQuoteSheet,
                initialText = quoteText,
                initialAuthor = quoteAuthor,
                initialFontSize = activeEntry?.fontSize ?: 16,
                initialColorHex = activeEntry?.colorHex ?: "#FFFFFF",
                initialIsBold = activeEntry?.isBold ?: false,
                initialIsItalic = activeEntry?.isItalic ?: false,
                initialAlignment = activeEntry?.alignment ?: "CENTER",
                onDismiss = { showEditQuoteSheet = false },
                onSave = { newQuote, newAuthor, fontSize, colorHex, isBold, isItalic, alignment ->
                    showEditQuoteSheet = false
                    onSaveQuote(newQuote, newAuthor, fontSize, colorHex, isBold, isItalic, alignment)
                }
            )
        }

        // Settings Redirect Dialog
        if (showSettingsRedirectDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showSettingsRedirectDialog = false },
                title = { Text("Camera Permission Required", color = Color.White) },
                text = { Text("Camera permission was permanently denied. Please enable it in system settings to take photos.", color = Color.White.copy(alpha = 0.8f)) },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            showSettingsRedirectDialog = false
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.fromParts("package", context.packageName, null)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    ) {
                        Text("Go to Settings", color = Color(0xFFFF6B00))
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showSettingsRedirectDialog = false }) {
                        Text("Cancel", color = Color.White)
                    }
                },
                containerColor = Color(0xFF1E1E1E)
            )
        }
    }
}

@Composable
private fun PhotoOrPlaceholder(activeEntry: JournalEntry?) {
    val context = LocalContext.current
    when {
        activeEntry == null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF4DB6AC)),
                contentAlignment = Alignment.Center
            ) {
                MinimalistDogPlaceholder(modifier = Modifier.fillMaxSize(0.6f))
            }
        }
        activeEntry.imagePath.startsWith("solid:") -> {
            val hex = activeEntry.imagePath.removePrefix("solid:")
            val color = try {
                Color(android.graphics.Color.parseColor(hex))
            } catch (e: Exception) {
                Color(0xFFFF0000)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            )
        }
        else -> {
            val resPath = remember(activeEntry.imagePath) {
                if (activeEntry.imagePath.startsWith("/")) activeEntry.imagePath
                else File(context.filesDir, activeEntry.imagePath).absolutePath
            }
            val modelRequest = remember(resPath, context) {
                coil.request.ImageRequest.Builder(context)
                    .data(resPath)
                    .size(1024)
                    .crossfade(true)
                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                    .error(android.R.drawable.ic_menu_camera)
                    .placeholder(android.R.drawable.ic_menu_camera)
                    .build()
            }
            AsyncImage(
                model = modelRequest,
                contentDescription = "Journal Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun MinimalistDogPlaceholder(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width * 0.25f

        // Head outline (white)
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = r,
            center = Offset(cx, cy)
        )

        // Left ear
        val leftEar = Path().apply {
            moveTo(cx - r, cy - r * 0.3f)
            cubicTo(cx - r * 1.6f, cy - r * 1.2f, cx - r * 1.3f, cy + r * 0.8f, cx - r * 0.8f, cy + r * 0.4f)
        }
        drawPath(path = leftEar, color = Color.White.copy(alpha = 0.8f))

        // Right ear
        val rightEar = Path().apply {
            moveTo(cx + r, cy - r * 0.3f)
            cubicTo(cx + r * 1.6f, cy - r * 1.2f, cx + r * 1.3f, cy + r * 0.8f, cx + r * 0.8f, cy + r * 0.4f)
        }
        drawPath(path = rightEar, color = Color.White.copy(alpha = 0.8f))

        // Nose
        drawCircle(
            color = Color(0xFF2A2A2A),
            radius = r * 0.25f,
            center = Offset(cx, cy + r * 0.4f)
        )

        // Eyes
        drawCircle(
            color = Color(0xFF2A2A2A),
            radius = r * 0.12f,
            center = Offset(cx - r * 0.4f, cy - r * 0.1f)
        )
        drawCircle(
            color = Color(0xFF2A2A2A),
            radius = r * 0.12f,
            center = Offset(cx + r * 0.4f, cy - r * 0.1f)
        )
    }
}

@Composable
private fun UtilityButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .border(1.dp, Color.White, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(device = "spec:width=411dp,height=891dp")
@Composable
fun PhotoJournalScreenPortraitPreview() {
    PhotoJournalScreen(
        activeEntry = null,
        is24Hour = false,
        onSaveQuote = { _, _, _, _, _, _, _ -> },
        onNavigateToManagement = {},
        onImageSelected = {},
        onNavigateToCrop = {},
        onNextEntry = {}
    )
}
