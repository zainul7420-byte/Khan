package com.example.utils

import com.example.model.FurnitureProject
import com.example.threeD.Mesh3D

object StlExporter {
    fun exportToStl(project: FurnitureProject): String {
        val sb = StringBuilder()
        val safeName = project.name.replace("[^a-zA-Z0-9_]".toRegex(), "_")
        sb.append("solid $safeName\n")

        val mesh = Mesh3D.buildFromParts(project.parts, null)

        for (face in mesh.faces) {
            val normal = face.normal
            val nx = normal.x
            val ny = normal.y
            val nz = normal.z

            if (face.vertexIndices.size == 4) {
                val v0 = mesh.vertices[face.vertexIndices[0]].position
                val v1 = mesh.vertices[face.vertexIndices[1]].position
                val v2 = mesh.vertices[face.vertexIndices[2]].position
                val v3 = mesh.vertices[face.vertexIndices[3]].position

                // Triangle 1 (0, 1, 2)
                appendFacet(sb, nx, ny, nz, v0, v1, v2)
                // Triangle 2 (0, 2, 3)
                appendFacet(sb, nx, ny, nz, v0, v2, v3)
            } else if (face.vertexIndices.size == 3) {
                val v0 = mesh.vertices[face.vertexIndices[0]].position
                val v1 = mesh.vertices[face.vertexIndices[1]].position
                val v2 = mesh.vertices[face.vertexIndices[2]].position
                appendFacet(sb, nx, ny, nz, v0, v1, v2)
            }
        }

        sb.append("endsolid $safeName\n")
        return sb.toString()
    }

    private fun appendFacet(
        sb: StringBuilder,
        nx: Float, ny: Float, nz: Float,
        v0: com.example.threeD.Vector3,
        v1: com.example.threeD.Vector3,
        v2: com.example.threeD.Vector3
    ) {
        sb.append(String.format(java.util.Locale.US, "  facet normal %.4f %.4f %.4f\n", nx, ny, nz))
        sb.append("    outer loop\n")
        sb.append(String.format(java.util.Locale.US, "      vertex %.4f %.4f %.4f\n", v0.x, v0.y, v0.z))
        sb.append(String.format(java.util.Locale.US, "      vertex %.4f %.4f %.4f\n", v1.x, v1.y, v1.z))
        sb.append(String.format(java.util.Locale.US, "      vertex %.4f %.4f %.4f\n", v2.x, v2.y, v2.z))
        sb.append("    endloop\n")
        sb.append("  endfacet\n")
    }
}
