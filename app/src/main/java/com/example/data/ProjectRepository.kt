package com.example.data

import com.example.database.Converters
import com.example.database.ProjectDao
import com.example.database.ProjectEntity
import com.example.model.FurniturePart
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit
import com.example.model.PartType
import com.example.model.WoodMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val converters: Converters = Converters()
) {
    val allProjects: Flow<List<FurnitureProject>> = projectDao.getAllProjects().map { entities ->
        entities.map { entity ->
            val parts = converters.toPartsList(entity.partsJson)
            entity.toDomain(parts)
        }
    }

    fun getProject(id: String): Flow<FurnitureProject?> = projectDao.getProjectById(id).map { entity ->
        entity?.let {
            val parts = converters.toPartsList(it.partsJson)
            it.toDomain(parts)
        }
    }

    suspend fun saveProject(project: FurnitureProject) {
        val updatedProject = project.copy(updatedAt = System.currentTimeMillis())
        val partsJson = converters.fromPartsList(updatedProject.parts)
        val entity = ProjectEntity.fromDomain(updatedProject, partsJson)
        projectDao.insertProject(entity)
    }

    suspend fun deleteProject(id: String) {
        projectDao.deleteProjectById(id)
    }

    suspend fun duplicateProject(project: FurnitureProject): FurnitureProject {
        val newProject = project.copy(
            id = UUID.randomUUID().toString(),
            name = "${project.name} (Copy)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            parts = project.parts.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        saveProject(newProject)
        return newProject
    }

    suspend fun initializeDefaultProjectsIfEmpty() {
        if (projectDao.getProjectCount() == 0) {
            val deskProject = createDefaultDeskProject()
            saveProject(deskProject)

            val recordPlayerProject = createRecordPlayerProject()
            saveProject(recordPlayerProject)

            val workshopProject = createWorkshopWorkbenchProject()
            saveProject(workshopProject)

            val bookshelfProject = createBookshelfProject()
            saveProject(bookshelfProject)
        }
    }

    companion object {
        fun createDefaultDeskProject(): FurnitureProject {
            val deskId = "sample_desk_01"
            return FurnitureProject(
                id = deskId,
                name = "Computer Desk",
                unit = MeasurementUnit.CM,
                defaultThickness = 1.8f,
                notes = "Solid desktop with left and right support panels and back modesty stretcher. Designed for standard 75cm ergonomic height.",
                parts = listOf(
                    // Desktop: 135 x 60 x 1.8 cm (Horizontal surface at top)
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Desktop",
                        type = PartType.ROUNDED_FRONT,
                        width = 135f,
                        height = 1.8f,
                        thickness = 60f,
                        positionX = 0f,
                        positionY = 74.1f,
                        positionZ = 0f,
                        material = WoodMaterial.LIGHT_OAK
                    ),
                    // Left Side: 60 x 75 x 1.8 cm
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Left Side Panel",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 75f,
                        thickness = 60f,
                        positionX = -66.6f,
                        positionY = 37.5f,
                        positionZ = 0f,
                        material = WoodMaterial.LIGHT_OAK
                    ),
                    // Right Side: 60 x 75 x 1.8 cm
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Right Side Panel",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 75f,
                        thickness = 60f,
                        positionX = 66.6f,
                        positionY = 37.5f,
                        positionZ = 0f,
                        material = WoodMaterial.LIGHT_OAK
                    ),
                    // Front / Back Stretcher Support: 131.4 x 30 x 1.8 cm
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Modesty Support",
                        type = PartType.BOARD,
                        width = 131.4f,
                        height = 30f,
                        thickness = 1.8f,
                        positionX = 0f,
                        positionY = 55f,
                        positionZ = -20f,
                        material = WoodMaterial.LIGHT_OAK
                    )
                )
            )
        }

        fun createRecordPlayerProject(): FurnitureProject {
            return FurnitureProject(
                id = "sample_record_player_02",
                name = "Record Player Console",
                unit = MeasurementUnit.CM,
                defaultThickness = 1.8f,
                notes = "Mid-century modern audio station with dual 12-inch vinyl storage bays and amplifier shelf.",
                parts = listOf(
                    // Top Deck
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Console Top",
                        type = PartType.ROUNDED_BOARD,
                        width = 95f,
                        height = 1.8f,
                        thickness = 45f,
                        positionX = 0f,
                        positionY = 68f,
                        positionZ = 0f,
                        material = WoodMaterial.WALNUT
                    ),
                    // Bottom Deck
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Console Base",
                        type = PartType.BOARD,
                        width = 95f,
                        height = 1.8f,
                        thickness = 45f,
                        positionX = 0f,
                        positionY = 28f,
                        positionZ = 0f,
                        material = WoodMaterial.WALNUT
                    ),
                    // Left Side
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Left Wall",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 38.2f,
                        thickness = 45f,
                        positionX = -46.6f,
                        positionY = 48f,
                        positionZ = 0f,
                        material = WoodMaterial.WALNUT
                    ),
                    // Right Side
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Right Wall",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 38.2f,
                        thickness = 45f,
                        positionX = 46.6f,
                        positionY = 48f,
                        positionZ = 0f,
                        material = WoodMaterial.WALNUT
                    ),
                    // Center Divider
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Center Vinyl Divider",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 38.2f,
                        thickness = 43f,
                        positionX = 0f,
                        positionY = 48f,
                        positionZ = 0f,
                        material = WoodMaterial.WALNUT
                    ),
                    // Left Front Leg
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Front-Left",
                        type = PartType.LEG,
                        width = 3.5f,
                        height = 26f,
                        thickness = 3.5f,
                        positionX = -40f,
                        positionY = 13f,
                        positionZ = 16f,
                        material = WoodMaterial.BLACK
                    ),
                    // Right Front Leg
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Front-Right",
                        type = PartType.LEG,
                        width = 3.5f,
                        height = 26f,
                        thickness = 3.5f,
                        positionX = 40f,
                        positionY = 13f,
                        positionZ = 16f,
                        material = WoodMaterial.BLACK
                    ),
                    // Left Back Leg
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Back-Left",
                        type = PartType.LEG,
                        width = 3.5f,
                        height = 26f,
                        thickness = 3.5f,
                        positionX = -40f,
                        positionY = 13f,
                        positionZ = -16f,
                        material = WoodMaterial.BLACK
                    ),
                    // Right Back Leg
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Back-Right",
                        type = PartType.LEG,
                        width = 3.5f,
                        height = 26f,
                        thickness = 3.5f,
                        positionX = 40f,
                        positionY = 13f,
                        positionZ = -16f,
                        material = WoodMaterial.BLACK
                    )
                )
            )
        }

        fun createWorkshopWorkbenchProject(): FurnitureProject {
            return FurnitureProject(
                id = "sample_workbench_03",
                name = "Workshop Workbench",
                unit = MeasurementUnit.CM,
                defaultThickness = 3.2f,
                notes = "Heavy duty workshop table with lower tool storage deck and double laminated top.",
                parts = listOf(
                    // Heavy Top Deck: 150 x 70 x 3.6 cm
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Butcher Block Top",
                        type = PartType.BOARD,
                        width = 150f,
                        height = 3.6f,
                        thickness = 70f,
                        positionX = 0f,
                        positionY = 88f,
                        positionZ = 0f,
                        material = WoodMaterial.PINE
                    ),
                    // 4 Heavy Corner Legs (7 x 7 cm solid timber)
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Front-Left",
                        type = PartType.LEG,
                        width = 7f,
                        height = 86f,
                        thickness = 7f,
                        positionX = -68f,
                        positionY = 43f,
                        positionZ = 28f,
                        material = WoodMaterial.PINE
                    ),
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Front-Right",
                        type = PartType.LEG,
                        width = 7f,
                        height = 86f,
                        thickness = 7f,
                        positionX = 68f,
                        positionY = 43f,
                        positionZ = 28f,
                        material = WoodMaterial.PINE
                    ),
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Back-Left",
                        type = PartType.LEG,
                        width = 7f,
                        height = 86f,
                        thickness = 7f,
                        positionX = -68f,
                        positionY = 43f,
                        positionZ = -28f,
                        material = WoodMaterial.PINE
                    ),
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Leg Back-Right",
                        type = PartType.LEG,
                        width = 7f,
                        height = 86f,
                        thickness = 7f,
                        positionX = 68f,
                        positionY = 43f,
                        positionZ = -28f,
                        material = WoodMaterial.PINE
                    ),
                    // Lower Shelf for heavy toolboxes
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Bottom Tool Shelf",
                        type = PartType.SHELF,
                        width = 136f,
                        height = 1.8f,
                        thickness = 56f,
                        positionX = 0f,
                        positionY = 22f,
                        positionZ = 0f,
                        material = WoodMaterial.PINE
                    )
                )
            )
        }

        fun createBookshelfProject(): FurnitureProject {
            return FurnitureProject(
                id = "sample_bookshelf_04",
                name = "Modern Bookshelf",
                unit = MeasurementUnit.CM,
                defaultThickness = 1.8f,
                notes = "3-shelf bookcase with clean Nordic proportions and vertical dividers.",
                parts = listOf(
                    // Left Outer Side: 120 x 28 x 1.8 cm
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Left Side",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 120f,
                        thickness = 28f,
                        positionX = -40f,
                        positionY = 60f,
                        positionZ = 0f,
                        material = WoodMaterial.DARK_OAK
                    ),
                    // Right Outer Side
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Right Side",
                        type = PartType.BOARD,
                        width = 1.8f,
                        height = 120f,
                        thickness = 28f,
                        positionX = 40f,
                        positionY = 60f,
                        positionZ = 0f,
                        material = WoodMaterial.DARK_OAK
                    ),
                    // Top Cap
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Top Panel",
                        type = PartType.BOARD,
                        width = 81.8f,
                        height = 1.8f,
                        thickness = 28f,
                        positionX = 0f,
                        positionY = 120f,
                        positionZ = 0f,
                        material = WoodMaterial.DARK_OAK
                    ),
                    // Bottom Cap
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Bottom Panel",
                        type = PartType.BOARD,
                        width = 81.8f,
                        height = 1.8f,
                        thickness = 28f,
                        positionX = 0f,
                        positionY = 6f,
                        positionZ = 0f,
                        material = WoodMaterial.DARK_OAK
                    ),
                    // Middle Shelf 1
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Shelf 1",
                        type = PartType.SHELF,
                        width = 78.2f,
                        height = 1.8f,
                        thickness = 28f,
                        positionX = 0f,
                        positionY = 44f,
                        positionZ = 0f,
                        material = WoodMaterial.LIGHT_OAK
                    ),
                    // Middle Shelf 2
                    FurniturePart(
                        id = UUID.randomUUID().toString(),
                        name = "Shelf 2",
                        type = PartType.SHELF,
                        width = 78.2f,
                        height = 1.8f,
                        thickness = 28f,
                        positionX = 0f,
                        positionY = 82f,
                        positionZ = 0f,
                        material = WoodMaterial.LIGHT_OAK
                    )
                )
            )
        }
    }
}
