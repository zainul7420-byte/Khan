package com.example.ui.components

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FurnitureProject
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.utils.CutListGenerator
import com.example.utils.FileSharingUtil
import com.example.utils.ObjExporter
import com.example.utils.StlExporter

@Composable
fun ExportDialog(
    project: FurnitureProject,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Export & Share Design",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate800
                )
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExportOptionTile(
                    icon = Icons.Default.ViewInAr,
                    title = "Export Wavefront OBJ",
                    subtitle = "Standard 3D mesh with materials for Blender, CAD, Fusion 360",
                    onClick = {
                        val obj = ObjExporter.exportToObj(project)
                        FileSharingUtil.shareExportFile(context, "${project.name}.obj", obj, "text/plain")
                        onDismiss()
                    }
                )

                ExportOptionTile(
                    icon = Icons.Default.Print,
                    title = "Export STL (3D Printing / CNC)",
                    subtitle = "Standard stereolithography solid model for slicers",
                    onClick = {
                        val stl = StlExporter.exportToStl(project)
                        FileSharingUtil.shareExportFile(context, "${project.name}.stl", stl, "application/sla")
                        onDismiss()
                    }
                )

                ExportOptionTile(
                    icon = Icons.Default.Description,
                    title = "Export Wood Cut List",
                    subtitle = "Cutting specs, dimensions, lumber count and sheet estimation",
                    onClick = {
                        val text = CutListGenerator.exportToText(project)
                        FileSharingUtil.shareText(context, "${project.name} Cut List", text)
                        onDismiss()
                    }
                )

                ExportOptionTile(
                    icon = Icons.Default.Share,
                    title = "Share Project Specs",
                    subtitle = "Share full design summary and part specifications",
                    onClick = {
                        val summary = buildString {
                            append("WoodCraft 3D Project: ${project.name}\n")
                            append("Parts: ${project.totalPartsCount} boards\n")
                            append("Surface Area: ${String.format("%.2f", project.totalAreaM2)} m²\n\n")
                            append(CutListGenerator.exportToText(project))
                        }
                        FileSharingUtil.shareText(context, "WoodCraft 3D - ${project.name}", summary)
                        onDismiss()
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = PrimaryBlue)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.testTag("export_dialog")
    )
}

@Composable
private fun ExportOptionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Slate100,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Slate800
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Slate600,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}
