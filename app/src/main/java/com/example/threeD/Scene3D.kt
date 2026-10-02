package com.example.threeD

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.model.FurniturePart
import com.example.model.MeasurementUnit
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

data class ProjectedPoint(
    val screenX: Float,
    val screenY: Float,
    val depth: Float,
    val isVisible: Boolean
)

data class ProjectedDimension(
    val label: String,
    val startScreen: Offset,
    val endScreen: Offset,
    val labelScreen: Offset,
    val isVisible: Boolean
)

class Scene3D {
    var yaw: Float = 35f       // degrees
    var pitch: Float = 24f     // degrees
    var distance: Float = 220f // cm
    var target: Vector3 = Vector3(0f, 35f, 0f)

    private val lightDir = Vector3(-0.4f, 0.8f, 0.5f).normalized()

    fun resetCamera() {
        yaw = 35f
        pitch = 24f
        distance = 220f
        target = Vector3(0f, 35f, 0f)
    }

    fun setViewPreset(preset: CameraPreset) {
        when (preset) {
            CameraPreset.PERSPECTIVE -> {
                yaw = 35f
                pitch = 24f
            }
            CameraPreset.FRONT -> {
                yaw = 0f
                pitch = 0f
            }
            CameraPreset.TOP -> {
                yaw = 0f
                pitch = 88f
            }
            CameraPreset.SIDE -> {
                yaw = 90f
                pitch = 0f
            }
            CameraPreset.ISOMETRIC -> {
                yaw = 45f
                pitch = 35.264f
            }
        }
    }

    fun orbit(deltaYaw: Float, deltaPitch: Float) {
        yaw = (yaw + deltaYaw) % 360f
        if (yaw < 0f) yaw += 360f
        pitch = (pitch + deltaPitch).coerceIn(-85f, 85f)
    }

    fun zoom(factor: Float) {
        distance = (distance / factor).coerceIn(30f, 800f)
    }

    fun pan(deltaScreenX: Float, deltaScreenY: Float) {
        val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
        val cosYaw = cos(radYaw)
        val sinYaw = sin(radYaw)

        // Move target along camera's right and up vectors in world space
        val panSpeed = distance * 0.0018f
        val dx = -deltaScreenX * panSpeed
        val dy = deltaScreenY * panSpeed

        val rightX = cosYaw * dx
        val rightZ = -sinYaw * dx

        target = Vector3(
            x = target.x + rightX,
            y = (target.y + dy).coerceIn(-50f, 300f),
            z = target.z + rightZ
        )
    }

    private fun getEyePosition(): Vector3 {
        val radYaw = Math.toRadians(yaw.toDouble()).toFloat()
        val radPitch = Math.toRadians(pitch.toDouble()).toFloat()
        val cosPitch = cos(radPitch)
        val sinPitch = sin(radPitch)

        return Vector3(
            x = target.x + distance * cosPitch * sin(radYaw),
            y = target.y + distance * sinPitch,
            z = target.z + distance * cosPitch * cos(radYaw)
        )
    }

    fun project(
        worldPos: Vector3,
        viewMat: Matrix4,
        projMat: Matrix4,
        viewportWidth: Float,
        viewportHeight: Float
    ): ProjectedPoint {
        val viewPos = viewMat.transform(worldPos)
        if (viewPos.z >= -1f) {
            return ProjectedPoint(0f, 0f, viewPos.z, false)
        }

        val clipPos = projMat.transform(viewPos)
        val screenX = (clipPos.x + 1f) * 0.5f * viewportWidth
        val screenY = (1f - clipPos.y) * 0.5f * viewportHeight

        return ProjectedPoint(screenX, screenY, -viewPos.z, true)
    }

