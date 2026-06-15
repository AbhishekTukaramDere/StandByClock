package com.yourcompany.standby.ui.components

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileOutputStream

val UploadIcon = ImageVector.Builder(
    name = "Upload",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
        moveTo(9f, 16f)
        horizontalLineTo(15f)
        verticalLineTo(10f)
        horizontalLineTo(19f)
        lineTo(12f, 3f)
        lineTo(5f, 10f)
        horizontalLineTo(9f)
        verticalLineTo(16f)
        close()
        moveTo(5f, 18f)
        horizontalLineTo(19f)
        verticalLineTo(20f)
        horizontalLineTo(5f)
        verticalLineTo(18f)
        close()
    }
}.build()

val DownloadIcon = ImageVector.Builder(
    name = "Download",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply {
    path(fill = androidx.compose.ui.graphics.SolidColor(Color.White)) {
        moveTo(19f, 9f)
        horizontalLineTo(15f)
        verticalLineTo(3f)
        horizontalLineTo(9f)
        verticalLineTo(9f)
        horizontalLineTo(5f)
        lineTo(12f, 16f)
        lineTo(19f, 9f)
        close()
        moveTo(5f, 18f)
        horizontalLineTo(19f)
        verticalLineTo(20f)
        horizontalLineTo(5f)
        verticalLineTo(18f)
        close()
    }
}.build()

private fun exportQuotesToDownloads(context: Context, quotes: List<String>) {
    if (quotes.isEmpty()) {
        Toast.makeText(context, "No custom quotes to export.", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val jsonObject = org.json.JSONObject().apply {
            val jsonArray = org.json.JSONArray()
            quotes.forEach { jsonArray.put(it) }
            put("quotes", jsonArray)
        }
        val fileName = "custom_quotes_${System.currentTimeMillis()}.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StandByClock")
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.bufferedWriter().use { it.write(jsonObject.toString(4)) }
                }
                Toast.makeText(context, "Exported: Downloads/StandByClock/$fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Failed to create file via MediaStore.", Toast.LENGTH_SHORT).show()
            }
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val appDir = File(downloadsDir, "StandByClock")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val targetFile = File(appDir, fileName)
            FileOutputStream(targetFile).use { outputStream ->
                outputStream.bufferedWriter().use { it.write(jsonObject.toString(4)) }
            }
            Toast.makeText(context, "Exported: Downloads/StandByClock/$fileName", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Export failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomQuotesManagerSheet(
    visible: Boolean,
    customQuotes: List<String>,
    onDismiss: () -> Unit,
    onAddQuote: (String) -> Unit,
    onDeleteQuote: (String) -> Unit,
    onImportQuotes: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        var newQuoteText by remember { mutableStateOf("") }
        val context = LocalContext.current
        val jsonPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                try {
                    val contentResolver = context.contentResolver
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonText = inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = org.json.JSONObject(jsonText)
                        val jsonArray = jsonObject.getJSONArray("quotes")
                        val importedList = mutableListOf<String>()
                        for (i in 0 until jsonArray.length()) {
                            val q = jsonArray.getString(i)
                            if (q.trim().isNotEmpty()) {
                                importedList.add(q.trim())
                            }
                        }
                        if (importedList.isNotEmpty()) {
                            onImportQuotes(importedList)
                        } else {
                            Toast.makeText(
                                context,
                                "No valid quotes found in JSON.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Failed to import JSON: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1E1E1E),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Manage Custom Quotes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Add Quote Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newQuoteText,
                        onValueChange = { if (it.length <= 300) newQuoteText = it },
                        placeholder = { Text("Enter a new custom quote...", color = Color.White.copy(alpha = 0.5f)) },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White),
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFFF6B00),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f)
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (newQuoteText.trim().isNotEmpty()) {
                                if (customQuotes.size >= 500) {
                                    Toast.makeText(context, "Limit reached (Max 500 quotes)", Toast.LENGTH_SHORT).show()
                                } else {
                                    onAddQuote(newQuoteText.trim())
                                    newQuoteText = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }

                // Import / Export JSON Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Import JSON Button
                    Button(
                        onClick = { jsonPickerLauncher.launch("application/json") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = UploadIcon,
                            contentDescription = "Import JSON File",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Import JSON",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Export JSON Button
                    Button(
                        onClick = { exportQuotesToDownloads(context, customQuotes) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = DownloadIcon,
                            contentDescription = "Export JSON File",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Export JSON",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                // Scrollable list of quotes
                Text(
                    text = "Your Custom Quotes (${customQuotes.size} / 500)",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                if (customQuotes.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No custom quotes yet.",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        customQuotes.forEach { quote ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "\"$quote\"",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = { onDeleteQuote(quote) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CustomQuotesManagerSheetPreview() {
    CustomQuotesManagerSheet(
        visible = true,
        customQuotes = listOf(
            "The only way to do great work is to love what you do.",
            "Stay hungry, stay foolish."
        ),
        onDismiss = {},
        onAddQuote = {},
        onDeleteQuote = {},
        onImportQuotes = {}
    )
}
