package com.yourcompany.standby.ui.screens

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import coil.compose.AsyncImage
import com.yourcompany.standby.data.local.entity.JournalEntry
import com.yourcompany.standby.ui.components.SolidColorPickerSheet
import com.yourcompany.standby.ui.viewmodel.MainViewModel
import com.yourcompany.standby.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

val JournalEntrySaver = object : Saver<JournalEntry?, Map<String, Any?>> {
    override fun SaverScope.save(value: JournalEntry?): Map<String, Any?>? {
        if (value == null) return null
        return mapOf(
            "id" to value.id,
            "imagePath" to value.imagePath,
            "quoteText" to value.quoteText,
            "quoteAuthor" to value.quoteAuthor,
            "fontSize" to value.fontSize,
            "colorHex" to value.colorHex,
            "isBold" to value.isBold,
            "isItalic" to value.isItalic,
            "alignment" to value.alignment,
            "startTime" to value.startTime,
            "endTime" to value.endTime,
            "displayOrder" to value.displayOrder,
            "isEnabled" to value.isEnabled,
            "createdAt" to value.createdAt
        )
    }

    override fun restore(value: Map<String, Any?>): JournalEntry? {
        if (value.isEmpty()) return null
        return JournalEntry(
            id = value["id"] as String,
            imagePath = value["imagePath"] as String,
            quoteText = value["quoteText"] as String,
            quoteAuthor = value["quoteAuthor"] as String,
            fontSize = value["fontSize"] as Int,
            colorHex = value["colorHex"] as String,
            isBold = value["isBold"] as Boolean,
            isItalic = value["isItalic"] as Boolean,
            alignment = value["alignment"] as String,
            startTime = value["startTime"] as? String,
            endTime = value["endTime"] as? String,
            displayOrder = value["displayOrder"] as Int,
            isEnabled = value["isEnabled"] as Boolean,
            createdAt = value["createdAt"] as Long
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalManagementScreen(
    onBack: () -> Unit,
    onNavigateToCrop: (String) -> Unit,
    croppedPath: String?,
    onConsumeCropResult: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val appState by viewModel.appState.collectAsState()
    val entries by viewModel.journalEntries.collectAsState()

    var showEditSheet by rememberSaveable { mutableStateOf(false) }
    var activeEditingEntry by rememberSaveable(stateSaver = JournalEntrySaver) { mutableStateOf<JournalEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntry?>(null) }

    // Recover cropped photo for editing/creating entry
    LaunchedEffect(croppedPath) {
        if (croppedPath != null) {
            val current = activeEditingEntry
            if (current != null) {
                activeEditingEntry = current.copy(imagePath = croppedPath)
            } else {
                activeEditingEntry = JournalEntry(imagePath = croppedPath, quoteText = "", quoteAuthor = "")
                showEditSheet = true
            }
            onConsumeCropResult()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manage Photo Journal", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    activeEditingEntry = JournalEntry(imagePath = "solid:#FF5252", quoteText = "", quoteAuthor = "")
                    showEditSheet = true
                },
                containerColor = Color(0xFFFF6B00),
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Display Settings Section
            val state = appState
            if (state != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Rotation Settings", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Rotation Mode Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf("SEQUENTIAL", "RANDOM", "SCHEDULED")
                            modes.forEach { mode ->
                                val isSelected = state.journalDisplayMode == mode
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFF6B00) else Color(0xFF2A2A2A))
                                        .clickable { viewModel.updateJournalDisplaySettings(mode, state.cycleIntervalMinutes) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = mode,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Cycle Interval Selector
                        if (state.journalDisplayMode != "SCHEDULED") {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Cycle Interval", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val intervals = listOf(1, 5, 15, 30, 60)
                                    intervals.forEach { minutes ->
                                        val isSel = state.cycleIntervalMinutes == minutes
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSel) Color(0xFFFF6B00) else Color(0xFF2A2A2A))
                                                .clickable { viewModel.updateJournalDisplaySettings(state.journalDisplayMode, minutes) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${minutes}m",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Entries List
            if (entries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No journal entries yet. Tap + to add one.", color = Color.White.copy(alpha = 0.5f))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val isActive = state?.activeJournalEntryId == entry.id
                        JournalEntryRow(
                            entry = entry,
                            isActive = isActive,
                            onToggleEnabled = { viewModel.updateJournalEntry(entry.copy(isEnabled = !entry.isEnabled)) },
                            onEdit = {
                                activeEditingEntry = entry
                                showEditSheet = true
                            },
                            onDelete = { entryToDelete = entry },
                            onSelect = { viewModel.setActiveJournalEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (showEditSheet && activeEditingEntry != null) {
        JournalEntryEditSheet(
            entry = activeEditingEntry!!,
            onEntryChanged = { activeEditingEntry = it },
            onDismiss = {
                showEditSheet = false
                activeEditingEntry = null
            },
            onSave = { updated ->
                if (entries.any { it.id == updated.id }) {
                    viewModel.updateJournalEntry(updated)
                } else {
                    viewModel.addJournalEntry(updated)
                }
                showEditSheet = false
                activeEditingEntry = null
            },
            onNavigateToCrop = onNavigateToCrop
        )
    }

    // Delete Confirmation Dialog
    if (entryToDelete != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Journal Entry", color = Color.White) },
            text = { Text("Are you sure you want to delete this entry? This action cannot be undone.", color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        entryToDelete?.let { viewModel.deleteJournalEntry(it) }
                        entryToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { entryToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }
}

@Composable
private fun JournalEntryRow(
    entry: JournalEntry,
    isActive: Boolean,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) Color(0xFFFF6B00) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with 128px maximum constraint downsampling
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                if (entry.imagePath.startsWith("solid:")) {
                    val hex = entry.imagePath.removePrefix("solid:")
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        Color.Red
                    }
                    Box(modifier = Modifier.fillMaxSize().background(color))
                } else {
                    val context = LocalContext.current
                    val resolved = remember(entry.imagePath, context) {
                        if (entry.imagePath.startsWith("/")) entry.imagePath
                        else File(context.filesDir, entry.imagePath).absolutePath
                    }
                    val thumbnailRequest = remember(resolved, context) {
                        coil.request.ImageRequest.Builder(context)
                            .data(resolved)
                            .size(128)
                            .crossfade(true)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .error(android.R.drawable.ic_menu_camera)
                            .placeholder(android.R.drawable.ic_menu_camera)
                            .build()
                    }
                    AsyncImage(
                        model = thumbnailRequest,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.quoteText.isNotEmpty()) "\"${entry.quoteText}\"" else "(No quote text)",
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.quoteAuthor.isNotEmpty()) {
                    Text(
                        text = "— ${entry.quoteAuthor}",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Schedule time if not always active
                if (entry.startTime != null && entry.endTime != null) {
                    Text(
                        text = "Schedule: ${entry.startTime} - ${entry.endTime}",
                        color = Color(0xFFFFD54F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        text = "Always Active",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action switches/buttons
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = entry.isEnabled,
                    onCheckedChange = { onToggleEnabled() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF6B00)
                    )
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color.White.copy(alpha = 0.7f))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalEntryEditSheet(
    entry: JournalEntry,
    onEntryChanged: (JournalEntry) -> Unit,
    onDismiss: () -> Unit,
    onSave: (JournalEntry) -> Unit,
    onNavigateToCrop: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var quoteText by remember { mutableStateOf(entry.quoteText) }
    var quoteAuthor by remember { mutableStateOf(entry.quoteAuthor) }
    var imagePathState by remember { mutableStateOf(entry.imagePath) }

    // Styling States
    var fontSizeSp by remember { mutableStateOf(entry.fontSize) }
    var quoteColorHex by remember { mutableStateOf(entry.colorHex) }
    var quoteIsBold by remember { mutableStateOf(entry.isBold) }
    var quoteIsItalic by remember { mutableStateOf(entry.isItalic) }
    var quoteAlignment by remember { mutableStateOf(entry.alignment) }

    // Time schedule states
    var startHour by remember { mutableStateOf(entry.startTime?.substringBefore(":") ?: "") }
    var startMinute by remember { mutableStateOf(entry.startTime?.substringAfter(":") ?: "") }
    var endHour by remember { mutableStateOf(entry.endTime?.substringBefore(":") ?: "") }
    var endMinute by remember { mutableStateOf(entry.endTime?.substringAfter(":") ?: "") }

    var isEnabledState by remember { mutableStateOf(entry.isEnabled) }
    var displayOrderVal by remember { mutableStateOf(entry.displayOrder.toString()) }

    fun getUpdatedEntry(): JournalEntry {
        val finalStartTime = if (startHour.isNotEmpty() && startMinute.isNotEmpty()) {
            val h = startHour.padStart(2, '0')
            val m = startMinute.padStart(2, '0')
            "$h:$m"
        } else null

        val finalEndTime = if (endHour.isNotEmpty() && endMinute.isNotEmpty()) {
            val h = endHour.padStart(2, '0')
            val m = endMinute.padStart(2, '0')
            "$h:$m"
        } else null

        val order = displayOrderVal.toIntOrNull() ?: 0

        return entry.copy(
            quoteText = quoteText,
            quoteAuthor = quoteAuthor,
            imagePath = imagePathState,
            fontSize = fontSizeSp,
            colorHex = quoteColorHex,
            isBold = quoteIsBold,
            isItalic = quoteIsItalic,
            alignment = quoteAlignment,
            startTime = finalStartTime,
            endTime = finalEndTime,
            isEnabled = isEnabledState,
            displayOrder = order
        )
    }

    var showColorPickerSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // File Capture setup
    val tempPhotoFile = remember { File(context.filesDir, "photos/temp_capture_edit.jpg") }
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
                    }
                }
            }
        }
    )

    val colorsList = listOf(
        "#FFFFFF", "#BDBDBD", "#FFD54F", "#4DB6AC",
        "#FF8A65", "#7E57C2", "#E91E63", "#CDDC39",
        "#FF0000", "#FF5252", "#2196F3", "#000000"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1E1E1E),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (entry.quoteText.isEmpty()) "Add Journal Entry" else "Edit Journal Entry",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            // Background Media Picker
            Text("Background Style", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onEntryChanged(getUpdatedEntry())
                        val hasCameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (hasCameraPermission) {
                            tempPhotoFile.delete()
                            tempPhotoFile.parentFile?.mkdirs()
                            cameraLauncher.launch(tempPhotoUri)
                        } else {
                            Toast.makeText(context, "Please grant camera permission in screen G first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Camera", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Camera", color = Color.White, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        onEntryChanged(getUpdatedEntry())
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery", color = Color.White, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = { showColorPickerSheet = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Palette, contentDescription = "Color Picker", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Color", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            // Preview Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                if (imagePathState.startsWith("solid:")) {
                    val hex = imagePathState.removePrefix("solid:")
                    val color = try {
                        Color(android.graphics.Color.parseColor(hex))
                    } catch (e: Exception) {
                        Color.Red
                    }
                    Box(modifier = Modifier.fillMaxSize().background(color))
                } else {
                    val resPath = if (imagePathState.startsWith("/")) imagePathState
                    else File(context.filesDir, imagePathState).absolutePath
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
                        contentDescription = "Background",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // Overlaid live text preview
                val previewColor = try {
                    Color(android.graphics.Color.parseColor(quoteColorHex))
                } catch (e: Exception) {
                    Color.White
                }
                val previewWeight = if (quoteIsBold) FontWeight.Bold else FontWeight.Normal
                val previewStyle = if (quoteIsItalic) FontStyle.Italic else FontStyle.Normal
                val previewAlign = when (quoteAlignment.uppercase()) {
                    "LEFT" -> TextAlign.Start
                    "RIGHT" -> TextAlign.End
                    else -> TextAlign.Center
                }
                Text(
                    text = if (quoteText.isNotEmpty()) "\"$quoteText\"" else "\"Peace begins with a smile.\"",
                    color = previewColor,
                    fontSize = fontSizeSp.sp,
                    fontWeight = previewWeight,
                    fontStyle = previewStyle,
                    textAlign = previewAlign,
                    modifier = Modifier.fillMaxWidth().padding(12.dp)
                )
            }

            // Input Fields
            OutlinedTextField(
                value = quoteText,
                onValueChange = { if (it.length <= 300) quoteText = it },
                label = { Text("Quote Text (Max 300)", color = Color.White.copy(alpha = 0.6f)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B00),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            OutlinedTextField(
                value = quoteAuthor,
                onValueChange = { if (it.length <= 50) quoteAuthor = it },
                label = { Text("Author (Max 50)", color = Color.White.copy(alpha = 0.6f)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B00),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Typography Config
            Text("Typography & Styling", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Font Size", color = Color.White, fontSize = 14.sp)
                    Text(text = "$fontSizeSp sp", color = Color(0xFFFF6B00), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Slider(
                    value = fontSizeSp.toFloat(),
                    onValueChange = { fontSizeSp = it.toInt() },
                    valueRange = 12f..32f
                )
            }

            Column {
                Text(text = "Text Color", color = Color.White, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 6,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colorsList.forEach { hex ->
                        val isSelected = hex.equals(quoteColorHex, ignoreCase = true)
                        val color = Color(android.graphics.Color.parseColor(hex))
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { quoteColorHex = hex }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconToggleButton(
                        checked = quoteIsBold,
                        onCheckedChange = { quoteIsBold = it }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatBold,
                            contentDescription = "Bold",
                            tint = if (quoteIsBold) Color(0xFFFF6B00) else Color.White
                        )
                    }
                    IconToggleButton(
                        checked = quoteIsItalic,
                        onCheckedChange = { quoteIsItalic = it }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatItalic,
                            contentDescription = "Italic",
                            tint = if (quoteIsItalic) Color(0xFFFF6B00) else Color.White
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { quoteAlignment = "LEFT" }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatAlignLeft,
                            contentDescription = "Align Left",
                            tint = if (quoteAlignment == "LEFT") Color(0xFFFF6B00) else Color.White
                        )
                    }
                    IconButton(onClick = { quoteAlignment = "CENTER" }) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignCenter,
                            contentDescription = "Align Center",
                            tint = if (quoteAlignment == "CENTER") Color(0xFFFF6B00) else Color.White
                        )
                    }
                    IconButton(onClick = { quoteAlignment = "RIGHT" }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.FormatAlignRight,
                            contentDescription = "Align Right",
                            tint = if (quoteAlignment == "RIGHT") Color(0xFFFF6B00) else Color.White
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Time schedule settings using TimePickerDialog
            Text("Scheduling (TimePickerDialog Picker)", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Start Time
                Column(modifier = Modifier.weight(1f)) {
                    Text("Start Time", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable {
                                val currentHour = startHour.toIntOrNull() ?: 8
                                val currentMin = startMinute.toIntOrNull() ?: 0
                                android.app.TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        startHour = h.toString().padStart(2, '0')
                                        startMinute = m.toString().padStart(2, '0')
                                    },
                                    currentHour,
                                    currentMin,
                                    true
                                ).show()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayText = if (startHour.isNotEmpty() && startMinute.isNotEmpty()) {
                                "$startHour:$startMinute"
                            } else {
                                "Always Active"
                            }
                            Text(text = displayText, color = Color.White, fontSize = 14.sp)
                            if (startHour.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Start Time",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            startHour = ""
                                            startMinute = ""
                                        }
                                )
                            }
                        }
                    }
                }

                // End Time
                Column(modifier = Modifier.weight(1f)) {
                    Text("End Time", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable {
                                val currentHour = endHour.toIntOrNull() ?: 17
                                val currentMin = endMinute.toIntOrNull() ?: 0
                                android.app.TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        endHour = h.toString().padStart(2, '0')
                                        endMinute = m.toString().padStart(2, '0')
                                    },
                                    currentHour,
                                    currentMin,
                                    true
                                ).show()
                            }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val displayText = if (endHour.isNotEmpty() && endMinute.isNotEmpty()) {
                                "$endHour:$endMinute"
                            } else {
                                "Always Active"
                            }
                            Text(text = displayText, color = Color.White, fontSize = 14.sp)
                            if (endHour.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear End Time",
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable {
                                            endHour = ""
                                            endMinute = ""
                                        }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            // Display settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Enabled", color = Color.White, fontSize = 14.sp)
                Switch(
                    checked = isEnabledState,
                    onCheckedChange = { isEnabledState = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFFF6B00)
                    )
                )
            }

            OutlinedTextField(
                value = displayOrderVal,
                onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) displayOrderVal = it },
                label = { Text("Display Order", color = Color.White.copy(alpha = 0.6f)) },
                textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF6B00),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(getUpdatedEntry())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
            ) {
                Text("Save Entry", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showColorPickerSheet) {
        SolidColorPickerSheet(
            visible = showColorPickerSheet,
            initialSelectedColorHex = imagePathState,
            onDismiss = { showColorPickerSheet = false },
            onColorSelected = { hex ->
                showColorPickerSheet = false
                imagePathState = hex
            }
        )
    }
}

@Preview
@Composable
fun JournalManagementScreenPreview() {
    JournalManagementScreen(
        onBack = {},
        onNavigateToCrop = {},
        croppedPath = null,
        onConsumeCropResult = {}
    )
}
