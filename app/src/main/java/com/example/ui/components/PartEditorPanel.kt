package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FurniturePart
import com.example.model.MeasurementUnit
import com.example.model.WoodMaterial
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun PartEditorPanel(
    part: FurniturePart,
    unit: MeasurementUnit,
    onDimensionsChange: (width: Float?, height: Float?, thickness: Float?) -> Unit,
    onPositionChange: (x: Float?, y: Float?, z: Float?) -> Unit,
    onRotationChange: (rx: Float?, ry: Float?, rz: Float?) -> Unit,
    onMaterialChange: (WoodMaterial, Long?) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Dimensions", "Position", "Rotation", "Material")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("part_editor_panel"),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Title & Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = part.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    )
                    Text(
                        text = "${part.type.title} • ${part.formattedDimensions(unit)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDuplicate,
                        modifier = Modifier.size(36.dp).testTag("button_duplicate_part")
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Part",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp).testTag("button_delete_part")
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Part",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(36.dp).testTag("button_close_panel")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close Panel",
                            tint = Slate600,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Sub-navigation Tabs
            TabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = Color.Transparent,
                contentColor = PrimaryBlue,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSubTab == index,
                        onClick = { selectedSubTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                when (selectedSubTab) {
                    0 -> DimensionsEditor(part, unit, onDimensionsChange)
                    1 -> PositionEditor(part, unit, onPositionChange)
                    2 -> RotationEditor(part, onRotationChange)
                    3 -> MaterialEditor(part, onMaterialChange)
                }
            }
        }
    }
}

@Composable
private fun DimensionsEditor(
    part: FurniturePart,
    unit: MeasurementUnit,
    onChange: (Float?, Float?, Float?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DimensionRow(
            label = "Width",
            valueCm = part.width,
            unit = unit,
            range = 5f..250f,
            step = 1f,
            onValueChange = { onChange(it, null, null) }
        )
        DimensionRow(
            label = "Height",
            valueCm = part.height,
            unit = unit,
            range = 1f..250f,
            step = 1f,
            onValueChange = { onChange(null, it, null) }
        )
        DimensionRow(
            label = "Thickness",
            valueCm = part.thickness,
            unit = unit,
            range = 0.5f..100f,
            step = 0.2f,
            onValueChange = { onChange(null, null, it) }
        )
    }
}

@Composable
private fun DimensionRow(
    label: String,
    valueCm: Float,
    unit: MeasurementUnit,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${unit.format(valueCm)}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            ),
            modifier = Modifier.width(110.dp)
        )

        IconButton(
            onClick = { onValueChange((valueCm - step).coerceIn(range.start, range.endInclusive)) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate700, modifier = Modifier.size(16.dp))
        }

        Slider(
            value = valueCm.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = PrimaryBlue,
                activeTrackColor = PrimaryBlue
            )
        )

        IconButton(
            onClick = { onValueChange((valueCm + step).coerceIn(range.start, range.endInclusive)) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Slate700, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun PositionEditor(
    part: FurniturePart,
    unit: MeasurementUnit,
    onChange: (Float?, Float?, Float?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PositionRow("X (Left/Right)", part.positionX, unit, -120f..120f, 1f) { onChange(it, null, null) }
        PositionRow("Y (Height)", part.positionY, unit, 0f..200f, 1f) { onChange(null, it, null) }
        PositionRow("Z (Front/Back)", part.positionZ, unit, -120f..120f, 1f) { onChange(null, null, it) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = { onChange(0f, null, 0f) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.OpenWith, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Center (X, Z)", fontSize = 11.sp)
            }
            FilledTonalButton(
                onClick = { onChange(null, part.height * 0.5f, null) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Place on Floor", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PositionRow(
    label: String,
    valueCm: Float,
    unit: MeasurementUnit,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${unit.format(valueCm)}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            ),
            modifier = Modifier.width(130.dp)
        )

        IconButton(
            onClick = { onValueChange(valueCm - step) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Slate700, modifier = Modifier.size(16.dp))
        }

        Slider(
            value = valueCm.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
        )

        IconButton(
            onClick = { onValueChange(valueCm + step) },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Slate700, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun RotationEditor(
    part: FurniturePart,
    onChange: (Float?, Float?, Float?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RotationRow("Rotate X", part.rotationX) { onChange(it, null, null) }
        RotationRow("Rotate Y", part.rotationY) { onChange(null, it, null) }
        RotationRow("Rotate Z", part.rotationZ) { onChange(null, null, it) }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { onChange(null, (part.rotationY + 90f) % 360f, null) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Rotate90DegreesCcw, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+90° Y", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = { onChange(0f, 0f, 0f) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset Angle", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RotationRow(
    label: String,
    degrees: Float,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: ${degrees.toInt()}°",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = Slate700
            ),
            modifier = Modifier.width(110.dp)
        )

        Slider(
            value = (degrees % 360f).coerceIn(0f, 360f),
            onValueChange = onValueChange,
            valueRange = 0f..360f,
            steps = 23, // 15 degree increments
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = PrimaryBlue, activeTrackColor = PrimaryBlue)
        )
    }
}

@Composable
private fun MaterialEditor(
    part: FurniturePart,
    onChange: (WoodMaterial, Long?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Material: ${part.material.displayName} — ${part.material.description}",
            style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(WoodMaterial.values()) { mat ->
                val isSelected = mat == part.material
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) Slate100 else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) PrimaryBlue else Slate300
                    ),
                    onClick = { onChange(mat, null) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(mat.primaryColor))
                                .border(1.dp, Color(0x30000000), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = mat.displayName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryBlue else Slate800
                            )
                        )
                    }
                }
            }
        }
    }
}
