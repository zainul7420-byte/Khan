package com.example.threeD

import kotlin.math.max
import kotlin.math.min

data class Ray(
    val origin: Vector3,
    val direction: Vector3
)

data class BoundingBox3D(
    val min: Vector3,
    val max: Vector3
) {
    val center: Vector3
        get() = Vector3(
            (min.x + max.x) * 0.5f,
            (min.y + max.y) * 0.5f,
            (min.z + max.z) * 0.5f
        )

    val size: Vector3
        get() = Vector3(
            max.x - min.x,
            max.y - min.y,
            max.z - min.z
        )

    fun getCorners(): List<Vector3> {
        return listOf(
            Vector3(min.x, min.y, min.z),
            Vector3(max.x, min.y, min.z),
            Vector3(max.x, min.y, max.z),
            Vector3(min.x, min.y, max.z),
            Vector3(min.x, max.y, min.z),
            Vector3(max.x, max.y, min.z),
            Vector3(max.x, max.y, max.z),
            Vector3(min.x, max.y, max.z)
        )
    }

    fun intersects(ray: Ray): Boolean {
        var tmin = (min.x - ray.origin.x) / ray.direction.x
        var tmax = (max.x - ray.origin.x) / ray.direction.x

        if (tmin > tmax) {
            val temp = tmin
            tmin = tmax
            tmax = temp
        }

        var tymin = (min.y - ray.origin.y) / ray.direction.y
        var tymax = (max.y - ray.origin.y) / ray.direction.y

        if (tymin > tymax) {
            val temp = tymin
            tymin = tymax
            tymax = temp
        }

        if (tmin > tymax || tymin > tmax) return false

        if (tymin > tmin) tmin = tymin
        if (tymax < tmax) tmax = tymax

        var tzmin = (min.z - ray.origin.z) / ray.direction.z
        var tzmax = (max.z - ray.origin.z) / ray.direction.z

        if (tzmin > tzmax) {
            val temp = tzmin
            tzmin = tzmax
            tzmax = temp
        }

        if (tmin > tzmax || tzmin > tmax) return false

        return true
    }

    companion object {
        fun fromPartLocal(width: Float, height: Float, thickness: Float): BoundingBox3D {
            val halfW = width * 0.5f
            val halfH = height * 0.5f
            val halfT = thickness * 0.5f
            return BoundingBox3D(
                min = Vector3(-halfW, -halfH, -halfT),
                max = Vector3(halfW, halfH, halfT)
            )
        }
    }
}
