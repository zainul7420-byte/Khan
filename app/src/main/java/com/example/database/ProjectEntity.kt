package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.FurniturePart
import com.example.model.FurnitureProject
import com.example.model.MeasurementUnit

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val unit: String,
    val defaultThickness: Float,
    val createdAt: Long,
    val updatedAt: Long,
    val notes: String,
    val partsJson: String
) {
    fun toDomain(parts: List<FurniturePart>): FurnitureProject {
        return FurnitureProject(
            id = id,
            name = name,
            unit = try { MeasurementUnit.valueOf(unit) } catch (e: Exception) { MeasurementUnit.CM },
            defaultThickness = defaultThickness,
            createdAt = createdAt,
            updatedAt = updatedAt,
            notes = notes,
            parts = parts
        )
    }

    companion object {
        fun fromDomain(project: FurnitureProject, partsJson: String): ProjectEntity {
            return ProjectEntity(
                id = project.id,
                name = project.name,
                unit = project.unit.name,
                defaultThickness = project.defaultThickness,
                createdAt = project.createdAt,
                updatedAt = project.updatedAt,
                notes = project.notes,
                partsJson = partsJson
            )
        }
    }
}