    /**
     * Hit testing: returns partId of clicked face in screen space, or null.
     */
    fun hitTest(
        tapX: Float,
        tapY: Float,
        mesh: Mesh3D,
        viewportWidth: Float,
        viewportHeight: Float
    ): String? {
        val eye = getEyePosition()
        val viewMat = Matrix4.lookAt(eye, target, Vector3.UP)
        val aspect = viewportWidth / max(1f, viewportHeight)
        val projMat = Matrix4.perspective(42f, aspect, 5f, 2000f)

        // Project vertices
        val projVertices = mesh.vertices.map { v ->
            project(v.position, viewMat, projMat, viewportWidth, viewportHeight)
        }

        // Sort faces from front to back (closest to camera first)
        val sortedFaces = mesh.faces.sortedBy { f ->
            val vpos = viewMat.transform(f.center)
            vpos.z // larger (less negative) is closer in view space
        }.reversed()

        for (face in sortedFaces) {
            val poly = mutableListOf<Offset>()
            var allVisible = true
            for (idx in face.vertexIndices) {
                val p = projVertices[idx]
                if (!p.isVisible) {
                    allVisible = false
                    break
                }
                poly.add(Offset(p.screenX, p.screenY))
            }
            if (allVisible && isPointInPolygon(Offset(tapX, tapY), poly)) {
                return face.partId
            }
        }
        return null
    }

