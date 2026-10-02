package com.example.utils

import com.example.model.FurniturePart
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit
import com.example.model.WoodMaterial

data class CutListItem(
    val title: String,
    val width: Float,
    val height: Float,
    val thickness: Float,
    val quantity: Int,
    val material: WoodMaterial,
    val partIds: List<String>
) {
    fun formattedDimensions(unit: MeasurementUnit): String {
        val w = unit.fromCm(width)
        val h = unit.fromCm(height)
        val t = unit.fromCm(thickness)
        return when (unit) {
            MeasurementUnit.MM -> String.format("%.0f × %.0f × %.1f %s", w, h, t, unit.symbol)
            MeasurementUnit.CM -> String.format("%.1f × %.1f × %.1f %s", w, h, t, unit.symbol)
            MeasurementUnit.INCH -> String.format("%.2f × %.2f × %.2f %s", w, h, t, unit.symbol)
        }
    }
}

object CutListGenerator {
    fun generateCutList(project: FurnitureProject): List<CutListItem> {
        val partsToCut = project.parts.filter { !it.type.isCutout }

        // Group by dimensions (within 0.1cm tolerance) and material
        val groups = mutableListOf<MutableList<FurniturePart>>()

        for (part in partsToCut) {
            var matchedGroup: MutableList<FurniturePart>? = null
            for (group in groups) {
                val rep = group.first()
                if (rep.material == part.material &&
                    areDimensionsEqual(rep.width, rep.height, rep.thickness, part.width, part.height, part.thickness)
                ) {
                    matchedGroup = group
                    break
                }
            }

            if (matchedGroup != null) {
                matchedGroup.add(part)
            } else {
                groups.add(mutableListOf(part))
            }
        }

        return groups.map { group ->
            val first = group.first()
            val baseName = cleanPartName(first.name)
            CutListItem(
                title = if (group.size == 1) first.name else "$baseName",
                width = first.width,
                height = first.height,
                thickness = first.thickness,
                quantity = group.size,
                material = first.material,
                partIds = group.map { it.id }
            )
        }
    }

    private fun areDimensionsEqual(
        w1: Float, h1: Float, t1: Float,
        w2: Float, h2: Float, t2: Float
    ): Boolean {
        // Match exact or rotated orientation (w vs h)
        val matchDirect = (Math.abs(w1 - w2) < 0.1f && Math.abs(h1 - h2) < 0.1f && Math.abs(t1 - t2) < 0.1f)
        val matchRotated = (Math.abs(w1 - h2) < 0.1f && Math.abs(h1 - w2) < 0.1f && Math.abs(t1 - t2) < 0.1f)
        return matchDirect || matchRotated
    }

    private fun cleanPartName(name: String): String {
        return name.replace(Regex("(Left|Right|Front|Back|Panel|1|2|3)"), "").trim().ifEmpty { name }
    }

    fun exportToText(project: FurnitureProject): String {
        val list = generateCutList(project)
        val sb = StringBuilder()
        sb.append("WOODWORKING CUTTING LIST\n")
        sb.append("Project: ${project.name}\n")
        sb.append("Unit: ${project.unit.label}\n")
        sb.append("Total Parts: ${project.totalPartsCount}\n")
        sb.append("Total Surface Area: ${String.format("%.2f", project.totalAreaM2)} m²\n")
        sb.append("Estimated 4x8' Sheets: ${String.format("%.1f", project.estimatedSheetsRequired)}\n")
        sb.append("------------------------------------\n\n")

        for (item in list) {
            sb.append("${item.title}:\n")
            sb.append("${item.formattedDimensions(project.unit)} — Qty ${item.quantity}\n")
            sb.append("Material: ${item.material.displayName}\n\n")
        }

        return sb.toString()
    }
}
