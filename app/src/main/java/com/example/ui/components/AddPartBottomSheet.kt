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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.CropLandscape
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewSidebar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MeasurementUnit
import com.example.model.PartType
import com.example.model.WoodMaterial
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPartBottomSheet(
    sheetState: SheetState,
    unit: MeasurementUnit,
    defaultThickness: Float,
    onDismiss: () -> Unit,
    onAddPart: (type: PartType, width: Float, height: Float, thickness: Float, material: WoodMaterial) -> Unit
) {
    var selectedType by remember { mutableStateOf(PartType.BOARD) }
    var selectedMaterial by remember { mutableStateOf(WoodMaterial.LIGHT_OAK) }

    // Dimensions state in current unit
    var widthInput by remember(selectedType, unit) {
        mutableStateOf(String.format(java.util.Locale.US, "%.1f", unit.fromCm(selectedType.defaultWidth)))
    }
    var heightInput by remember(selectedType, unit) {
        mutableStateOf(String.format(java.util.Locale.US, "%.1f", unit.fromCm(selectedType.defaultHeight)))
    }
    var thicknessInput by remember(selectedType, unit) {
        mutableStateOf(String.format(java.util.Locale.US, "%.1f", unit.fromCm(defaultThickness)))
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("add_part_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add 3D Part",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "Unit: ${unit.symbol}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Slate600
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Part Types Grid (Board, Rounded board, Rounded front, Hole, Cutout, Drawer, Door, Shelf, Leg)
            Text(
                text = "SELECT COMPONENT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(PartType.values()) { type ->
                    PartTypeCard(
                        type = type,
                        isSelected = type == selectedType,
                        onClick = {
                            selectedType = type
                            widthInput = String.format(java.util.Locale.US, "%.1f", unit.fromCm(type.defaultWidth))
                            heightInput = String.format(java.util.Locale.US, "%.1f", unit.fromCm(type.defaultHeight))
                            thicknessInput = String.format(java.util.Locale.US, "%.1f", unit.fromCm(type.defaultThickness))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Numeric Dimension Inputs
            Text(
                text = "DIMENSIONS (${unit.symbol.uppercase()})",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = widthInput,
                    onValueChange = { widthInput = it },
                    label = { Text("Width") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_part_width")
                )
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text("Height/Depth") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_part_height")
                )
                OutlinedTextField(
                    value = thicknessInput,
                    onValueChange = { thicknessInput = it },
                    label = { Text("Thickness") },
                    singleLine = true,
                    modifier = Modifier.weight(1f).testTag("input_part_thickness")
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wood Material Selector
            Text(
                text = "WOOD MATERIAL",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(WoodMaterial.values()) { material ->
                    MaterialChip(
                        material = material,
                        isSelected = material == selectedMaterial,
                        onClick = { selectedMaterial = material }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Add Button
            Button(
                onClick = {
                    val wCm = unit.parseToCm(widthInput.toFloatOrNull() ?: selectedType.defaultWidth)
                    val hCm = unit.parseToCm(heightInput.toFloatOrNull() ?: selectedType.defaultHeight)
                    val tCm = unit.parseToCm(thicknessInput.toFloatOrNull() ?: defaultThickness)
                    onAddPart(selectedType, wCm, hCm, tCm, selectedMaterial)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_confirm_add_part"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add ${selectedType.title} to 3D Scene",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PartTypeCard(
    type: PartType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Slate100 else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryBlue else Slate200
        ),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PrimaryBlue.copy(alpha = 0.15f) else Slate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPartTypeIcon(type),
                    contentDescription = type.title,
                    tint = if (isSelected) PrimaryBlue else Slate600,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = type.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryBlue else Slate800
                    )
                )
                Text(
                    text = "${type.defaultWidth.toInt()}×${type.defaultHeight.toInt()} cm",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate600,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun MaterialChip(
    material: WoodMaterial,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Slate100 else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) PrimaryBlue else Slate200
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(material.primaryColor))
                    .border(1.dp, Color(0x30000000), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = material.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryBlue else Slate800
                )
            )
        }
    }
}

private fun getPartTypeIcon(type: PartType): ImageVector {
    return when (type) {
        PartType.BOARD -> Icons.Default.CropLandscape
        PartType.ROUNDED_BOARD -> Icons.Default.RoundedCorner
        PartType.ROUNDED_FRONT -> Icons.Default.ViewAgenda
        PartType.SHELF -> Icons.Default.TableRows
        PartType.DRAWER -> Icons.Default.Inbox
        PartType.DOOR -> Icons.Default.MeetingRoom
        PartType.LEG -> Icons.Default.ViewSidebar
        PartType.HOLE_CIRCULAR -> Icons.Default.RadioButtonUnchecked
        PartType.HOLE_RECTANGULAR -> Icons.Default.CropSquare
        PartType.CUTOUT_RECTANGULAR -> Icons.Default.ContentCut
    }
}
