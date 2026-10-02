package com.example.threeD

import androidx.compose.ui.graphics.Color
import com.example.model.FurniturePart
import com.example.model.PartType
import kotlin.math.cos
import kotlin.math.sin

data class Vertex3D(
    val position: Vector3,
    val normal: Vector3 = Vector3(0f, 1f, 0f),
    val u: Float = 0f,
    val v: Float = 0f
)

data class Face3D(
    val vertexIndices: IntArray,
    val normal: Vector3,
    val center: Vector3,
    val baseColor: Color,
    val secondaryColor: Color,
    val partId: String,
    val isSelected: Boolean = false,
    val isCutout: Boolean = false,
    val roughness: Float = 0.5f
)

data class Edge3D(
    val startIndex: Int,
    val endIndex: Int,
    val partId: String,
    val isSelected: Boolean = false
)

class Mesh3D(
    val vertices: MutableList<Vertex3D> = mutableListOf(),
    val faces: MutableList<Face3D> = mutableListOf(),
    val edges: MutableList<Edge3D> = mutableListOf()
) {
    companion object {
        fun buildFromParts(parts: List<FurniturePart>, selectedPartId: String?): Mesh3D {
            val mesh = Mesh3D()
            for (part in parts) {
                if (!part.isVisible) continue
                val isSelected = part.id == selectedPartId
                addPartToMesh(mesh, part, isSelected)
            }
            return mesh
        }

        private fun addPartToMesh(mesh: Mesh3D, part: FurniturePart, isSelected: Boolean) {
            val baseIndex = mesh.vertices.size
            val hw = part.width * 0.5f
            val hh = part.height * 0.5f
            val ht = part.thickness * 0.5f

            val baseColor = part.material.getColor(part.customColor)
            val grainColor = Color(part.material.secondaryColor)

            // Local corner positions for standard box / board
            // X: width, Y: height (vertical or length), Z: thickness
            val localCorners = arrayOf(
                Vector3(-hw, -hh, -ht), // 0: Bottom-Left-Back
                Vector3(hw, -hh, -ht),  // 1: Bottom-Right-Back
                Vector3(hw, hh, -ht),   // 2: Top-Right-Back
                Vector3(-hw, hh, -ht),  // 3: Top-Left-Back
                Vector3(-hw, -hh, ht),  // 4: Bottom-Left-Front
                Vector3(hw, -hh, ht),   // 5: Bottom-Right-Front
                Vector3(hw, hh, ht),    // 6: Top-Right-Front
                Vector3(-hw, hh, ht)    // 7: Top-Left-Front
            )

            // Calculate transformation matrix: Translation * RotY * RotX * RotZ
            val transMat = Matrix4.translation(part.positionX, part.positionY, part.positionZ)
            val rotYMat = Matrix4.rotationY(part.rotationY)
            val rotXMat = Matrix4.rotationX(part.rotationX)
            val rotZMat = Matrix4.rotationZ(part.rotationZ)

            val worldMat = Matrix4()
            worldMat.multiply(transMat)
            worldMat.multiply(rotYMat)
            worldMat.multiply(rotXMat)
            worldMat.multiply(rotZMat)

            // Transform vertices to world space
            for (localCorner in localCorners) {
                val worldPos = worldMat.transform(localCorner)
                mesh.vertices.add(Vertex3D(worldPos))
            }

            // Define 6 box faces with outward normals
            val faceDefs = arrayOf(
                // Front face (+Z)
                FaceDef(intArrayOf(4, 5, 6, 7), Vector3(0f, 0f, 1f)),
                // Back face (-Z)
                FaceDef(intArrayOf(1, 0, 3, 2), Vector3(0f, 0f, -1f)),
                // Top face (+Y)
                FaceDef(intArrayOf(7, 6, 2, 3), Vector3(0f, 1f, 0f)),
                // Bottom face (-Y)
                FaceDef(intArrayOf(0, 1, 5, 4), Vector3(0f, -1f, 0f)),
                // Right face (+X)
                FaceDef(intArrayOf(5, 1, 2, 6), Vector3(1f, 0f, 0f)),
                // Left face (-X)
                FaceDef(intArrayOf(0, 4, 7, 3), Vector3(-1f, 0f, 0f))
            )

            for (def in faceDefs) {
                val indices = IntArray(def.indices.size) { i -> baseIndex + def.indices[i] }
                val worldNormal = worldMat.transformDirection(def.normal).normalized()
                
                var centerSum = Vector3.ZERO
                for (idx in indices) {
                    centerSum += mesh.vertices[idx].position
                }
                val faceCenter = centerSum / indices.size.toFloat()

                mesh.faces.add(
                    Face3D(
                        vertexIndices = indices,
                        normal = worldNormal,
                        center = faceCenter,
                        baseColor = if (part.type.isCutout) baseColor.copy(alpha = 0.4f) else baseColor,
                        secondaryColor = grainColor,
                        partId = part.id,
                        isSelected = isSelected,
                        isCutout = part.type.isCutout,
                        roughness = part.material.roughness
                    )
                )
            }

            // Add visual wireframe edges
            val edgePairs = arrayOf(
                // Bottom ring
                Pair(0, 1), Pair(1, 5), Pair(5, 4), Pair(4, 0),
                // Top ring
                Pair(3, 2), Pair(2, 6), Pair(6, 7), Pair(7, 3),
                // Vertical pillars
                Pair(0, 3), Pair(1, 2), Pair(5, 6), Pair(4, 7)
            )

            for (pair in edgePairs) {
                mesh.edges.add(
                    Edge3D(
                        startIndex = baseIndex + pair.first,
                        endIndex = baseIndex + pair.second,
                        partId = part.id,
                        isSelected = isSelected
                    )
                )
            }

            // If it's a drawer, add handle hardware geometry!
            if (part.type == PartType.DRAWER) {
                addDrawerHandle(mesh, worldMat, hw, hh, ht, part.id, isSelected)
            } else if (part.type == PartType.DOOR) {
                addDoorHandle(mesh, worldMat, hw, hh, ht, part.id, isSelected)
            }
        }

        private fun addDrawerHandle(
            mesh: Mesh3D,
            worldMat: Matrix4,
            hw: Float,
            hh: Float,
            ht: Float,
            partId: String,
            isSelected: Boolean
        ) {
            val handleWidth = 12f
            val handleHeight = 2f
            val handleThickness = 2.5f

            val hhw = handleWidth * 0.5f
            val hhh = handleHeight * 0.5f
            val frontZ = ht

            val handleBaseIdx = mesh.vertices.size
            val localHandleCorners = arrayOf(
                Vector3(-hhw, -hhh, frontZ),
                Vector3(hhw, -hhh, frontZ),
                Vector3(hhw, hhh, frontZ),
                Vector3(-hhw, hhh, frontZ),
                Vector3(-hhw, -hhh, frontZ + handleThickness),
                Vector3(hhw, -hhh, frontZ + handleThickness),
                Vector3(hhw, hhh, frontZ + handleThickness),
                Vector3(-hhw, hhh, frontZ + handleThickness)
            )

            for (c in localHandleCorners) {
                mesh.vertices.add(Vertex3D(worldMat.transform(c)))
            }

            val handleColor = Color(0xFF1E293B) // Dark metallic handle
            val faceIndices = arrayOf(
                intArrayOf(4, 5, 6, 7), // Front
                intArrayOf(7, 6, 2, 3), // Top
                intArrayOf(5, 1, 2, 6), // Right
                intArrayOf(0, 4, 7, 3), // Left
                intArrayOf(0, 1, 5, 4)  // Bottom
            )

            for (f in faceIndices) {
                val indices = IntArray(f.size) { i -> handleBaseIdx + f[i] }
                var centerSum = Vector3.ZERO
                for (idx in indices) centerSum += mesh.vertices[idx].position
                val center = centerSum / indices.size.toFloat()
                mesh.faces.add(
                    Face3D(
                        vertexIndices = indices,
                        normal = worldMat.transformDirection(Vector3(0f, 0f, 1f)).normalized(),
                        center = center,
                        baseColor = handleColor,
                        secondaryColor = handleColor,
                        partId = partId,
                        isSelected = isSelected,
                        roughness = 0.2f
                    )
                )
            }
        }

        private fun addDoorHandle(
            mesh: Mesh3D,
            worldMat: Matrix4,
            hw: Float,
            hh: Float,
            ht: Float,
            partId: String,
            isSelected: Boolean
        ) {
            val handleX = hw - 4f // Close to edge
            val handleRadius = 2.0f
            val frontZ = ht

            val handleBaseIdx = mesh.vertices.size
            val knobCorners = arrayOf(
                Vector3(handleX - handleRadius, -handleRadius, frontZ),
                Vector3(handleX + handleRadius, -handleRadius, frontZ),
                Vector3(handleX + handleRadius, handleRadius, frontZ),
                Vector3(handleX - handleRadius, handleRadius, frontZ),
                Vector3(handleX - handleRadius, -handleRadius, frontZ + 2.5f),
                Vector3(handleX + handleRadius, -handleRadius, frontZ + 2.5f),
                Vector3(handleX + handleRadius, handleRadius, frontZ + 2.5f),
                Vector3(handleX - handleRadius, handleRadius, frontZ + 2.5f)
            )

            for (c in knobCorners) {
                mesh.vertices.add(Vertex3D(worldMat.transform(c)))
            }

            val knobColor = Color(0xFFD97706) // Warm brass knob
            val indices = IntArray(4) { i -> handleBaseIdx + 4 + i }
            var centerSum = Vector3.ZERO
            for (idx in indices) centerSum += mesh.vertices[idx].position
            val center = centerSum / 4f

            mesh.faces.add(
                Face3D(
                    vertexIndices = indices,
                    normal = worldMat.transformDirection(Vector3(0f, 0f, 1f)).normalized(),
                    center = center,
                    baseColor = knobColor,
                    secondaryColor = knobColor,
                    partId = partId,
                    isSelected = isSelected,
                    roughness = 0.1f
                )
            )
        }
    }

    private data class FaceDef(val indices: IntArray, val normal: Vector3)
}
