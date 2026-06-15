package com.yourcompany.standby.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoUploadSheet(
    visible: Boolean,
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onTakePhoto: () -> Unit,
    onChooseGallery: () -> Unit,
    onChooseColor: () -> Unit,
    onCropCurrent: () -> Unit,
    onRemovePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color(0xFF1E1E1E),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Text(
                    text = "Photo Options",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                PhotoOptionItem(
                    text = "Take Photo",
                    icon = Icons.Default.CameraAlt,
                    onClick = onTakePhoto
                )
                PhotoOptionItem(
                    text = "Choose from Gallery",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = onChooseGallery
                )
                PhotoOptionItem(
                    text = "Solid Color",
                    icon = Icons.Default.Palette,
                    onClick = onChooseColor
                )

                if (hasPhoto) {
                    PhotoOptionItem(
                        text = "Crop Current",
                        icon = Icons.Default.Crop,
                        onClick = onCropCurrent
                    )
                    PhotoOptionItem(
                        text = "Remove Background",
                        icon = Icons.Default.Delete,
                        textColor = Color.Red,
                        iconColor = Color.Red,
                        onClick = onRemovePhoto
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))

                PhotoOptionItem(
                    text = "Cancel",
                    icon = Icons.Default.Close,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
private fun PhotoOptionItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    textColor: Color = Color.White,
    iconColor: Color = Color.White.copy(alpha = 0.6f)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

@Preview
@Composable
fun PhotoUploadSheetPreview() {
    PhotoUploadSheet(
        visible = true,
        hasPhoto = true,
        onDismiss = {},
        onTakePhoto = {},
        onChooseGallery = {},
        onChooseColor = {},
        onCropCurrent = {},
        onRemovePhoto = {}
    )
}
