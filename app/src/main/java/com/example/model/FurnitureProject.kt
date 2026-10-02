package com.example.model

import java.util.UUID

data class FurnitureProject(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "New project",
    val unit: MeasurementUnit = MeasurementUnit.CM,
    val defaultThickness: Float = 1.8f,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val notes: String = "",
    val parts: List<FurniturePart> = emptyList()
) {
    val totalPartsCount: Int
        get() = parts.filter { !it.type.isCutout }.size

    /**
     * Total wood area in square meters
     */
    val totalAreaM2: Float
        get() = parts.filter { !it.type.isCutout }.sumOf { (it.faceAreaCm2).toDouble() }.toFloat() / 10000f

    /**
     * Estimated standard 244 x 122 cm (8x4 ft) plywood sheets required with 15% kerf allowance
     */
    val estimatedSheetsRequired: Float
        get() {
            val sheetAreaM2 = (2.44f * 1.22f)
            val effectiveArea = totalAreaM2 * 1.15f
            return if (sheetAreaM2 > 0) (effectiveArea / sheetAreaM2) else 0f
        }
}
