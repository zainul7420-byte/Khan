package com.example.model

enum class PartType(
    val title: String,
    val description: String,
    val defaultWidth: Float,     // cm
    val defaultHeight: Float,    // cm (depth/length)
    val defaultThickness: Float, // cm
    val isCutout: Boolean = false
) {
    BOARD(
        title = "Board",
        description = "Standard rectangular wooden plank or panel",
        defaultWidth = 100f,
        defaultHeight = 50f,
        defaultThickness = 1.8f
    ),
    ROUNDED_BOARD(
        title = "Rounded Board",
        description = "Wooden board with smoothly rounded 4 corners",
        defaultWidth = 100f,
        defaultHeight = 50f,
        defaultThickness = 1.8f
    ),
    ROUNDED_FRONT(
        title = "Rounded Front",
        description = "Board with soft bullnose front edge",
        defaultWidth = 100f,
        defaultHeight = 50f,
        defaultThickness = 1.8f
    ),
    SHELF(
        title = "Shelf",
        description = "Horizontal shelf insert panel",
        defaultWidth = 80f,
        defaultHeight = 35f,
        defaultThickness = 1.8f
    ),
    DRAWER(
        title = "Drawer",
        description = "Sliding box drawer with face panel and handle",
        defaultWidth = 45f,
        defaultHeight = 40f,
        defaultThickness = 18f
    ),
    DOOR(
        title = "Door",
        description = "Hinged cabinet door with front pull handle",
        defaultWidth = 45f,
        defaultHeight = 70f,
        defaultThickness = 1.8f
    ),
    LEG(
        title = "Leg",
        description = "Solid square or tapered furniture leg support",
        defaultWidth = 4.5f,
        defaultHeight = 4.5f,
        defaultThickness = 72f
    ),
    HOLE_CIRCULAR(
        title = "Circular Hole",
        description = "Cable grommet or dowel drill hole cutout",
        defaultWidth = 6f,
        defaultHeight = 6f,
        defaultThickness = 2.5f,
        isCutout = true
    ),
    HOLE_RECTANGULAR(
        title = "Rectangular Hole",
        description = "Cable pass-through or mortise slot cutout",
        defaultWidth = 12f,
        defaultHeight = 6f,
        defaultThickness = 2.5f,
        isCutout = true
    ),
    CUTOUT_RECTANGULAR(
        title = "Rectangular Cutout",
        description = "Corner or side notch cutout for joints",
        defaultWidth = 10f,
        defaultHeight = 10f,
        defaultThickness = 2.5f,
        isCutout = true
    )
}
