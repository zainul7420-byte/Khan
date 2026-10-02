package com.example.model

import java.util.UUID

data class FurniturePart(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "Board",
    val type: PartType = PartType.BOARD,
    val width: Float = 100f,      // cm (local X axis)
    val height: Float = 50f,      // cm (local Y axis)
    val thickness: Float = 1.8f,  // cm (local Z axis)
    val positionX: Float = 0f,    // cm
    val positionY: Float = 0f,    // cm
    val positionZ: Float = 0f,    // cm
    val rotationX: Float = 0f,    // degrees
    val rotationY: Float = 0f,    // degrees
    val rotationZ: Float = 0f,    // degrees
    val material: WoodMaterial = WoodMaterial.LIGHT_OAK,
    val customColor: Long? = null,
    val cornerRadius: Float = 0f, // for rounded boards
    val isVisible: Boolean = true,
    val isLocked: Boolean = false
) {
    /**
     * Calculates surface area of one face in cm^2
     */
    val faceAreaCm2: Float
        get() = width * height

    /**
     * Calculates total volume in cm^3
     */
    val volumeCm3: Float
        get() = width * height * thickness

    /**
     * Board dimensions formatted string (e.g. "135 × 60 × 1.8 cm")
     */
    fun formattedDimensions(unit: MeasurementUnit = MeasurementUnit.CM): String {
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
