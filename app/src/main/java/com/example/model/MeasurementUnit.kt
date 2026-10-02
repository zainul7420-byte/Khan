package com.example.model

enum class MeasurementUnit(
    val symbol: String,
    val label: String,
    val toCmFactor: Float,
    val fromCmFactor: Float
) {
    CM(
        symbol = "cm",
        label = "Centimeters",
        toCmFactor = 1.0f,
        fromCmFactor = 1.0f
    ),
    MM(
        symbol = "mm",
        label = "Millimeters",
        toCmFactor = 0.1f,
        fromCmFactor = 10.0f
    ),
    INCH(
        symbol = "in",
        label = "Inches",
        toCmFactor = 2.54f,
        fromCmFactor = 1f / 2.54f
    );

    fun format(valueInCm: Float): String {
        val converted = valueInCm * fromCmFactor
        return when (this) {
            MM -> String.format("%.0f %s", converted, symbol)
            CM -> {
                if (converted % 1.0f == 0f) {
                    String.format("%.0f %s", converted, symbol)
                } else {
                    String.format("%.1f %s", converted, symbol)
                }
            }
            INCH -> String.format("%.2f %s", converted, symbol)
        }
    }

    fun parseToCm(input: Float): Float {
        return input * toCmFactor
    }

    fun fromCm(valueInCm: Float): Float {
        return valueInCm * fromCmFactor
    }
}
