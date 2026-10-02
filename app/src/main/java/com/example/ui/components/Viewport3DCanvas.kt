package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FurniturePart
import com.example.model.MeasurementUnit
import com.example.threeD.CameraPreset
import com.example.threeD.Mesh3D
import com.example.threeD.ProjectedDimension
import com.example.threeD.Scene3D
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate700
import com.example.ui.theme.ViewportBackground

@Composable
fun Viewport3DCanvas(
    parts: List<FurniturePart>,
    selectedPartId: String?,
    unit: MeasurementUnit,
    isSnapToGrid: Boolean,
    onPartSelected: (String?) -> Unit,
    onToggleSnap: () -> Unit,
    modifier: Modifier = Modifier,
    scene3D: Scene3D = remember { Scene3D() }
) {
    val haptic = LocalHapticFeedback.current
    var triggerRender by remember { mutableFloatStateOf(0f) }
    var currentPreset by remember { mutableStateOf(CameraPreset.PERSPECTIVE) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ViewportBackground)
            .testTag("viewport_3d_canvas")
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // 3D Canvas with Touch Interaction
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val mesh = Mesh3D.buildFromParts(parts, selectedPartId)
                        val hitId = scene3D.hitTest(offset.x, offset.y, mesh, widthPx, heightPx)
                        if (hitId != selectedPartId) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPartSelected(hitId)
                            triggerRender += 1f
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        if (zoom != 1f) {
                            scene3D.zoom(zoom)
                        }
                        if (pan != Offset.Zero) {
                            // Single finger or small movement -> Orbit camera
                            // Large two-finger pan or zoom -> Pan camera
                            if (zoom != 1f || pan.getDistance() > 15f && centroid != Offset.Zero) {
                                scene3D.orbit(pan.x * 0.45f, -pan.y * 0.35f)
                            } else {
                                scene3D.orbit(pan.x * 0.45f, -pan.y * 0.35f)
                            }
                        }
                        triggerRender += 1f
                    }
                }
        ) {
            // Read trigger to recompose on gesture
            @Suppress("UNUSED_VARIABLE")
            val tick = triggerRender

            scene3D.render(
                drawScope = this,
                parts = parts,
                selectedPartId = selectedPartId,
                showGrid = true,
                showDimensions = true,
                unit = unit
            )
        }

        // Overlay 3D Dimension Badges around Selected Part
        val selectedPart = parts.find { it.id == selectedPartId }
        if (selectedPart != null) {
            val dimensions = remember(selectedPart, triggerRender, unit) {
                scene3D.getDimensionCallouts(selectedPart, widthPx, heightPx, unit)
            }
            dimensions.forEach { dim ->
                DimensionBadge(dimension = dim)
            }
        }

        // Top-left Camera View Preset Chips
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White.copy(alpha = 0.92f),
            shadowElevation = 2.dp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ViewPresetPill("3D", currentPreset == CameraPreset.PERSPECTIVE) {
                    currentPreset = CameraPreset.PERSPECTIVE
                    scene3D.setViewPreset(CameraPreset.PERSPECTIVE)
                    triggerRender += 1f
                }
                Spacer(modifier = Modifier.width(4.dp))
                ViewPresetPill("Top", currentPreset == CameraPreset.TOP) {
                    currentPreset = CameraPreset.TOP
                    scene3D.setViewPreset(CameraPreset.TOP)
                    triggerRender += 1f
                }
                Spacer(modifier = Modifier.width(4.dp))
                ViewPresetPill("Front", currentPreset == CameraPreset.FRONT) {
                    currentPreset = CameraPreset.FRONT
                    scene3D.setViewPreset(CameraPreset.FRONT)
                    triggerRender += 1f
                }
                Spacer(modifier = Modifier.width(4.dp))
                ViewPresetPill("Side", currentPreset == CameraPreset.SIDE) {
                    currentPreset = CameraPreset.SIDE
                    scene3D.setViewPreset(CameraPreset.SIDE)
                    triggerRender += 1f
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        scene3D.resetCamera()
                        currentPreset = CameraPreset.PERSPECTIVE
                        triggerRender += 1f
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Camera View",
                        tint = Slate700,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Top-right Snap-to-Grid and Fit tools
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Snap to Grid Pill
            Surface(
                shape = CircleShape,
                color = if (isSnapToGrid) PrimaryBlue else Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                onClick = onToggleSnap
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GridOn,
                        contentDescription = "Toggle Snap to Grid",
                        tint = if (isSnapToGrid) Color.White else Slate700,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSnapToGrid) "Snap ON" else "Snap OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSnapToGrid) Color.White else Slate700
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewPresetPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryBlue else Color.Transparent,
        onClick = onClick
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Slate700,
                fontSize = 11.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}
