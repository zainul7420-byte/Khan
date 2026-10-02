package com.example.model

import androidx.compose.ui.graphics.Color

enum class WoodMaterial(
    val displayName: String,
    val primaryColor: Long,
    val secondaryColor: Long, // Grain line color
    val roughness: Float,
    val description: String
) {
    LIGHT_OAK(
        displayName = "Light Oak",
        primaryColor = 0xFFDDB88C,
        secondaryColor = 0xFFBF9668,
        roughness = 0.55f,
        description = "Natural honey-toned oak with subtle amber grain"
    ),
    DARK_OAK(
        displayName = "Dark Oak",
        primaryColor = 0xFF7A4B2A,
        secondaryColor = 0xFF543118,
        roughness = 0.45f,
        description = "Rich espresso-stained oak with deep pores"
    ),
    WALNUT(
        displayName = "Walnut",
        primaryColor = 0xFF4A3428,
        secondaryColor = 0xFF312117,
        roughness = 0.4f,
        description = "Luxurious dark chocolate American walnut"
    ),
    PINE(
        displayName = "Pine",
        primaryColor = 0xFFE8C88B,
        secondaryColor = 0xFFC9A25E,
        roughness = 0.65f,
        description = "Pale knotty softwood ideal for rustic work"
    ),
    WHITE(
        displayName = "White Laminate",
        primaryColor = 0xFFF3F4F6,
        secondaryColor = 0xFFE5E7EB,
        roughness = 0.3f,
        description = "Clean modern melamine edge-banded finish"
    ),
    BLACK(
        displayName = "Matte Black",
        primaryColor = 0xFF212529,
        secondaryColor = 0xFF14171A,
        roughness = 0.35f,
        description = "Sleek industrial black powder-coat / ebonized"
    ),
    CUSTOM(
        displayName = "Custom Color",
        primaryColor = 0xFF3B82F6,
        secondaryColor = 0xFF1D4ED8,
        roughness = 0.5f,
        description = "Custom tinted paint or lacquer"
    );

    fun getColor(customColorValue: Long? = null): Color {
        return if (this == CUSTOM && customColorValue != null) {
            Color(customColorValue)
        } else {
            Color(primaryColor)
        }
    }
}