    private fun isPointInPolygon(p: Offset, poly: List<Offset>): Boolean {
        if (poly.size < 3) return false
        var inside = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val vi = poly[i]
            val vj = poly[j]
            if ((vi.y > p.y) != (vj.y > p.y) &&
                (p.x < (vj.x - vi.x) * (p.y - vi.y) / (vj.y - vi.y) + vi.x)
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }

    fun render(
        drawScope: DrawScope,
        parts: List<FurniturePart>,
        selectedPartId: String?,
        showGrid: Boolean = true,
        showDimensions: Boolean = true,
        unit: MeasurementUnit = MeasurementUnit.CM
    ) {
        val viewportWidth = drawScope.size.width
        val viewportHeight = drawScope.size.height

        val eye = getEyePosition()
        val viewMat = Matrix4.lookAt(eye, target, Vector3.UP)
        val aspect = viewportWidth / max(1f, viewportHeight)
        val projMat = Matrix4.perspective(42f, aspect, 5f, 2000f)

        // 1. Draw Ground Perspective Grid
        if (showGrid) {
            renderGrid(drawScope, viewMat, projMat, viewportWidth, viewportHeight)
        }

        // 2. Build 3D Mesh
        val mesh = Mesh3D.buildFromParts(parts, selectedPartId)

        // 3. Project Vertices
        val projVertices = mesh.vertices.map { v ->
            project(v.position, viewMat, projMat, viewportWidth, viewportHeight)
        }

        // 4. Ground Shadow for parts
        renderDropShadow(drawScope, parts, viewMat, projMat, viewportWidth, viewportHeight)

        // 5. Sort faces by distance to eye (Painter's algorithm)
        val sortedFaces = mesh.faces.map { face ->
            val distSq = (face.center - eye).length()
            Pair(face, distSq)
        }.sortedByDescending { it.second }.map { it.first }

        // 6. Draw Faces
        for (face in sortedFaces) {
            var anyBehind = false
            val poly = mutableListOf<Offset>()
            for (idx in face.vertexIndices) {
                val p = projVertices[idx]
                if (!p.isVisible) {
                    anyBehind = true
                    break
                }
                poly.add(Offset(p.screenX, p.screenY))
            }
            if (anyBehind || poly.size < 3) continue

            // Back-face culling check
            val viewToFace = (face.center - eye).normalized()
            val dotCam = face.normal.dot(viewToFace)
            if (dotCam > 0.05f) {
                // Facing away from camera
                continue
            }

            // Directional lighting calculation
            val diffuse = max(0f, face.normal.dot(lightDir))
            val ambient = 0.38f
            val lightIntensity = (ambient + diffuse * 0.62f).coerceIn(0.2f, 1.15f)

            // Calculate lit base color
            val r = (face.baseColor.red * lightIntensity).coerceIn(0f, 1f)
            val g = (face.baseColor.green * lightIntensity).coerceIn(0f, 1f)
            val b = (face.baseColor.blue * lightIntensity).coerceIn(0f, 1f)
            val litColor = Color(r, g, b, face.baseColor.alpha)

            // Draw Face Polygon
            val path = Path().apply {
                moveTo(poly[0].x, poly[0].y)
                for (i in 1 until poly.size) {
                    lineTo(poly[i].x, poly[i].y)
                }
                close()
            }
            drawScope.drawPath(path, litColor)

            // Subtle wood grain lines on front/top face
            if (!face.isCutout && poly.size == 4 && diffuse > 0.1f) {
                val grainColor = face.secondaryColor.copy(alpha = 0.18f * lightIntensity)
                drawWoodGrain(drawScope, poly, grainColor)
            }
        }

        // 7. Draw Wireframe Edges & Selection Highlights
        for (edge in mesh.edges) {
            val p1 = projVertices[edge.startIndex]
            val p2 = projVertices[edge.endIndex]
            if (!p1.isVisible || !p2.isVisible) continue

            if (edge.isSelected) {
                // Vibrant Blue selection glow + crisp border
                drawScope.drawLine(
                    color = Color(0xFF2563EB),
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
            } else {
                // Subtle clean CAD border
                drawScope.drawLine(
                    color = Color(0x35000000),
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = 1.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 8. Draw 3D Dimension Callouts for Selected Object
        if (showDimensions && selectedPartId != null) {
            val selectedPart = parts.find { it.id == selectedPartId }
            if (selectedPart != null) {
                renderPartDimensions(
                    drawScope,
                    selectedPart,
                    viewMat,
                    projMat,
                    viewportWidth,
                    viewportHeight,
                    unit
                )
            }
        }

        // 9. Draw 3D Orientation Axis in bottom-left
        renderAxisGizmo(drawScope, viewMat)
    }

    private fun drawWoodGrain(drawScope: DrawScope, quad: List<Offset>, color: Color) {
        val steps = 3
        for (i in 1..steps) {
            val t = i.toFloat() / (steps + 1)
            val start = Offset(
                quad[0].x * (1f - t) + quad[3].x * t,
                quad[0].y * (1f - t) + quad[3].y * t
            )
            val end = Offset(
                quad[1].x * (1f - t) + quad[2].x * t,
                quad[1].y * (1f - t) + quad[2].y * t
            )
            drawScope.drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 1.0f
            )
        }
    }

    private fun renderGrid(
        drawScope: DrawScope,
        viewMat: Matrix4,
        projMat: Matrix4,
        viewportWidth: Float,
        viewportHeight: Float
    ) {
        val gridSize = 150f // cm from center (-150 to +150)
        val step = 25f     // grid step every 25cm
        val minorStep = 5f

        val gridMinorColor = Color(0x18475569)
        val gridMajorColor = Color(0x38475569)
        val axisXColor = Color(0x60EF4444) // Red for X
        val axisZColor = Color(0x603B82F6) // Blue for Z

        var x = -gridSize
        while (x <= gridSize) {
            val p1 = project(Vector3(x, 0f, -gridSize), viewMat, projMat, viewportWidth, viewportHeight)
            val p2 = project(Vector3(x, 0f, gridSize), viewMat, projMat, viewportWidth, viewportHeight)
            if (p1.isVisible && p2.isVisible) {
                val color = when {
                    x == 0f -> axisZColor
                    x % step == 0f -> gridMajorColor
                    else -> gridMinorColor
                }
                drawScope.drawLine(
                    color = color,
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = if (x == 0f) 2f else 1f
                )
            }
            x += step
        }

        var z = -gridSize
        while (z <= gridSize) {
            val p1 = project(Vector3(-gridSize, 0f, z), viewMat, projMat, viewportWidth, viewportHeight)
            val p2 = project(Vector3(gridSize, 0f, z), viewMat, projMat, viewportWidth, viewportHeight)
            if (p1.isVisible && p2.isVisible) {
                val color = when {
                    z == 0f -> axisXColor
                    z % step == 0f -> gridMajorColor
                    else -> gridMinorColor
                }
                drawScope.drawLine(
                    color = color,
                    start = Offset(p1.screenX, p1.screenY),
                    end = Offset(p2.screenX, p2.screenY),
                    strokeWidth = if (z == 0f) 2f else 1f
                )
            }
            z += step
        }
    }

    private fun renderDropShadow(
        drawScope: DrawScope,
        parts: List<FurniturePart>,
        viewMat: Matrix4,
        projMat: Matrix4,
        viewportWidth: Float,
        viewportHeight: Float
    ) {
        if (parts.isEmpty()) return
        // Ground contact shadow under furniture
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minZ = Float.MAX_VALUE
        var maxZ = -Float.MAX_VALUE

        for (p in parts) {
            val hw = p.width * 0.5f
            val ht = p.thickness * 0.5f
            minX = min(minX, p.positionX - hw)
            maxX = max(maxX, p.positionX + hw)
            minZ = min(minZ, p.positionZ - ht)
            maxZ = max(maxZ, p.positionZ + ht)
        }

        val padding = 5f
        val c0 = project(Vector3(minX - padding, 0.05f, minZ - padding), viewMat, projMat, viewportWidth, viewportHeight)
        val c1 = project(Vector3(maxX + padding, 0.05f, minZ - padding), viewMat, projMat, viewportWidth, viewportHeight)
        val c2 = project(Vector3(maxX + padding, 0.05f, maxZ + padding), viewMat, projMat, viewportWidth, viewportHeight)
        val c3 = project(Vector3(minX - padding, 0.05f, maxZ + padding), viewMat, projMat, viewportWidth, viewportHeight)

        if (c0.isVisible && c1.isVisible && c2.isVisible && c3.isVisible) {
            val shadowPath = Path().apply {
                moveTo(c0.screenX, c0.screenY)
                lineTo(c1.screenX, c1.screenY)
                lineTo(c2.screenX, c2.screenY)
                lineTo(c3.screenX, c3.screenY)
                close()
            }
            drawScope.drawPath(shadowPath, Color(0x180F172A))
        }
    }

    private fun renderPartDimensions(
        drawScope: DrawScope,
        part: FurniturePart,
        viewMat: Matrix4,
        projMat: Matrix4,
        viewportWidth: Float,
        viewportHeight: Float,
        unit: MeasurementUnit
    ) {
        val hw = part.width * 0.5f
        val hh = part.height * 0.5f
        val ht = part.thickness * 0.5f

        val transMat = Matrix4.translation(part.positionX, part.positionY, part.positionZ)
        val rotYMat = Matrix4.rotationY(part.rotationY)
        val rotXMat = Matrix4.rotationX(part.rotationX)
        val rotZMat = Matrix4.rotationZ(part.rotationZ)

        val worldMat = Matrix4()
        worldMat.multiply(transMat)
        worldMat.multiply(rotYMat)
        worldMat.multiply(rotXMat)
        worldMat.multiply(rotZMat)

        val offsetDist = 6.0f // offset dimension line outward

        // 1. Width Dimension Line (along X axis at bottom front)
        val wStart = worldMat.transform(Vector3(-hw, -hh - offsetDist, ht + offsetDist))
        val wEnd = worldMat.transform(Vector3(hw, -hh - offsetDist, ht + offsetDist))
        val pw1 = project(wStart, viewMat, projMat, viewportWidth, viewportHeight)
        val pw2 = project(wEnd, viewMat, projMat, viewportWidth, viewportHeight)

        if (pw1.isVisible && pw2.isVisible) {
            drawDimensionLine(
                drawScope,
                Offset(pw1.screenX, pw1.screenY),
                Offset(pw2.screenX, pw2.screenY),
                "W: ${unit.format(part.width)}"
            )
        }

        // 2. Height Dimension Line (along Y axis on right side)
        val hStart = worldMat.transform(Vector3(hw + offsetDist, -hh, ht + offsetDist))
        val hEnd = worldMat.transform(Vector3(hw + offsetDist, hh, ht + offsetDist))
        val ph1 = project(hStart, viewMat, projMat, viewportWidth, viewportHeight)
        val ph2 = project(hEnd, viewMat, projMat, viewportWidth, viewportHeight)

        if (ph1.isVisible && ph2.isVisible) {
            drawDimensionLine(
                drawScope,
                Offset(ph1.screenX, ph1.screenY),
                Offset(ph2.screenX, ph2.screenY),
                "H: ${unit.format(part.height)}"
            )
        }

        // 3. Thickness / Depth Dimension Line (along Z axis on right side top)
        val tStart = worldMat.transform(Vector3(hw + offsetDist, hh, -ht))
        val tEnd = worldMat.transform(Vector3(hw + offsetDist, hh, ht))
        val pt1 = project(tStart, viewMat, projMat, viewportWidth, viewportHeight)
        val pt2 = project(tEnd, viewMat, projMat, viewportWidth, viewportHeight)

        if (pt1.isVisible && pt2.isVisible) {
            drawDimensionLine(
                drawScope,
                Offset(pt1.screenX, pt1.screenY),
                Offset(pt2.screenX, pt2.screenY),
                "T: ${unit.format(part.thickness)}"
            )
        }
    }

    private fun drawDimensionLine(
        drawScope: DrawScope,
        start: Offset,
        end: Offset,
        label: String
    ) {
        val lineColor = Color(0xFF2563EB)
        // Main dimension line
        drawScope.drawLine(
            color = lineColor,
            start = start,
            end = end,
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )

        // Tick marks at endpoints
        val dir = (end - start)
        val len = dir.getDistance()
        if (len > 0.001f) {
            val normal = Offset(-dir.y / len, dir.x / len) * 6f
            drawScope.drawLine(
                color = lineColor,
                start = start - normal,
                end = start + normal,
                strokeWidth = 2.0f
            )
            drawScope.drawLine(
                color = lineColor,
                start = end - normal,
                end = end + normal,
                strokeWidth = 2.0f
            )

            // Center indicator circle
            val mid = (start + end) * 0.5f
            drawScope.drawCircle(
                color = Color.White,
                radius = 4f,
                center = mid
            )
            drawScope.drawCircle(
                color = lineColor,
                radius = 4f,
                center = mid,
                style = Stroke(width = 1.5f)
            )
        }
    }

    private fun renderAxisGizmo(drawScope: DrawScope, viewMat: Matrix4) {
        val gizmoCenter = Offset(64f, drawScope.size.height - 64f)
        val axisLen = 36f

        // Transform base axis directions by view rotation
        val rotView = Matrix4()
        rotView.m[0] = viewMat.m[0]; rotView.m[1] = viewMat.m[1]; rotView.m[2] = viewMat.m[2]
        rotView.m[4] = viewMat.m[4]; rotView.m[5] = viewMat.m[5]; rotView.m[6] = viewMat.m[6]
        rotView.m[8] = viewMat.m[8]; rotView.m[9] = viewMat.m[9]; rotView.m[10] = viewMat.m[10]

        val xDir = rotView.transformDirection(Vector3(1f, 0f, 0f))
        val yDir = rotView.transformDirection(Vector3(0f, 1f, 0f))
        val zDir = rotView.transformDirection(Vector3(0f, 0f, 1f))

        // Draw Gizmo background circle
        drawScope.drawCircle(
            color = Color(0xCCFFFFFF),
            radius = 32f,
            center = gizmoCenter
        )
        drawScope.drawCircle(
            color = Color(0x30CBD5E1),
            radius = 32f,
            center = gizmoCenter,
            style = Stroke(width = 1.5f)
        )

        // X Axis (Red)
        drawScope.drawLine(
            color = Color(0xFFEF4444),
            start = gizmoCenter,
            end = gizmoCenter + Offset(xDir.x * axisLen, -xDir.y * axisLen),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Y Axis (Green)
        drawScope.drawLine(
            color = Color(0xFF10B981),
            start = gizmoCenter,
            end = gizmoCenter + Offset(yDir.x * axisLen, -yDir.y * axisLen),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )

        // Z Axis (Blue)
        drawScope.drawLine(
            color = Color(0xFF3B82F6),
            start = gizmoCenter,
            end = gizmoCenter + Offset(zDir.x * axisLen, -zDir.y * axisLen),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    /**
     * Calculates the screen bounds for a floating dimension badge overlay
     */
    fun getDimensionCallouts(
        part: FurniturePart,
        viewportWidth: Float,
        viewportHeight: Float,
        unit: MeasurementUnit
    ): List<ProjectedDimension> {
        val eye = getEyePosition()
        val viewMat = Matrix4.lookAt(eye, target, Vector3.UP)
        val aspect = viewportWidth / max(1f, viewportHeight)
        val projMat = Matrix4.perspective(42f, aspect, 5f, 2000f)

        val hw = part.width * 0.5f
        val hh = part.height * 0.5f
        val ht = part.thickness * 0.5f

        val transMat = Matrix4.translation(part.positionX, part.positionY, part.positionZ)
        val rotYMat = Matrix4.rotationY(part.rotationY)
        val rotXMat = Matrix4.rotationX(part.rotationX)
        val rotZMat = Matrix4.rotationZ(part.rotationZ)

        val worldMat = Matrix4()
        worldMat.multiply(transMat)
        worldMat.multiply(rotYMat)
        worldMat.multiply(rotXMat)
        worldMat.multiply(rotZMat)

        val offsetDist = 8.0f
        val result = mutableListOf<ProjectedDimension>()

        // Width Callout
        val wStart = worldMat.transform(Vector3(-hw, -hh - offsetDist, ht + offsetDist))
        val wEnd = worldMat.transform(Vector3(hw, -hh - offsetDist, ht + offsetDist))
        val pw1 = project(wStart, viewMat, projMat, viewportWidth, viewportHeight)
        val pw2 = project(wEnd, viewMat, projMat, viewportWidth, viewportHeight)
        if (pw1.isVisible && pw2.isVisible) {
            val mid = Offset((pw1.screenX + pw2.screenX) * 0.5f, (pw1.screenY + pw2.screenY) * 0.5f)
            result.add(
                ProjectedDimension(
                    label = "Width: ${unit.format(part.width)}",
                    startScreen = Offset(pw1.screenX, pw1.screenY),
                    endScreen = Offset(pw2.screenX, pw2.screenY),
                    labelScreen = mid,
                    isVisible = true
                )
            )
        }

        // Depth Callout (Z axis)
        val tStart = worldMat.transform(Vector3(hw + offsetDist, hh, -ht))
        val tEnd = worldMat.transform(Vector3(hw + offsetDist, hh, ht))
        val pt1 = project(tStart, viewMat, projMat, viewportWidth, viewportHeight)
        val pt2 = project(tEnd, viewMat, projMat, viewportWidth, viewportHeight)
        if (pt1.isVisible && pt2.isVisible) {
            val mid = Offset((pt1.screenX + pt2.screenX) * 0.5f, (pt1.screenY + pt2.screenY) * 0.5f)
            result.add(
                ProjectedDimension(
                    label = "Depth: ${unit.format(part.thickness)}",
                    startScreen = Offset(pt1.screenX, pt1.screenY),
                    endScreen = Offset(pt2.screenX, pt2.screenY),
                    labelScreen = mid,
                    isVisible = true
                )
            )
        }

        // Height Callout (Y axis)
        val hStart = worldMat.transform(Vector3(hw + offsetDist, -hh, ht + offsetDist))
        val hEnd = worldMat.transform(Vector3(hw + offsetDist, hh, ht + offsetDist))
        val ph1 = project(hStart, viewMat, projMat, viewportWidth, viewportHeight)
        val ph2 = project(hEnd, viewMat, projMat, viewportWidth, viewportHeight)
        if (ph1.isVisible && ph2.isVisible) {
            val mid = Offset((ph1.screenX + ph2.screenX) * 0.5f, (ph1.screenY + ph2.screenY) * 0.5f)
            result.add(
                ProjectedDimension(
                    label = "Height: ${unit.format(part.height)}",
                    startScreen = Offset(ph1.screenX, ph1.screenY),
                    endScreen = Offset(ph2.screenX, ph2.screenY),
                    labelScreen = mid,
                    isVisible = true
                )
            )
        }

        return result
    }
}

enum class CameraPreset {
    PERSPECTIVE,
    FRONT,
    TOP,
    SIDE,
    ISOMETRIC
}
