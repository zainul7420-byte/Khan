package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chair
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit
import com.example.threeD.CameraPreset
import com.example.threeD.Scene3D
import com.example.ui.components.ExportDialog
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.WoodOak
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    projects: List<FurnitureProject>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOpenProject: (String) -> Unit,
    onCreateProject: (name: String, unit: MeasurementUnit, thickness: Float) -> Unit,
    onDuplicateProject: (FurnitureProject) -> Unit,
    onDeleteProject: (String) -> Unit
) {
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var projectToExport by remember { mutableStateOf<FurnitureProject?>(null) }
    var projectToDelete by remember { mutableStateOf<FurnitureProject?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Search creations...", fontSize = 14.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = Slate200
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(onClick = {
                                    onSearchQueryChange("")
                                    isSearchExpanded = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Search")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("search_text_field")
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PrimaryBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewInAr,
                                    contentDescription = "WoodCraft 3D Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "WoodCraft 3D",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                )
                                Text(
                                    text = "Furniture & Woodworking Studio",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = Slate600,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { showAboutDialog = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Slate800)
                        }
                    }
                },
                actions = {
                    if (!isSearchExpanded) {
                        IconButton(onClick = { isSearchExpanded = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Slate800)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("New project", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("fab_new_project")
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Screen Title: "Your creations"
            item {
                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)) {
                    Text(
                        text = "Your creations",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800
                        )
                    )
                    Text(
                        text = "${projects.size} 3D design${if (projects.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            }

            // Projects List
            if (projects.isEmpty()) {
                item {
                    EmptyProjectsCard(onNewProject = { showNewProjectDialog = true })
                }
            } else {
                items(projects) { project ->
                    ProjectCard(
                        project = project,
                        onClick = { onOpenProject(project.id) },
                        onDuplicate = { onDuplicateProject(project) },
                        onExport = { projectToExport = project },
                        onDelete = { projectToDelete = project }
                    )
                }
            }

            // Examples Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "WORKSHOP EXAMPLES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate600,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        ExampleFurnitureCard(
                            title = "Computer Desk",
                            subtitle = "135 × 60 cm Ergonomic Desk",
                            tags = "Light Oak • 4 parts",
                            onClick = {
                                val desk = projects.find { it.name.contains("Desk", ignoreCase = true) }
                                if (desk != null) onOpenProject(desk.id) else showNewProjectDialog = true
                            }
                        )
                    }
                    item {
                        ExampleFurnitureCard(
                            title = "Record Player",
                            subtitle = "Mid-century Audio Console",
                            tags = "Walnut • Vinyl bays",
                            onClick = {
                                val console = projects.find { it.name.contains("Record", ignoreCase = true) }
                                if (console != null) onOpenProject(console.id) else showNewProjectDialog = true
                            }
                        )
                    }
                    item {
                        ExampleFurnitureCard(
                            title = "Workshop",
                            subtitle = "Heavy Duty Timber Workbench",
                            tags = "Pine • Tool shelf",
                            onClick = {
                                val bench = projects.find { it.name.contains("Workbench", ignoreCase = true) }
                                if (bench != null) onOpenProject(bench.id) else showNewProjectDialog = true
                            }
                        )
                    }
                    item {
                        ExampleFurnitureCard(
                            title = "Modern Bookshelf",
                            subtitle = "Nordic Tiered Shelf",
                            tags = "Dark Oak • 6 parts",
                            onClick = {
                                val shelf = projects.find { it.name.contains("Bookshelf", ignoreCase = true) }
                                if (shelf != null) onOpenProject(shelf.id) else showNewProjectDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showNewProjectDialog) {
        NewProjectDialog(
            onDismiss = { showNewProjectDialog = false },
            onConfirm = { name, unit, thickness ->
                showNewProjectDialog = false
                onCreateProject(name, unit, thickness)
            }
        )
    }

    // Export Dialog
    projectToExport?.let { project ->
        ExportDialog(
            project = project,
            onDismiss = { projectToExport = null }
        )
    }

    // Delete Confirmation Dialog
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text("Delete Project?", fontWeight = FontWeight.Bold, color = Slate800) },
            text = { Text("Are you sure you want to delete '${project.name}'? This cannot be undone.", color = Slate600) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteProject(project.id)
                        projectToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = Slate600)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("WoodCraft 3D Studio", fontWeight = FontWeight.Bold, color = Slate800)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Original 3D woodworking and furniture design software created with Kotlin and Jetpack Compose.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Slate700)
                    )
                    Text(
                        text = "• Real-time 3D board assembly & orbit camera\n• Accurate centimeter & inch measurements\n• Automatic cut list generator\n• CAD STL & Wavefront OBJ export\n• Local persistent storage with Room\n• Augmented Reality room vision",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate600)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Close", color = PrimaryBlue)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun ProjectCard(
    project: FurnitureProject,
    onClick: () -> Unit,
    onDuplicate: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(project.updatedAt) { dateFormatter.format(Date(project.updatedAt)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Live 3D Miniature Thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate100),
                contentAlignment = Alignment.Center
            ) {
                val previewScene = remember(project.parts) {
                    Scene3D().apply {
                        distance = 230f
                        pitch = 25f
                        yaw = 35f
                    }
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    previewScene.render(
                        drawScope = this,
                        parts = project.parts,
                        selectedPartId = null,
                        showGrid = false,
                        showDimensions = false,
                        unit = project.unit
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Project Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${project.totalPartsCount} parts • ${project.unit.symbol.uppercase()}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Modified $formattedDate",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate600,
                        fontSize = 11.sp
                    )
                )
            }

            // Three-dot Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.testTag("button_project_menu")
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Project Options",
                        tint = Slate600
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = {
                            showMenu = false
                            onDuplicate()
                        },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = PrimaryBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Export & Share") },
                        onClick = {
                            showMenu = false
                            onExport()
                        },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = PrimaryBlue) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ExampleFurnitureCard(
    title: String,
    subtitle: String,
    tags: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(WoodOak.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chair,
                        contentDescription = null,
                        tint = WoodOak,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Slate100
                ) {
                    Text(
                        text = "Example",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            color = Slate600
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate600,
                        fontSize = 11.sp
                    )
                )
                Text(
                    text = tags,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyProjectsCard(onNewProject: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ViewInAr,
                contentDescription = null,
                tint = Slate600,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "No custom designs yet",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
            )
            Text(
                text = "Create a custom desk, shelf or cabinet with 3D wooden boards.",
                style = MaterialTheme.typography.bodySmall.copy(color = Slate600),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            TextButton(onClick = onNewProject) {
                Text("+ Create New Project", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}
