package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MeasurementUnit
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

@Composable
fun NewProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, unit: MeasurementUnit, thickness: Float) -> Unit
) {
    var projectName by remember { mutableStateOf("New project") }
    var selectedUnit by remember { mutableStateOf(MeasurementUnit.CM) }
    var thicknessText by remember { mutableStateOf("1.8") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Create,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Create New Project",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Project Name Field
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_project_name")
                )

                // Measurement Unit Selector
                Column {
                    Text(
                        text = "MEASUREMENT UNIT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate600,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MeasurementUnit.values().forEach { unit ->
                            FilterChip(
                                selected = unit == selectedUnit,
                                onClick = {
                                    selectedUnit = unit
                                    thicknessText = when (unit) {
                                        MeasurementUnit.CM -> "1.8"
                                        MeasurementUnit.MM -> "18"
                                        MeasurementUnit.INCH -> "0.75"
                                    }
                                },
                                label = {
                                    Text(
                                        text = "${unit.label} (${unit.symbol})",
                                        fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Default Board Thickness
                OutlinedTextField(
                    value = thicknessText,
                    onValueChange = { thicknessText = it },
                    label = { Text("Default Board Thickness (${selectedUnit.symbol})") },
                    supportingText = { Text("Standard cabinet/furniture panel thickness") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("input_new_project_thickness")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rawVal = thicknessText.toFloatOrNull() ?: 1.8f
                    val thicknessCm = selectedUnit.parseToCm(rawVal)
                    onConfirm(projectName, selectedUnit, thicknessCm)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("button_confirm_create_project")
            ) {
                Text("Start Designing", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate600)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("new_project_dialog")
    )
}
