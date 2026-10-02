package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.ui.components.AddPartBottomSheet
import com.example.ui.components.ExportDialog
import com.example.ui.components.PartEditorPanel
import com.example.ui.components.Viewport3DCanvas
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.viewmodel.EditorTab
import com.example.viewmodel.EditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    onNavigateBack: () -> Unit
) {
    val project by viewModel.project.collectAsState()
    val selectedPartId by viewModel.selectedPartId.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val showAddPartSheet by viewModel.showAddPartSheet.collectAsState()
    val isSnapToGrid by viewModel.isSnapToGrid.collectAsState()
    val canUndo by viewModel.undoRedoManager.canUndo.collectAsState()
    val canRedo by viewModel.undoRedoManager.canRedo.collectAsState()

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showExportDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showUnitMenu by remember { mutableStateOf(false) }

    BackHandler {
        onNavigateBack()
    }

    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading project...", color = Slate600)
        }
        return
    }

    val currentProject = project!!
    val selectedPart = viewModel.selectedPart

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("button_back_home")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Home",
                            tint = Slate800
                        )
                    }
                },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.testTag("project_title_row")
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.testTag("clickable_rename")
                            ) {
                                Text(
                                    text = currentProject.name,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    ),
                                    maxLines = 1
                                )
                                IconButton(
                                    onClick = { showRenameDialog = true },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Rename",
                                        tint = Slate600,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Unit Selector Dropdown Chip
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Slate100,
                                onClick = { showUnitMenu = true },
                                modifier = Modifier.testTag("button_unit_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentProject.unit.symbol.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Slate800
                                        )
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showUnitMenu,
                                onDismissRequest = { showUnitMenu = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                MeasurementUnit.values().forEach { unitOption ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "${unitOption.label} (${unitOption.symbol})",
                                                    fontWeight = if (unitOption == currentProject.unit) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (unitOption == currentProject.unit) PrimaryBlue else Slate800
                                                )
                                                if (unitOption == currentProject.unit) {
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        },
                                        onClick = {
                                            showUnitMenu = false
                                            viewModel.changeUnit(unitOption)
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = canUndo,
                        modifier = Modifier.testTag("button_undo")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (canUndo) Slate800 else Slate200
                        )
                    }

                    // Redo Button
                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = canRedo,
                        modifier = Modifier.testTag("button_redo")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            tint = if (canRedo) Slate800 else Slate200
                        )
                    }

                    // Export / Share Button
                    IconButton(
                        onClick = { showExportDialog = true },
                        modifier = Modifier.testTag("button_share_export")
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Export & Share",
                            tint = PrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // 4-Tab Bottom Navigation Bar (Assembly, Parts list, Notes, AR Vision)
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == EditorTab.ASSEMBLY,
                    onClick = { viewModel.setActiveTab(EditorTab.ASSEMBLY) },
                    icon = { Icon(Icons.Default.ViewInAr, contentDescription = "Assembly") },
                    label = { Text("Assembly", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = Slate100
                    ),
                    modifier = Modifier.testTag("tab_assembly")
                )
                NavigationBarItem(
                    selected = activeTab == EditorTab.PARTS_LIST,
                    onClick = { viewModel.setActiveTab(EditorTab.PARTS_LIST) },
                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Parts list") },
                    label = { Text("Parts list", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = Slate100
                    ),
                    modifier = Modifier.testTag("tab_parts_list")
                )
                NavigationBarItem(
                    selected = activeTab == EditorTab.NOTES,
                    onClick = { viewModel.setActiveTab(EditorTab.NOTES) },
                    icon = { Icon(Icons.Default.EditNote, contentDescription = "Notes") },
                    label = { Text("Notes", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = Slate100
                    ),
                    modifier = Modifier.testTag("tab_notes")
                )
                NavigationBarItem(
                    selected = activeTab == EditorTab.AR_VISION,
                    onClick = { viewModel.setActiveTab(EditorTab.AR_VISION) },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "AR Vision") },
                    label = { Text("AR Vision", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = Slate100
                    ),
                    modifier = Modifier.testTag("tab_ar_vision")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                EditorTab.ASSEMBLY -> {
                    // 3D Canvas
                    Viewport3DCanvas(
                        parts = currentProject.parts,
                        selectedPartId = selectedPartId,
                        unit = currentProject.unit,
                        isSnapToGrid = isSnapToGrid,
                        onPartSelected = { viewModel.selectPart(it) },
                        onToggleSnap = { viewModel.toggleSnapToGrid() },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Floating "+ Add Part" FAB when in Assembly
                    FloatingActionButton(
                        onClick = { viewModel.setShowAddPartSheet(true) },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = if (selectedPart != null) 240.dp else 24.dp)
                            .testTag("fab_add_part")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Part", modifier = Modifier.size(28.dp))
                    }

                    // Bottom Object Editing Controls when a part is selected
                    AnimatedVisibility(
                        visible = selectedPart != null,
                        enter = slideInVertically(initialOffsetY = { it }),
                        exit = slideOutVertically(targetOffsetY = { it }),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        selectedPart?.let { part ->
                            PartEditorPanel(
                                part = part,
                                unit = currentProject.unit,
                                onDimensionsChange = { w, h, t ->
                                    viewModel.updateSelectedPartDimensions(w, h, t)
                                },
                                onPositionChange = { x, y, z ->
                                    viewModel.updateSelectedPartPosition(x, y, z)
                                },
                                onRotationChange = { rx, ry, rz ->
                                    viewModel.updateSelectedPartRotation(rx, ry, rz)
                                },
                                onMaterialChange = { mat, color ->
                                    viewModel.updateSelectedPartMaterial(mat, color)
                                },
                                onDuplicate = { viewModel.duplicateSelectedPart() },
                                onDelete = { viewModel.deleteSelectedPart() },
                                onClose = { viewModel.selectPart(null) }
                            )
                        }
                    }
                }

                EditorTab.PARTS_LIST -> {
                    PartsListTab(
                        project = currentProject,
                        onPartSelect = { partId ->
                            viewModel.selectPart(partId)
                            viewModel.setActiveTab(EditorTab.ASSEMBLY)
                        }
                    )
                }

                EditorTab.NOTES -> {
                    NotesTab(
                        notes = currentProject.notes,
                        onNotesChange = { viewModel.updateNotes(it) }
                    )
                }

                EditorTab.AR_VISION -> {
                    ArVisionTab(project = currentProject)
                }
            }
        }
    }

    // Add Part Modal Bottom Sheet
    if (showAddPartSheet) {
        AddPartBottomSheet(
            sheetState = addSheetState,
            unit = currentProject.unit,
            defaultThickness = currentProject.defaultThickness,
            onDismiss = { viewModel.setShowAddPartSheet(false) },
            onAddPart = { type, w, h, t, mat ->
                viewModel.addPart(
                    type = type,
                    customWidth = w,
                    customHeight = h,
                    customThickness = t,
                    customMaterial = mat
                )
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            project = currentProject,
            onDismiss = { showExportDialog = false }
        )
    }

    // Rename Project Dialog
    if (showRenameDialog) {
        var tempName by remember { mutableStateOf(currentProject.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Project", fontWeight = FontWeight.Bold, color = Slate800) },
            text = {
                OutlinedTextField(
                    value = tempName,
                    onValueChange = { tempName = it },
                    label = { Text("Project Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_rename_project")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateProjectName(tempName)
                        showRenameDialog = false
                    }
                ) {
                    Text("Save", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Slate600)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
