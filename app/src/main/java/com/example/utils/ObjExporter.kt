package com.example.utils

import com.example.model.FurnitureProject
import com.example.threeD.Mesh3D

object ObjExporter {
    fun exportToObj(project: FurnitureProject): String {
        val sb = StringBuilder()
        sb.append("# WoodCraft 3D Wavefront OBJ Exporter\n")
        sb.append("# Project: ${project.name}\n")
        sb.append("# Date: ${java.util.Date()}\n\n")

        val mesh = Mesh3D.buildFromParts(project.parts, null)

        // Write vertices (convert cm to meters for standard 3D CAD meters)
        for (v in mesh.vertices) {
            val vx = v.position.x * 0.01f
            val vy = v.position.y * 0.01f
            val vz = v.position.z * 0.01f
            sb.append(String.format(java.util.Locale.US, "v %.4f %.4f %.4f\n", vx, vy, vz))
        }
        sb.append("\n")

        // Group faces by part
        var currentPartId = ""
        for (face in mesh.faces) {
            if (face.partId != currentPartId) {
                currentPartId = face.partId
                val partName = project.parts.find { it.id == currentPartId }?.name ?: "Part"
                sb.append("g ${partName.replace(" ", "_")}\n")
                sb.append("usemtl ${face.baseColor}\n")
            }

            // OBJ is 1-indexed
            if (face.vertexIndices.size == 4) {
                val i0 = face.vertexIndices[0] + 1
                val i1 = face.vertexIndices[1] + 1
                val i2 = face.vertexIndices[2] + 1
                val i3 = face.vertexIndices[3] + 1
                // Quad as two triangles
                sb.append("f $i0 $i1 $i2\n")
                sb.append("f $i0 $i2 $i3\n")
            } else if (face.vertexIndices.size == 3) {
                val i0 = face.vertexIndices[0] + 1
                val i1 = face.vertexIndices[1] + 1
                val i2 = face.vertexIndices[2] + 1
                sb.append("f $i0 $i1 $i2\n")
            }
        }

        return sb.toString()
    }
}
