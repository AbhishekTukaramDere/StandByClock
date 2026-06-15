package com.yourcompany.standby.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SolidColorPickerSheet(
    visible: Boolean,
    initialSelectedColorHex: String?,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        val colors = listOf(
            "#FF0000", "#FF5252", "#FF6B00", "#FFD54F",
            "#4CAF50", "#8FA68E", "#4DB6AC", "#2196F3",
            "#7E57C2", "#E91E63", "#1A1A1A", "#000000"
        )
        
        var selectedColorHex by remember { 
            mutableStateOf(
                if (initialSelectedColorHex?.startsWith("solid:") == true) {
                    initialSelectedColorHex.removePrefix("solid:")
                } else {
                    "#FF0000" // Fresh Red Default
                }
            ) 
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
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Select Background Color",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Grid of 12 colors: 3 rows x 4 columns
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    maxItemsInEachRow = 4,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    colors.forEach { hex ->
                        val isSelected = selectedColorHex.equals(hex, ignoreCase = true)
                        val color = Color(android.graphics.Color.parseColor(hex))
                        
                        val borderWidth by animateDpAsState(
                            targetValue = if (isSelected) 3.dp else 0.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "BorderWidth"
                        )

                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = borderWidth,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onColorSelected("solid:$selectedColorHex")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B00))
                ) {
                    Text("Apply", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview
@Composable
fun SolidColorPickerSheetPreview() {
    SolidColorPickerSheet(
        visible = true,
        initialSelectedColorHex = "solid:#FF0000",
        onDismiss = {},
        onColorSelected = {}
    )
}
